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

package io.barracks.eventdispatcher.rest.entity;

import io.barracks.eventdispatcher.model.*;
import io.barracks.eventdispatcher.utils.BigQueryHookUtils;
import io.barracks.eventdispatcher.utils.GoogleAnalyticsHookUtils;
import io.barracks.eventdispatcher.utils.HookEntityUtils;
import io.barracks.eventdispatcher.utils.WebhookUtils;
import org.junit.Test;

import static io.barracks.eventdispatcher.rest.entity.HookEntity.fromHook;
import static org.assertj.core.api.Assertions.assertThat;

public class HookEntityTest {

    @Test
    public void toHook_whenWeb_shouldReturnWebhook() {
        //Given
        final HookEntity entity = HookEntityUtils.getWebHookEntity();
        final Webhook excepted = Webhook.builder()
                .userId(entity.getUserId())
                .eventType(EventType.PING)
                .name(entity.getName())
                .url(entity.getUrl())
                .build();

        //When
        final Hook result = entity.toHook();

        //Then
        assertThat(result).isEqualTo(excepted);
    }

    @Test
    public void toHook_whenGoogleAnalytics_shouldReturnGAHook() {
        //Given
        final HookEntity entity = HookEntityUtils.getGoogleAnalyticsHookEntity();
        final GoogleAnalyticsHook excepted = GoogleAnalyticsHook.builder()
                .userId(entity.getUserId())
                .eventType(EventType.PING)
                .name(entity.getName())
                .gaTrackingId(entity.getGaTrackingId())
                .build();

        //When
        final Hook result = entity.toHook();

        //Then
        assertThat(result).isEqualTo(excepted);
    }

    @Test
    public void toHook_whenBigquery_shouldReturnBQHook() {
        //Given
        final HookEntity entity = HookEntityUtils.getBigqueryHookEntity();
        final BigQueryHook excepted = BigQueryHook.builder()
                .userId(entity.getUserId())
                .eventType(EventType.PING)
                .name(entity.getName())
                .googleClientSecret(entity.getGoogleClientSecret())
                .build();

        //When
        final Hook result = entity.toHook();

        //Then
        assertThat(result).isEqualTo(excepted);
    }

    @Test
    public void fromHook_whenWebhook_shouldReturnEntity() {
        //Given
        final Webhook hook = WebhookUtils.getWebhook();
        final HookEntity expected = HookEntity.builder()
                .eventType(EventType.PING)
                .userId(hook.getUserId())
                .name(hook.getName())
                .url(hook.getUrl())
                .type("web")
                .build();

        //When
        final HookEntity result = fromHook(hook);

        //Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void fromHook_whenGoogleAnalyticshook_shouldReturnEntity() {
        //Given
        final GoogleAnalyticsHook hook = GoogleAnalyticsHookUtils.getGoogleAnalyticsHook();
        final HookEntity expected = HookEntity.builder()
                .eventType(EventType.PING)
                .userId(hook.getUserId())
                .name(hook.getName())
                .gaTrackingId(hook.getGaTrackingId())
                .type("google_analytics")
                .build();

        //When
        final HookEntity result = fromHook(hook);

        //Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void fromHook_whenBigqueryhook_shouldReturnEntity() {
        //Given
        final BigQueryHook hook = BigQueryHookUtils.getBigQueryHook();
        final HookEntity expected = HookEntity.builder()
                .eventType(EventType.PING)
                .userId(hook.getUserId())
                .name(hook.getName())
                .googleClientSecret(hook.getGoogleClientSecret().getHiddenClientSecret())
                .type("bigquery")
                .build();

        //When
        final HookEntity result = fromHook(hook);

        //Then
        assertThat(result).isEqualTo(expected);
    }


}
