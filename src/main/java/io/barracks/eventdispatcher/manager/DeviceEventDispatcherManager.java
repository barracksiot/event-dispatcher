/*
 * MIT License
 *
 * Copyright (c) 2017 Barracks Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.barracks.eventdispatcher.manager;

import io.barracks.eventdispatcher.client.AuthorizationServiceClient;
import io.barracks.eventdispatcher.client.QueuingServiceClient;
import io.barracks.eventdispatcher.model.DeviceChangeEvent;
import io.barracks.eventdispatcher.model.DeviceEvent;
import io.barracks.eventdispatcher.model.EventType;
import io.barracks.eventdispatcher.model.Hook;
import io.barracks.eventdispatcher.repository.HookRepository;
import io.barracks.eventdispatcher.repository.exception.HookNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeviceEventDispatcherManager {

    private AuthorizationServiceClient authorizationServiceClient;

    private QueuingServiceClient queuingServiceClient;

    private HookRepository hookRepository;

    @Autowired
    public DeviceEventDispatcherManager(AuthorizationServiceClient authorizationServiceClient,
                                        QueuingServiceClient queuingServiceClient,
                                        HookRepository hookRepository) {
        this.authorizationServiceClient = authorizationServiceClient;
        this.queuingServiceClient = queuingServiceClient;
        this.hookRepository = hookRepository;
    }

    public void postDeviceEvent(DeviceEvent deviceEvent) {

        sendMessageToAllHookPagesForType(deviceEvent, 0, EventType.PING);
    }

    public void postDeviceEnrollment(DeviceEvent deviceEvent) {

        final DeviceEvent event = deviceEvent.toBuilder()
                .request(
                        deviceEvent.getRequest().toBuilder()
                                .userId(deviceEvent.getUserId())
                                .unitId(deviceEvent.getUnitId())
                                .build())
                .build();
        sendMessageToAllHookPagesForType(event, 0, EventType.ENROLLMENT);
    }

    public void postDeviceChangeEvent(DeviceChangeEvent deviceChangeEvent, EventType eventType) {

        final DeviceEvent deviceEvent = deviceChangeEvent.getDeviceEvent();
        final DeviceChangeEvent event = deviceChangeEvent.toBuilder()
                .deviceEvent(deviceEvent.toBuilder()
                        .request(
                                deviceEvent.getRequest().toBuilder()
                                        .userId(deviceEvent.getUserId())
                                        .unitId(deviceEvent.getUnitId())
                                        .build())
                        .build()
                )
                .build();
        sendMessageToAllHookWithChangePagesForType(event, 0, eventType);
    }

    private void sendMessageToAllHookPagesForType(DeviceEvent deviceEvent, int pageIndex, EventType eventType) {
        final Pageable pageable = new PageRequest(pageIndex, 100);
        final Page<Hook> page = hookRepository.getHooksByEventType(deviceEvent.getRequest().getUserId(), pageable, eventType.name());
        page.getContent().forEach(hook ->
                queuingServiceClient.postDeviceEventHook(deviceEvent, hook)
        );
        final long totalPages = page.getTotalPages();
        if (pageIndex < totalPages - 1) {
            sendMessageToAllHookPagesForType(deviceEvent, ++pageIndex, eventType);
        }
    }

    private void sendMessageToAllHookWithChangePagesForType(DeviceChangeEvent deviceEvent, int pageIndex, EventType eventType) {
        final Pageable pageable = new PageRequest(pageIndex, 100);
        final Page<Hook> page = hookRepository.getHooksByEventType(deviceEvent.getDeviceEvent().getRequest().getUserId(), pageable, eventType.name());
        page.getContent().forEach(hook ->
                queuingServiceClient.postDeviceChangeEventHook(deviceEvent, hook)
        );
        final long totalPages = page.getTotalPages();
        if (pageIndex < totalPages - 1) {
            sendMessageToAllHookWithChangePagesForType(deviceEvent, ++pageIndex, eventType);
        }
    }

    public Hook saveHook(Hook hook) {
        return hookRepository.saveHook(hook);
    }

    public Hook getHook(String userId, String name) {
        return hookRepository.getHookByUserIdAndName(userId, name).orElseThrow(() -> new HookNotFoundException(userId, name));
    }

    public Page<Hook> getHooks(String userId, Pageable pageable) {
        return hookRepository.getHooks(userId, pageable);
    }

    public void deleteHook(String userId, String name) {
        hookRepository.deleteHook(userId, name);
    }

    public Hook updateHook(String userId, String name, Hook hook) {
        final Hook oldHook = hookRepository.getHookByUserIdAndName(userId, name).get();
        hook.setId(oldHook.getId());
        return hookRepository.updateHook(name, hook);
    }
}
