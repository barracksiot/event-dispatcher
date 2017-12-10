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

package io.barracks.eventdispatcher.client;


import io.barracks.eventdispatcher.client.entity.DeviceChangeEventHook;
import io.barracks.eventdispatcher.model.DeviceChangeEvent;
import io.barracks.eventdispatcher.model.DeviceEvent;
import io.barracks.eventdispatcher.client.entity.DeviceEventHook;
import io.barracks.eventdispatcher.model.Hook;
import io.barracks.eventdispatcher.exception.InvalidHookException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QueuingServiceClient {

    private static final String WEBHOOK_CLASS = "Webhook";
    private static final String GOOGLE_ANALYTICS_CLASS = "GoogleAnalyticsHook";
    private static final String BIGQUERY_CLASS = "BigQueryHook";


    private final RabbitTemplate rabbitTemplate;

    private final CounterService counter;
    private final String deviceEventRoutingKey;
    private final String deviceChangeEventoutingKey;
    private String webhookExchange;
    private String googleAnalyticsExchange;
    private String bigQueryExchange;

    @Autowired
    public QueuingServiceClient(
            RabbitTemplate rabbitTemplate,
            CounterService counter,
            @Value("${io.barracks.web.exchangename}") String webhookExchange,
            @Value("${io.barracks.googleanalytics.exchangename}") String googleAnalyticsExchange,
            @Value("${io.barracks.bigquery.exchangename}") String bigQueryExchange,
            @Value("${io.barracks.deviceevent.routingkey}") String deviceEventRoutingKey,
            @Value("${io.barracks.devicechangeevent.routingkey}") String deviceChangeEventoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.counter = counter;
        this.webhookExchange = webhookExchange;
        this.googleAnalyticsExchange = googleAnalyticsExchange;
        this.bigQueryExchange = bigQueryExchange;
        this.deviceEventRoutingKey = deviceEventRoutingKey;
        this.deviceChangeEventoutingKey = deviceChangeEventoutingKey;
    }

    public void postDeviceEventHook(DeviceEvent deviceEvent, Hook hook) {
        try {
            final DeviceEventHook deviceEventHook = DeviceEventHook.builder()
                    .deviceEvent(deviceEvent)
                    .hook(hook)
                    .build();
            final String exchangeName = getExchangeName(hook);
            rabbitTemplate.convertAndSend(exchangeName, deviceEventRoutingKey, deviceEventHook);
            incrementRabbitMQMetric("success");
        } catch (Exception e) {
            log.error("The message cannot be sent to RabbitMQ. It is possible that the broker is not running. Exception : " + e);
            incrementRabbitMQMetric("error");
        }
    }

    public void postDeviceChangeEventHook(DeviceChangeEvent deviceChangeEvent, Hook hook) {
        try {
            final DeviceChangeEventHook deviceChangeEventHook = DeviceChangeEventHook.builder()
                    .deviceChangeEvent(deviceChangeEvent)
                    .hook(hook)
                    .build();
            final String exchangeName = getExchangeName(hook);
            rabbitTemplate.convertAndSend(exchangeName, deviceChangeEventoutingKey, deviceChangeEventHook);
            incrementRabbitMQMetric("success");
        } catch (Exception e) {
            log.error("The message cannot be sent to RabbitMQ. It is possible that the broker is not running. Exception : " + e);
            incrementRabbitMQMetric("error");
        }
    }

    String getExchangeName(Hook hook) {
        switch (hook.getClass().getSimpleName()) {
            case WEBHOOK_CLASS:
                return webhookExchange;
            case GOOGLE_ANALYTICS_CLASS:
                return googleAnalyticsExchange;
            case BIGQUERY_CLASS:
                return bigQueryExchange;
            default:
                throw new InvalidHookException(hook.getUserId(), hook.getName());
        }
    }

    private void incrementRabbitMQMetric(String status) {
        counter.increment("message.process." + status);
    }

}
