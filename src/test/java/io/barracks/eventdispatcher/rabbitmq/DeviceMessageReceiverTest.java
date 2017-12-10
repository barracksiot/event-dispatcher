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

package io.barracks.eventdispatcher.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.barracks.eventdispatcher.manager.DeviceEventDispatcherManager;
import io.barracks.eventdispatcher.model.DeviceChangeEvent;
import io.barracks.eventdispatcher.model.DeviceEvent;
import io.barracks.eventdispatcher.model.EventType;
import io.barracks.eventdispatcher.utils.DeviceChangeEventUtils;
import io.barracks.eventdispatcher.utils.DeviceEventUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.metrics.CounterService;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageReceiverTest {

    private DeviceMessageReceiver deviceMessageReceiver;

    @Mock
    private DeviceEventDispatcherManager deviceEventDispatcherManager;

    @Before
    public void setUp() {
        deviceMessageReceiver = new DeviceMessageReceiver(deviceEventDispatcherManager, new ObjectMapper(), mock(CounterService.class));
    }

    @Test
    public void receiveEventMessage_whenAllIsFine_shouldCallManager() throws Exception {
        //Given
        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent();

        //When
        deviceMessageReceiver.receiveDeviceEventMessage(deviceEvent);

        //Then
        verify(deviceEventDispatcherManager).postDeviceEvent(deviceEvent);
    }

    @Test
    public void receiveEventMessage_whenException_shouldLogError() throws Exception {
        //Given
        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent();
        doThrow(Exception.class).when(deviceEventDispatcherManager).postDeviceEvent(deviceEvent);

        //When
        deviceMessageReceiver.receiveDeviceEventMessage(deviceEvent);

        //Then
        verify(deviceEventDispatcherManager).postDeviceEvent(deviceEvent);
    }

    @Test
    public void receiveEnrollmentMessage_whenAllIsFine_shouldCallManager() throws Exception {
        //Given
        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent();

        //When
        deviceMessageReceiver.receiveEnrollmentMessage(deviceEvent);

        //Then
        verify(deviceEventDispatcherManager).postDeviceEnrollment(deviceEvent);
    }

    @Test
    public void receiveEnrollmentMessage_whenException_shouldLogError() throws Exception {
        //Given
        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent();
        doThrow(Exception.class).when(deviceEventDispatcherManager).postDeviceEnrollment(deviceEvent);

        //When
        deviceMessageReceiver.receiveEnrollmentMessage(deviceEvent);

        //Then
        verify(deviceEventDispatcherManager).postDeviceEnrollment(deviceEvent);
    }

    @Test
    public void receiveDeviceDataMessage_whenAllIsFine_shouldCallManager() throws Exception {
        //Given
        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent();

        //When
        deviceMessageReceiver.receiveDeviceDataMessage(deviceChangeEvent);

        //Then
        verify(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_DATA_CHANGE);
    }

    @Test
    public void receiveDeviceDataMessage_whenException_shouldLogError() throws Exception {
        //Given
        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent();
        doThrow(Exception.class).when(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_DATA_CHANGE);

        //When
        deviceMessageReceiver.receiveDeviceDataMessage(deviceChangeEvent);

        //Then
        verify(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_DATA_CHANGE);
    }

    @Test
    public void receiveDevicePackageMessage_whenAllIsFine_shouldCallManager() throws Exception {
        //Given
        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent();

        //When
        deviceMessageReceiver.receiveDevicePackageMessage(deviceChangeEvent);

        //Then
        verify(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_PACKAGE_CHANGE);
    }

    @Test
    public void receiveDevicePackageMessage_whenException_shouldLogError() throws Exception {
        //Given
        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent();
        doThrow(Exception.class).when(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_PACKAGE_CHANGE);

        //When
        deviceMessageReceiver.receiveDevicePackageMessage(deviceChangeEvent);

        //Then
        verify(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_PACKAGE_CHANGE);
    }

}
