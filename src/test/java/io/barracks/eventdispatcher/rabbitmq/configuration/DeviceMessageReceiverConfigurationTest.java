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

package io.barracks.eventdispatcher.rabbitmq.configuration;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq;
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.barracks.eventdispatcher.Application;
import io.barracks.eventdispatcher.config.RabbitMQConfig;
import io.barracks.eventdispatcher.manager.DeviceEventDispatcherManager;
import io.barracks.eventdispatcher.model.DeviceChangeEvent;
import io.barracks.eventdispatcher.model.DeviceEvent;
import io.barracks.eventdispatcher.model.EventType;
import io.barracks.eventdispatcher.rabbitmq.DeviceMessageReceiver;
import io.barracks.eventdispatcher.utils.DeviceChangeEventUtils;
import io.barracks.eventdispatcher.utils.DeviceEventUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.junit.BrokerRunning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RabbitMQConfig.class, Application.class})
public class DeviceMessageReceiverConfigurationTest {

    private static EmbeddedRabbitMq rabbitMq;

    @SpyBean
    private DeviceMessageReceiver receiver;

    @Value("${io.barracks.ping.exchangename}")
    private String eventExchangeName;

    @Value("${io.barracks.enrollment.exchangename}")
    private String enrollmentExchangeName;

    @Value("${io.barracks.devicedata.exchangename}")
    private String deviceDataExchangeName;

    @Value("${io.barracks.devicepackage.exchangename}")
    private String devicePackageExchangeName;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private DeviceEventDispatcherManager deviceEventDispatcherManager;

    @BeforeClass
    public static void startBroker() throws ExecutionException, InterruptedException {
        EmbeddedRabbitMqConfig config = new EmbeddedRabbitMqConfig.Builder()
                .erlangCheckTimeoutInMillis(150000)
                .rabbitMqServerInitializationTimeoutInMillis(100000)
                .defaultRabbitMqCtlTimeoutInMillis(100000)
                .downloadConnectionTimeoutInMillis(150000)
                .downloadReadTimeoutInMillis(150000)
                .build();
        rabbitMq = new EmbeddedRabbitMq(config);
        rabbitMq.start();
    }

    @AfterClass
    public static void clear() throws Exception {
        rabbitMq.stop();
    }

    @Before
    public void isBrokerRunning() {
        BrokerRunning.isRunning();
    }

    @Test
    public void receiveEventMessage_whenAllIsFine_shouldCallManager() throws Exception {
        //Given
        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent();

        //When
        this.rabbitTemplate.convertSendAndReceive(eventExchangeName, "test.v2.afsdsf", deviceEvent);

        //Then
        verify(receiver).receiveDeviceEventMessage(deviceEvent);
        verify(deviceEventDispatcherManager).postDeviceEvent(deviceEvent);
    }

    @Test
    public void receiveEventMessage_whenException_shouldLogError() throws Exception {
        //Given
        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent();
        doThrow(Exception.class).when(deviceEventDispatcherManager).postDeviceEvent(any());

        //When
        this.rabbitTemplate.convertSendAndReceive(eventExchangeName, "test.v2.afsdsf", deviceEvent);

        //Then
        verify(receiver).receiveDeviceEventMessage(deviceEvent);
        verify(deviceEventDispatcherManager).postDeviceEvent(deviceEvent);
    }

    @Test
    public void receiveEnrollmentMessage_whenAllIsFine_shouldCallManager() throws Exception {
        //Given
        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent();

        //When
        this.rabbitTemplate.convertSendAndReceive(enrollmentExchangeName, "devices.enrollment.coucou", deviceEvent);

        //Then
        verify(receiver).receiveEnrollmentMessage(deviceEvent);
        verify(deviceEventDispatcherManager).postDeviceEnrollment(deviceEvent);
    }

    @Test
    public void receiveEnrollmentMessage_whenException_shouldLogError() throws Exception {
        //Given
        final DeviceEvent deviceEvent = DeviceEventUtils.getDeviceEvent();
        doThrow(Exception.class).when(deviceEventDispatcherManager).postDeviceEnrollment(any());

        //When
        this.rabbitTemplate.convertSendAndReceive(enrollmentExchangeName, "devices.enrollment.salut", deviceEvent);

        //Then
        verify(receiver).receiveEnrollmentMessage(deviceEvent);
        verify(deviceEventDispatcherManager).postDeviceEnrollment(deviceEvent);
    }

    @Test
    public void receiveDeviceDataMessage_whenAllIsFine_shouldCallManager() throws Exception {
        //Given
        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent();

        //When
        this.rabbitTemplate.convertSendAndReceive(deviceDataExchangeName, "devices.data.coucou", deviceChangeEvent);

        //Then
        verify(receiver).receiveDeviceDataMessage(deviceChangeEvent);
        verify(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_DATA_CHANGE);
    }

    @Test
    public void receiveDeviceDataMessage_whenException_shouldLogError() throws Exception {
        //Given
        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent();
        doThrow(Exception.class).when(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_DATA_CHANGE);

        //When
        this.rabbitTemplate.convertSendAndReceive(deviceDataExchangeName, "devices.data.salut", deviceChangeEvent);

        //Then
        verify(receiver).receiveDeviceDataMessage(deviceChangeEvent);
        verify(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_DATA_CHANGE);
    }

    @Test
    public void receiveDevicePackageMessage_whenAllIsFine_shouldCallManager() throws Exception {
        //Given
        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent();

        //When
        this.rabbitTemplate.convertSendAndReceive(devicePackageExchangeName, "devices.package.coucou", deviceChangeEvent);

        //Then
        verify(receiver).receiveDevicePackageMessage(deviceChangeEvent);
        verify(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_PACKAGE_CHANGE);
    }

    @Test
    public void receiveDevicePackageMessage_whenException_shouldLogError() throws Exception {
        //Given
        final DeviceChangeEvent deviceChangeEvent = DeviceChangeEventUtils.getDeviceChangeEvent();
        doThrow(Exception.class).when(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_PACKAGE_CHANGE);

        //When
        this.rabbitTemplate.convertSendAndReceive(devicePackageExchangeName, "devices.package.salut", deviceChangeEvent);

        //Then
        verify(receiver).receiveDevicePackageMessage(deviceChangeEvent);
        verify(deviceEventDispatcherManager).postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_PACKAGE_CHANGE);
    }

}
