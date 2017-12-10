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


import io.barracks.eventdispatcher.model.BigQueryHook;
import io.barracks.eventdispatcher.model.GoogleAnalyticsHook;
import io.barracks.eventdispatcher.model.Webhook;
import io.barracks.eventdispatcher.utils.BigQueryHookUtils;
import io.barracks.eventdispatcher.utils.GoogleAnalyticsHookUtils;
import io.barracks.eventdispatcher.utils.WebhookUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit4.SpringRunner;

import static io.barracks.eventdispatcher.rest.entity.HookEntity.fromHook;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class HookEntityJsonTest {

    @Autowired
    private JacksonTester<HookEntity> json;

    @Test
    public void serialize_whenWebhok_shouldFillAllFieldsExceptId() throws Exception {
        // Given
        final Webhook source = WebhookUtils.getWebhook();
        final HookEntity hookEntity = fromHook(source);

        // When
        final JsonContent<HookEntity> result = json.write(hookEntity);

        // Then
        System.out.print(result.getJson());
        assertThat(result).extractingJsonPathStringValue("eventType").isEqualTo(source.getEventType().name());
        assertThat(result).extractingJsonPathStringValue("id").isNull();
        assertThat(result).extractingJsonPathStringValue("userId").isEqualTo(source.getUserId());
        assertThat(result).extractingJsonPathStringValue("name").isEqualTo(source.getName());
        assertThat(result).extractingJsonPathStringValue("url").isEqualTo(source.getUrl());
    }

    @Test
    public void serialize_whenGoogleAnalyticsHook_shouldFillAllFieldsExceptId() throws Exception {
        // Given
        final GoogleAnalyticsHook source = GoogleAnalyticsHookUtils.getGoogleAnalyticsHook();
        final HookEntity hookEntity = fromHook(source);

        // When
        final JsonContent<HookEntity> result = json.write(hookEntity);

        // Then
        System.out.print(result.getJson());
        assertThat(result).extractingJsonPathStringValue("eventType").isEqualTo(source.getEventType().name());
        assertThat(result).extractingJsonPathStringValue("id").isNull();
        assertThat(result).extractingJsonPathStringValue("userId").isEqualTo(source.getUserId());
        assertThat(result).extractingJsonPathStringValue("name").isEqualTo(source.getName());
        assertThat(result).extractingJsonPathStringValue("gaTrackingId").isEqualTo(source.getGaTrackingId());
    }

    @Test
    public void serialize_whenBigQueryHook_shouldFillAllFieldsExceptId() throws Exception {
        // Given
        final BigQueryHook source = BigQueryHookUtils.getBigQueryHook();
        final HookEntity hookEntity = fromHook(source);

        // When
        final JsonContent<HookEntity> result = json.write(hookEntity);

        // Then
        System.out.print(result.getJson());
        assertThat(result).extractingJsonPathStringValue("eventType").isEqualTo(source.getEventType().name());
        assertThat(result).extractingJsonPathStringValue("id").isNull();
        assertThat(result).extractingJsonPathStringValue("userId").isEqualTo(source.getUserId());
        assertThat(result).extractingJsonPathStringValue("name").isEqualTo(source.getName());
        assertThat(result).extractingJsonPathStringValue("googleClientSecret.type").isEqualTo(source.getGoogleClientSecret().getType());
        assertThat(result).extractingJsonPathStringValue("googleClientSecret.project_id").isEqualTo(source.getGoogleClientSecret().getProjectId());
        assertThat(result).extractingJsonPathStringValue("googleClientSecret.private_key").isEqualTo("****");
        assertThat(result).extractingJsonPathStringValue("googleClientSecret.private_key_id").isEqualTo("****");
        assertThat(result).extractingJsonPathStringValue("googleClientSecret.client_email").isEqualTo(source.getGoogleClientSecret().getClientEmail());
        assertThat(result).extractingJsonPathStringValue("googleClientSecret.client_id").isEqualTo(source.getGoogleClientSecret().getClientId());
    }

}
