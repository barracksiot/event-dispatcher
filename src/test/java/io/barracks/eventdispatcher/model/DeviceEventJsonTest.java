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

package io.barracks.eventdispatcher.model;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.barracks.eventdispatcher.utils.DeviceEventUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class DeviceEventJsonTest {

    @Autowired
    private JacksonTester<DeviceEvent> json;

    @Value("classpath:io/barracks/eventdispatcher/model/deviceEvent.json")
    private Resource deviceEventResource;

    @Test
    public void serialize_whenWebhook_shouldFillAllFieldsExceptIdAndExchangeAndEventType() throws Exception {
        // Given
        final DeviceEvent source = DeviceEventUtils.getDeviceEvent();

        // When
        final JsonContent<DeviceEvent> result = json.write(source);

        // Then
        assertThat(result).extractingJsonPathStringValue("request.userId").isEqualTo(source.getRequest().getUserId());
        assertThat(result).extractingJsonPathStringValue("request.unitId").isEqualTo(source.getRequest().getUnitId());
        assertThat(result).extractingJsonPathStringValue("request.userAgent").isEqualTo(source.getRequest().getUserAgent());
        assertThat(result).extractingJsonPathStringValue("request.ipAddress").isEqualTo(source.getRequest().getIpAddress());
        assertThat(result).extractingJsonPathStringValue("response.available[0].reference").isEqualTo(source.getResponse().getAvailable().get(0).getReference());
        assertThat(result).extractingJsonPathStringValue("response.available[0].version").isEqualTo(source.getResponse().getAvailable().get(0).getVersion());
        assertThat(result).extractingJsonPathStringValue("response.available[0].url").isEqualTo(source.getResponse().getAvailable().get(0).getUrl());
        assertThat(result).extractingJsonPathStringValue("response.available[0].md5").isEqualTo(source.getResponse().getAvailable().get(0).getMd5());
        assertThat(result).extractingJsonPathNumberValue("response.available[0].size").isEqualTo(source.getResponse().getAvailable().get(0).getSize());
        assertThat(result).extractingJsonPathStringValue("response.unchanged[0].reference").isEqualTo(source.getResponse().getUnchanged().get(0).getReference());
        assertThat(result).extractingJsonPathStringValue("response.unavailable[0].reference").isEqualTo(source.getResponse().getUnavailable().get(0).getReference());
        assertThat(result).extractingJsonPathStringValue("response.changed[0].reference").isEqualTo(source.getResponse().getChanged().get(0).getReference());
    }

    @Test
    public void deserialize_shouldWebhook_shouldFillAllFieldsExceptExchangeAndEventType() throws Exception {
        // Given
        final DeviceRequest request = DeviceRequest.builder()
                .unitId("ID transmitted by the device")
                .userId("Unique ID for the user")
                .userAgent("Version of the SDK installed on the device that sent the information")
                .ipAddress("IP address of the device")
                .customClientData(JsonNodeFactory.instance.objectNode().put("what", "this"))
                .build();

        final Version version = Version.builder()
                .reference("io.barracks.package")
                .filename("barracks-package-2-5-1.tar.gz")
                .md5("4c2383f5c88e9110642953b5dd7c88a1")
                .size(76544567L)
                .version("2-5-1")
                .build();

        final ResolvedVersions response = ResolvedVersions.builder()
                .addAvailable(version)
                .addAvailable(version)
                .addUnavailable(version)
                .addChanged(version)
                .addChanged(version)
                .addUnchanged(version)
                .build();

        final DeviceEvent expected = DeviceEventUtils.getDeviceEvent()
                .toBuilder()
                .userId("Unique ID for the user")
                .unitId("ID transmitted by the device")
                .request(request)
                .response(response)
                .build();

        // When
        final ObjectContent<DeviceEvent> result = json.read(deviceEventResource);

        // Then
        assertThat(result).isEqualTo(expected);
    }
}
