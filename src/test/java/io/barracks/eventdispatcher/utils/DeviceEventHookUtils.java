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


import io.barracks.eventdispatcher.client.entity.DeviceEventHook;

import static io.barracks.eventdispatcher.utils.DeviceEventUtils.getDeviceEvent;
import static io.barracks.eventdispatcher.utils.WebhookUtils.getWebhook;
import static org.assertj.core.api.Assertions.assertThat;

public class DeviceEventHookUtils {

    public static DeviceEventHook getDeviceEventHook() {
        final DeviceEventHook deviceEventHook = DeviceEventHook.builder()
                .deviceEvent(getDeviceEvent())
                .hook(getWebhook())
                .build();
        assertThat(deviceEventHook).hasNoNullFieldsOrProperties();
        return deviceEventHook;
    }

}
