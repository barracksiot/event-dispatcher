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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.barracks.eventdispatcher.client.entity.DeviceChangeEventHook;
import io.barracks.eventdispatcher.client.entity.DeviceEventHook;
import io.barracks.eventdispatcher.model.BigQueryHook;
import io.barracks.eventdispatcher.model.GoogleAnalyticsHook;
import io.barracks.eventdispatcher.model.Hook;
import io.barracks.eventdispatcher.utils.WebhookUtils;
import io.barracks.eventdispatcher.utils.DeviceChangeEventHookUtils;
import io.barracks.eventdispatcher.utils.DeviceEventHookUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.metrics.CounterService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class QueuingServiceClientTest {

    private QueuingServiceClient queuingServiceClient;

    private String webExchangeName = "test_web_exchange";
    private String gaExchangeName = "test_google_analytics_exchange";
    private String bigqueryExchangeName = "test_bigquery_exchange";
    private String deviceEventRoutingKey = "test_routing_key";
    private String deviceEventChangeRoutingKey = "test_change_routing_key";

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private CounterService counterService;

    @Before
    public void setUp() {
        queuingServiceClient = new QueuingServiceClient(
                rabbitTemplate,
                counterService,
                webExchangeName,
                gaExchangeName,
                bigqueryExchangeName,
                deviceEventRoutingKey,
                deviceEventChangeRoutingKey
        );
    }

    @Test
    public void postDeviceEventHook_whenServiceSucceeds_serverShouldBeCalled() throws JsonProcessingException {
        // Given
        final DeviceEventHook deviceEventHook = DeviceEventHookUtils.getDeviceEventHook();

        // When
        queuingServiceClient.postDeviceEventHook(deviceEventHook.getDeviceEvent(), deviceEventHook.getHook());

        // Then
        verify(rabbitTemplate).convertAndSend(webExchangeName, deviceEventRoutingKey, deviceEventHook);
    }

    @Test
    public void postDeviceEventHook_whenServiceFails_shouldLogError() throws JsonProcessingException {
        // Given
        final DeviceEventHook deviceEventHook = DeviceEventHookUtils.getDeviceEventHook();

        doThrow(Exception.class).when(rabbitTemplate).convertAndSend(webExchangeName, deviceEventRoutingKey, deviceEventHook);

        // When
        queuingServiceClient.postDeviceEventHook(deviceEventHook.getDeviceEvent(), deviceEventHook.getHook());

        // Then
        verify(rabbitTemplate).convertAndSend(webExchangeName, deviceEventRoutingKey, deviceEventHook);
    }

    @Test
    public void postDeviceChangeEventHook_whenServiceSucceeds_serverShouldBeCalled() throws JsonProcessingException {
        // Given
        final DeviceChangeEventHook deviceChangeEventHook = DeviceChangeEventHookUtils.getDeviceChangeEventHook();

        // When
        queuingServiceClient.postDeviceChangeEventHook(deviceChangeEventHook.getDeviceChangeEvent(), deviceChangeEventHook.getHook());

        // Then
        verify(rabbitTemplate).convertAndSend(webExchangeName, deviceEventChangeRoutingKey, deviceChangeEventHook);
    }

    @Test
    public void postDeviceChangeEventHook_whenServiceFails_shouldLogError() throws JsonProcessingException {
        // Given
        final DeviceChangeEventHook deviceChangeEventHook = DeviceChangeEventHookUtils.getDeviceChangeEventHook();
        doThrow(Exception.class).when(rabbitTemplate).convertAndSend(webExchangeName, deviceEventChangeRoutingKey, deviceChangeEventHook);

        // When
        queuingServiceClient.postDeviceChangeEventHook(deviceChangeEventHook.getDeviceChangeEvent(), deviceChangeEventHook.getHook());

        // Then
        verify(rabbitTemplate).convertAndSend(webExchangeName, deviceEventChangeRoutingKey, deviceChangeEventHook);
    }

    @Test
    public void getExchange_whenWebhook_shouldReturnString() {
        //Given
        final Hook hook = WebhookUtils.getWebhook();

        //When
        final String result = queuingServiceClient.getExchangeName(hook);

        //Then
        assertThat(result).isEqualTo(webExchangeName);
    }

    @Test
    public void getExchange_whengaHook_shouldReturnString() {
        //Given
        final Hook hook = GoogleAnalyticsHook.builder().build();

        //When
        final String result = queuingServiceClient.getExchangeName(hook);

        //Then
        assertThat(result).isEqualTo(gaExchangeName);
    }

    @Test
    public void getExchange_whenBigqueryHook_shouldReturnString() {
        //Given
        final Hook hook = BigQueryHook.builder().build();

        //When
        final String result = queuingServiceClient.getExchangeName(hook);

        //Then
        assertThat(result).isEqualTo(bigqueryExchangeName);
    }

}
