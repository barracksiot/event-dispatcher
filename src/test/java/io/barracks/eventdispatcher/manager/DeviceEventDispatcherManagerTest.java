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
import io.barracks.eventdispatcher.model.*;
import io.barracks.eventdispatcher.repository.HookRepository;
import io.barracks.eventdispatcher.repository.exception.HookNotFoundException;
import io.barracks.eventdispatcher.utils.DeviceChangeEventUtils;
import io.barracks.eventdispatcher.utils.DeviceEventUtils;
import io.barracks.eventdispatcher.utils.DeviceRequestUtils;
import io.barracks.eventdispatcher.utils.WebhookUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceEventDispatcherManagerTest {

    @Mock
    private AuthorizationServiceClient authorizationServiceClient;

    @Mock
    private QueuingServiceClient queuingServiceClient;

    private DeviceEventDispatcherManager deviceEventDispatcherManager;

    @Mock
    private HookRepository hookRepository;

    @Before
    public void setUp() {
        deviceEventDispatcherManager = new DeviceEventDispatcherManager(authorizationServiceClient, queuingServiceClient, hookRepository);
    }

    @Test
    public void postDeviceEvent_whenNoHooks_shouldNotSendToAnyone() throws Exception {
        // Given
        final DeviceRequest deviceRequest = DeviceRequestUtils.getDeviceRequest().toBuilder().build();
        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent().toBuilder().request(deviceRequest).build();

        doReturn(new PageImpl<Hook>(new ArrayList<>())).when(hookRepository).getHooksByEventType(eq(deviceRequest.getUserId()), any(Pageable.class), eq(EventType.PING.name()));

        // When
        deviceEventDispatcherManager.postDeviceEvent(deviceEvent);

        // When / Then
        verify(hookRepository).getHooksByEventType(eq(deviceRequest.getUserId()), any(Pageable.class), eq(EventType.PING.name()));
        verify(queuingServiceClient, never()).postDeviceEventHook(eq(deviceEvent), any(Hook.class));
    }

    @Test
    public void postDeviceEvent_whenHooks_shouldSendToHooks() throws Exception {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final DeviceRequest deviceRequest = DeviceRequestUtils.getDeviceRequest().toBuilder()
                .build();

        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent().toBuilder()
                .request(deviceRequest)
                .build();
        final Hook hook1 = WebhookUtils.getWebhook();
        final Hook hook2 = WebhookUtils.getWebhook();
        final List<Hook> hookList = Arrays.asList(hook1, hook2);
        final Page hookPage = new PageImpl(hookList, pageable, 2L);

        doReturn(hookPage).when(hookRepository).getHooksByEventType(eq(deviceRequest.getUserId()), any(Pageable.class), eq(EventType.PING.name()));

        // When
        deviceEventDispatcherManager.postDeviceEvent(deviceEvent);

        // When / Then
        verify(hookRepository).getHooksByEventType(eq(deviceRequest.getUserId()), any(Pageable.class), eq(EventType.PING.name()));
        verify(queuingServiceClient, new Times(2)).postDeviceEventHook(eq(deviceEvent), any(Hook.class));
    }

    @Test
    public void postDeviceEvent_whenManyHooks_shouldSendToHooks() throws Exception {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final DeviceRequest deviceRequest = DeviceRequestUtils.getDeviceRequest().toBuilder()
                .build();

        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent().toBuilder()
                .request(deviceRequest)
                .build();

        final List<Hook> hookList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            hookList.add(WebhookUtils.getWebhook(deviceRequest.getUserId()));
        }

        final Page hookPage = new PageImpl(hookList, pageable, 15L);

        doReturn(hookPage).when(hookRepository).getHooksByEventType(eq(deviceRequest.getUserId()), any(Pageable.class), eq(EventType.PING.name()));

        // When
        deviceEventDispatcherManager.postDeviceEvent(deviceEvent);

        // When / Then
        verify(hookRepository, new Times(2)).getHooksByEventType(eq(deviceRequest.getUserId()), any(Pageable.class), eq(EventType.PING.name()));
        verify(queuingServiceClient, new Times(30)).postDeviceEventHook(eq(deviceEvent), any(Hook.class));
    }

    @Test
    public void postDeviceEnrollment_whenHooksOfDifferentTypes_shouldCallQueuingServiceClient() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String unitId = UUID.randomUUID().toString();

        final Pageable pageable = new PageRequest(0, 10);
        final DeviceRequest deviceRequest = DeviceRequestUtils.getDeviceRequest().toBuilder()
                .build();

        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent().toBuilder()
                .userId(userId)
                .unitId(unitId)
                .request(deviceRequest.toBuilder().userId(userId).unitId(unitId).build())
                .build();
        final Hook hook2 = WebhookUtils.getWebhook(EventType.ENROLLMENT);
        final Hook hook3 = WebhookUtils.getWebhook(EventType.ENROLLMENT);
        final List<Hook> hookList = Arrays.asList(hook2, hook3);
        final Page hookPage = new PageImpl(hookList, pageable, 2L);

        doReturn(hookPage).when(hookRepository).getHooksByEventType(eq(userId), any(Pageable.class), eq(EventType.ENROLLMENT.name()));

        // When
        deviceEventDispatcherManager.postDeviceEnrollment(deviceEvent);

        // When / Then
        verify(hookRepository).getHooksByEventType(eq(userId), any(Pageable.class), eq(EventType.ENROLLMENT.name()));
        verify(queuingServiceClient, new Times(2)).postDeviceEventHook(eq(deviceEvent), any(Hook.class));
    }

    @Test
    public void postDeviceDataChangeEvent_whenHooksOfDifferentTypes_shouldCallQueuingServiceClient() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String unitId = UUID.randomUUID().toString();

        final Pageable pageable = new PageRequest(0, 10);

        final DeviceRequest oldRequest = DeviceRequestUtils.getDeviceRequest();
        final DeviceRequest deviceRequest = DeviceRequestUtils.getDeviceRequest();

        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent().toBuilder()
                .userId(userId)
                .unitId(unitId)
                .request(deviceRequest.toBuilder().userId(userId).unitId(unitId).build())
                .build();

        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent()
                .toBuilder()
                .oldRequest(oldRequest)
                .deviceEvent(deviceEvent)
                .build();

        final Hook hook2 = WebhookUtils.getWebhook(EventType.DEVICE_DATA_CHANGE);
        final Hook hook3 = WebhookUtils.getWebhook(EventType.DEVICE_DATA_CHANGE);
        final List<Hook> hookList = Arrays.asList(hook2, hook3);
        final Page hookPage = new PageImpl(hookList, pageable, 2L);

        doReturn(hookPage).when(hookRepository).getHooksByEventType(eq(userId), any(Pageable.class), eq(EventType.DEVICE_DATA_CHANGE.name()));

        // When
        deviceEventDispatcherManager.postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_DATA_CHANGE);

        // When / Then
        verify(hookRepository).getHooksByEventType(eq(userId), any(Pageable.class), eq(EventType.DEVICE_DATA_CHANGE.name()));
        verify(queuingServiceClient, new Times(2)).postDeviceChangeEventHook(eq(deviceChangeEvent), any(Hook.class));
    }

    @Test
    public void postDevicePackageChangeEvent_whenHooksOfDifferentTypes_shouldCallQueuingServiceClient() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String unitId = UUID.randomUUID().toString();

        final Pageable pageable = new PageRequest(0, 10);

        final DeviceRequest oldRequest = DeviceRequestUtils.getDeviceRequest();
        final DeviceRequest deviceRequest = DeviceRequestUtils.getDeviceRequest();

        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent().toBuilder()
                .userId(userId)
                .unitId(unitId)
                .request(deviceRequest.toBuilder().userId(userId).unitId(unitId).build())
                .build();

        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent()
                .toBuilder()
                .oldRequest(oldRequest)
                .deviceEvent(deviceEvent)
                .build();

        final Hook hook2 = WebhookUtils.getWebhook(EventType.DEVICE_PACKAGE_CHANGE);
        final Hook hook3 = WebhookUtils.getWebhook(EventType.DEVICE_PACKAGE_CHANGE);
        final List<Hook> hookList = Arrays.asList(hook2, hook3);
        final Page hookPage = new PageImpl(hookList, pageable, 2L);

        doReturn(hookPage).when(hookRepository).getHooksByEventType(eq(userId), any(Pageable.class), eq(EventType.DEVICE_PACKAGE_CHANGE.name()));

        // When
        deviceEventDispatcherManager.postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_PACKAGE_CHANGE);

        // When / Then
        verify(hookRepository).getHooksByEventType(eq(userId), any(Pageable.class), eq(EventType.DEVICE_PACKAGE_CHANGE.name()));
        verify(queuingServiceClient, new Times(2)).postDeviceChangeEventHook(eq(deviceChangeEvent), any(Hook.class));
    }

    @Test
    public void postDevicePackageChangeEvent_whenManyHooks_shouldSendToHooks() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String unitId = UUID.randomUUID().toString();

        final Pageable pageable = new PageRequest(0, 10);

        final DeviceRequest oldRequest = DeviceRequestUtils.getDeviceRequest();
        final DeviceRequest deviceRequest = DeviceRequestUtils.getDeviceRequest();

        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent().toBuilder()
                .userId(userId)
                .unitId(unitId)
                .request(deviceRequest.toBuilder().userId(userId).unitId(unitId).build())
                .build();

        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent()
                .toBuilder()
                .oldRequest(oldRequest)
                .deviceEvent(deviceEvent)
                .build();

        final List<Hook> hookList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            hookList.add(WebhookUtils.getWebhook(deviceRequest.getUserId()));
        }

        final Page hookPage = new PageImpl(hookList, pageable, 15L);

        doReturn(hookPage).when(hookRepository).getHooksByEventType(eq(deviceEvent.getRequest().getUserId()), any(Pageable.class), eq(EventType.DEVICE_PACKAGE_CHANGE.name()));

        // When
        deviceEventDispatcherManager.postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_PACKAGE_CHANGE);

        // When / Then
        verify(hookRepository, new Times(2)).getHooksByEventType(eq(deviceEvent.getRequest().getUserId()), any(Pageable.class), eq(EventType.DEVICE_PACKAGE_CHANGE.name()));
        verify(queuingServiceClient, new Times(30)).postDeviceChangeEventHook(eq(deviceChangeEvent), any(Hook.class));
    }

    @Test
    public void saveHook_whenAllIsFine_shouldReturnHook() {
        //Given
        final Hook hook = WebhookUtils.getWebhook();
        doReturn(hook).when(hookRepository).saveHook(hook);

        //When
        final Hook result = deviceEventDispatcherManager.saveHook(hook);

        //Then
        verify(hookRepository).saveHook(hook);
        assertThat(result).isEqualTo(hook);

    }

    @Test
    public void getHook_whenAllIsFine_shouldReturnHook() {
        //Given
        final Hook hook = WebhookUtils.getWebhook();
        final String userId = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        doReturn(Optional.of(hook)).when(hookRepository).getHookByUserIdAndName(userId, name);

        //When
        final Hook result = deviceEventDispatcherManager.getHook(userId, name);

        //Then
        verify(hookRepository).getHookByUserIdAndName(userId, name);
        assertThat(result).isEqualTo(hook);
    }


    @Test
    public void getHook_whenNoHook_shouldThrowException() {
        //Given
        final String userId = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        doReturn(Optional.empty()).when(hookRepository).getHookByUserIdAndName(userId, name);

        // When /Then
        assertThatExceptionOfType(HookNotFoundException.class)
                .isThrownBy(() -> deviceEventDispatcherManager.getHook(userId, name));
    }

    @Test
    public void getHooks_whenAllIsFine_shouldReturnHooks() {
        //Given
        final Pageable pageable = new PageRequest(0, 10);
        final String userId = UUID.randomUUID().toString();
        final Hook hook1 = WebhookUtils.getWebhook(userId);
        final Hook hook2 = WebhookUtils.getWebhook(userId);
        final List<Hook> expectedList = Arrays.asList(hook1, hook2);
        final Page<Hook> expected = new PageImpl(expectedList, pageable, 2L);
        doReturn(expected).when(hookRepository).getHooks(userId, pageable);

        //When
        final Page<Hook> result = deviceEventDispatcherManager.getHooks(userId, pageable);

        //Then
        verify(hookRepository).getHooks(userId, pageable);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void deleteHook_whenAllIsFine_shouldCallRepository() {
        //Given
        final String name = UUID.randomUUID().toString();
        final String userId = UUID.randomUUID().toString();
        doNothing().when(hookRepository).deleteHook(userId, name);

        //When
        deviceEventDispatcherManager.deleteHook(userId, name);

        //Then
        verify(hookRepository).deleteHook(userId, name);
    }

    @Test
    public void updateHook_whenAllIsFine_shouldReturnHook() {
        //Given
        final Hook oldHook = WebhookUtils.getWebhook();
        final Hook newHook = WebhookUtils.getWebhook();
        final Webhook expected = ((Webhook) newHook).toBuilder().id(oldHook.getId()).build();
        final String userId = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        doReturn(Optional.of(oldHook)).when(hookRepository).getHookByUserIdAndName(userId, name);
        doReturn(expected).when(hookRepository).updateHook(eq(name), refEq(expected));

        //When
        final Hook result = deviceEventDispatcherManager.updateHook(userId, name, newHook);

        //Then
        verify(hookRepository).getHookByUserIdAndName(userId, name);
        verify(hookRepository).updateHook(eq(name), refEq(expected));
        assertThat(result).isEqualTo(expected);
    }

}
