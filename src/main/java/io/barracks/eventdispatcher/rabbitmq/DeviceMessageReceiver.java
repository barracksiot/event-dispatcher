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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeviceMessageReceiver {

    private final DeviceEventDispatcherManager deviceEventDispatcherManager;
    private final CounterService counter;
    private final ObjectMapper objectMapper;

    @Autowired
    public DeviceMessageReceiver(DeviceEventDispatcherManager deviceEventDispatcherManager,
                                 ObjectMapper objectMapper,
                                 CounterService counter
    ) {
        this.deviceEventDispatcherManager = deviceEventDispatcherManager;
        this.objectMapper = objectMapper;
        this.counter = counter;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${io.barracks.ping.queuename}", durable = "true", autoDelete = "false"),
                    exchange = @Exchange(value = "${io.barracks.ping.exchangename}", type = "fanout", durable = "true"),
                    key = "${io.barracks.amqp.routingkey}"
            )
    )
    public void receiveDeviceEventMessage(@Payload DeviceEvent deviceEvent) {
        try {
            deviceEventDispatcherManager.postDeviceEvent(deviceEvent);
            incrementRabbitMQMetric("event.success");
        } catch (Exception e) {
            log.error("Error while sending event data", e);
            incrementRabbitMQMetric("event.error");
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${io.barracks.enrollment.queuename}", durable = "true", autoDelete = "false"),
                    exchange = @Exchange(value = "${io.barracks.enrollment.exchangename}", type = "topic", durable = "true"),
                    key = "${io.barracks.enrollment.routingkey}"
            )
    )
    public void receiveEnrollmentMessage(@Payload DeviceEvent deviceEvent) {
        try {
            deviceEventDispatcherManager.postDeviceEnrollment(deviceEvent);
            incrementRabbitMQMetric("enrollment.success");
        } catch (Exception e) {
            log.error("Error while sending enrollment data", e);
            incrementRabbitMQMetric("enrollment.error");
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${io.barracks.devicedata.queuename}", durable = "true", autoDelete = "false"),
                    exchange = @Exchange(value = "${io.barracks.devicedata.exchangename}", type = "topic", durable = "true"),
                    key = "${io.barracks.devicedata.routingkey}"
            )
    )
    public void receiveDeviceDataMessage(@Payload DeviceChangeEvent deviceChangeEvent) {
        try {
            deviceEventDispatcherManager.postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_DATA_CHANGE);
            incrementRabbitMQMetric("device.data.success");
        } catch (Exception e) {
            log.error("Error while sending enrollment data", e);
            incrementRabbitMQMetric("device.data.error");
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${io.barracks.devicepackage.queuename}", durable = "true", autoDelete = "false"),
                    exchange = @Exchange(value = "${io.barracks.devicepackage.exchangename}", type = "topic", durable = "true"),
                    key = "${io.barracks.devicepackage.routingkey}"
            )
    )
    public void receiveDevicePackageMessage(@Payload DeviceChangeEvent deviceChangeEvent) {
        try {
            deviceEventDispatcherManager.postDeviceChangeEvent(deviceChangeEvent, EventType.DEVICE_PACKAGE_CHANGE);
            incrementRabbitMQMetric("device.package.success");
        } catch (Exception e) {
            log.error("Error while sending enrollment data", e);
            incrementRabbitMQMetric("device.package.error");
        }
    }

    private void incrementRabbitMQMetric(String status) {
        counter.increment("message.process." + status);
    }

}
