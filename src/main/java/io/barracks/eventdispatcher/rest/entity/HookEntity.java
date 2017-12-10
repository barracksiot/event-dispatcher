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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.barracks.eventdispatcher.model.*;
import io.barracks.eventdispatcher.exception.InvalidHookException;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.core.Relation;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
@AllArgsConstructor
@Relation(collectionRelation = "hooks")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class HookEntity {

    private static final String WEB_TYPE = "web";
    private static final String GOOGLE_ANALYTICS_TYPE = "google_analytics";
    private static final String BIGQUERY_TYPE = "bigquery";

    private String type;

    private EventType eventType;

    private String userId;

    private String name;

    private String url;

    private String gaTrackingId;

    private GoogleClientSecret googleClientSecret;

    public static HookEntity fromHook(Hook hook) {
        final HookEntity result = HookEntity.builder()
                .eventType(hook.getEventType())
                .userId(hook.getUserId())
                .name(hook.getName())
                .build();

        if (hook.getClass().equals(Webhook.class)) {
            return result.toBuilder()
                    .type(WEB_TYPE)
                    .url(((Webhook) hook).getUrl())
                    .build();
        } else if (hook.getClass().equals(GoogleAnalyticsHook.class)) {
            return result.toBuilder()
                    .type(GOOGLE_ANALYTICS_TYPE)
                    .gaTrackingId(((GoogleAnalyticsHook) hook).getGaTrackingId())
                    .build();
        } else if (hook.getClass().equals(BigQueryHook.class)) {
            return result.toBuilder()
                    .type(BIGQUERY_TYPE)
                    .googleClientSecret(((BigQueryHook) hook).getGoogleClientSecret().getHiddenClientSecret())
                    .build();
        } else {
            throw new InvalidHookException(hook.getUserId(), hook.getName());
        }
    }

    public Hook toHook() {

        switch (this.getType()) {
            case WEB_TYPE:
                return Webhook.builder()
                        .userId(this.getUserId())
                        .name(this.getName())
                        .eventType(this.getEventType())
                        .url(this.getUrl())
                        .build();
            case GOOGLE_ANALYTICS_TYPE:
                return GoogleAnalyticsHook.builder()
                        .userId(this.getUserId())
                        .name(this.getName())
                        .eventType(this.getEventType())
                        .gaTrackingId(this.getGaTrackingId())
                        .build();
            case BIGQUERY_TYPE:
                return BigQueryHook.builder()
                        .userId(this.getUserId())
                        .name(this.getName())
                        .eventType(this.getEventType())
                        .googleClientSecret(this.getGoogleClientSecret())
                        .build();
            default:
                throw new InvalidHookException(this.getUserId(), this.getName());
        }
    }

    public static Page<HookEntity> fromHookPage(Page<Hook> page) {
        final List<Hook> hookList = page.getContent();
        final List<HookEntity> hookEntityList = hookList.stream()
                .map(HookEntity::fromHook)
                .collect(Collectors.toList());

        final Pageable pageable = new PageRequest(page.getNumber(), page.getSize());

        return new PageImpl<>(hookEntityList, pageable, page.getTotalElements());
    }

}
