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
import io.barracks.eventdispatcher.model.GoogleAnalyticsHook;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleAnalyticsHookUtils {

    public static GoogleAnalyticsHook getGoogleAnalyticsHook() {
        final GoogleAnalyticsHook googleAnalyticsHook = GoogleAnalyticsHook.builder()
                .id(UUID.randomUUID().toString())
                .eventType(EventType.PING)
                .name(UUID.randomUUID().toString())
                .gaTrackingId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .build();
        assertThat(googleAnalyticsHook).hasNoNullFieldsOrProperties();
        return googleAnalyticsHook;
    }

    public static GoogleAnalyticsHook getGoogleAnalyticsHook(String userId) {
        final GoogleAnalyticsHook googleAnalyticsHook = GoogleAnalyticsHook.builder()
                .id(UUID.randomUUID().toString())
                .eventType(EventType.PING)
                .name(UUID.randomUUID().toString())
                .gaTrackingId(UUID.randomUUID().toString())
                .userId(userId)
                .build();
        assertThat(googleAnalyticsHook).hasNoNullFieldsOrProperties();
        return googleAnalyticsHook;
    }

}