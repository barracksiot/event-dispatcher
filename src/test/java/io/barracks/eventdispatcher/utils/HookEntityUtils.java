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

package io.barracks.eventdispatcher.utils;

import io.barracks.eventdispatcher.model.EventType;
import io.barracks.eventdispatcher.rest.entity.HookEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class HookEntityUtils {

    public static HookEntity getWebHookEntity() {
        final HookEntity hookEntity = HookEntity.builder()
                .type("web")
                .name(UUID.randomUUID().toString())
                .eventType(EventType.PING)
                .url(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .build();
        return hookEntity;
    }

    public static HookEntity getGoogleAnalyticsHookEntity() {
        final HookEntity hookEntity = HookEntity.builder()
                .type("google_analytics")
                .name(UUID.randomUUID().toString())
                .eventType(EventType.PING)
                .gaTrackingId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .build();
        return hookEntity;
    }

    public static HookEntity getBigqueryHookEntity() {
        final HookEntity hookEntity = HookEntity.builder()
                .type("bigquery")
                .name(UUID.randomUUID().toString())
                .eventType(EventType.PING)
                .googleClientSecret(GoogleClientSecretUtils.getGoogleClientSecret())
                .userId(UUID.randomUUID().toString())
                .build();
        return hookEntity;
    }


}
