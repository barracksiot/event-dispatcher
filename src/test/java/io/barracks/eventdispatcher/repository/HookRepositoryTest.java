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

package io.barracks.eventdispatcher.repository;

import io.barracks.eventdispatcher.model.EventType;
import io.barracks.eventdispatcher.model.Hook;
import io.barracks.eventdispatcher.model.Webhook;
import io.barracks.eventdispatcher.repository.exception.HookCreationFailedException;
import io.barracks.eventdispatcher.repository.exception.HookNotFoundException;
import io.barracks.eventdispatcher.repository.exception.HookUpdateFailedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.barracks.eventdispatcher.utils.WebhookUtils.getWebhook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
@BarracksRepositoryTest
public class HookRepositoryTest {

    @Autowired
    private HookRepository hookRepository;

    @Before
    public void setUp() throws Exception {
        hookRepository.deleteAll();
    }

    @Test
    public void saveHook_whenWebhook_shouldReturnWebhook() {
        // Given
        final Webhook webhook = getWebhook();

        // When
        final Hook result = hookRepository.saveHook(webhook);

        // Then
        assertThat(result).isEqualTo(webhook);
        assertThat(result.getClass()).isEqualTo(Webhook.class);
    }

    @Test
    public void saveHook_whenNameAlreadyTakenForUser_shouldThrowException() {
        // Given
        final Webhook webhook = getWebhook();
        hookRepository.insert(webhook);

        // Then When
        assertThatExceptionOfType(HookCreationFailedException.class).isThrownBy(() ->
                hookRepository.saveHook(webhook)
        );
    }

    @Test
    public void getHookByUserIdAndName_whenHook_shouldReturnHook() {
        // Given
        final Hook hook = getWebhook();
        final Hook expected = hookRepository.saveHook(hook);

        // When
        final Optional<Hook> result = hookRepository.getHookByUserIdAndName(hook.getUserId(), hook.getName());

        // Then
        assertThat(result).isPresent().contains(expected);
    }

    @Test
    public void getHookByUserIdAndName_whenNoHook_shouldReturnNull() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // When
        final Optional<Hook> result = hookRepository.getHookByUserIdAndName(userId, name);

        // Then
        assertThat(result).isEqualTo(Optional.empty());
    }

    @Test
    public void deleteHookByUserIdAndName_whenHook_shouldDeleteHook() {
        // Given
        final Hook expected = getWebhook();
        hookRepository.saveHook(expected);

        // When
        hookRepository.deleteHook(expected.getUserId(), expected.getName());

        // Then
        final Optional<Hook> result = hookRepository.getHookByUserIdAndName(expected.getUserId(), expected.getName());
        assertThat(result).isEqualTo(Optional.empty());
    }

    @Test
    public void deleteHookByUserIdAndName_whenNoHook_shouldThrowException() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // When / Then
        assertThatExceptionOfType(HookNotFoundException.class)
                .isThrownBy(() -> hookRepository.deleteHook(userId, name));
    }

    @Test
    public void getHooks_whenHooks_shouldReturnResult() {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final String userId1 = UUID.randomUUID().toString();
        final String userId2 = UUID.randomUUID().toString();

        final Webhook hook1 = getWebhook(userId1);
        final Webhook hook2 = getWebhook(userId1);
        final Webhook hook3 = getWebhook(userId2);
        final Webhook hook4 = getWebhook(userId2);

        final List<Webhook> hookList = Arrays.asList(hook1, hook2);
        hookRepository.insert(hook1);
        hookRepository.insert(hook2);
        hookRepository.insert(hook3);
        hookRepository.insert(hook4);

        // When
        final Page<Hook> result = hookRepository.getHooks(userId1, pageable);

        // Then
        assertThat(result).hasSize(2).isSubsetOf(hookList);
    }

    @Test
    public void getHooks_whenManyHooks_shouldReturnPagedList() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final Pageable pageable = new PageRequest(1, 5);
        final List<Hook> expected = createHooks(userId);

        // When
        final Page<Hook> result = hookRepository.getHooks(userId, pageable);

        // Then
        assertThat(result).hasSize(5).isSubsetOf(expected);
    }

    @Test
    public void getHooksByEventType_whenHooks_shouldReturnResult() {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final String userId1 = UUID.randomUUID().toString();
        final String userId2 = UUID.randomUUID().toString();

        final Webhook hook1 = getWebhook(userId1).toBuilder().eventType(EventType.PING).build();
        final Webhook hook2 = getWebhook(userId1).toBuilder().eventType(EventType.PING).build();
        final Webhook hook3 = getWebhook(userId1).toBuilder().eventType(EventType.ENROLLMENT).build();
        final Webhook hook4 = getWebhook(userId2).toBuilder().eventType(EventType.PING).build();

        final List<Webhook> hookList = Arrays.asList(hook1, hook2);
        hookRepository.insert(hook1);
        hookRepository.insert(hook2);
        hookRepository.insert(hook3);
        hookRepository.insert(hook4);

        // When
        final Page<Hook> result = hookRepository.getHooksByEventType(userId1, pageable, EventType.PING.name());

        // Then
        assertThat(result).hasSize(2).isSubsetOf(hookList);
    }

    @Test
    public void updateHook_whenWebhook_shouldReturnWebhook() {
        // Given
        final Webhook webhook = getWebhook();
        final Webhook newHook = webhook.toBuilder().name("newName").build();
        hookRepository.insert(webhook);
        final String name = webhook.getName();

        // When
        final Hook result = hookRepository.updateHook(name, newHook);

        // Then
        assertThat(result).isEqualTo(newHook);
        assertThat(result.getClass()).isEqualTo(Webhook.class);
    }

    @Test
    public void updateHook_whenNewNameAlreadyTakenForUser_shouldThrowException() {
        // Given
        final String name = "oldName";
        final String newName = "newName";
        final Webhook oldHook = getWebhook().toBuilder().name(name).build();
        final Webhook newHook = oldHook.toBuilder().name("newName").build();
        final Webhook doublonNameHook = getWebhook(oldHook.getUserId()).toBuilder().name(newName).build();
        hookRepository.insert(doublonNameHook);

        // Then When
        assertThatExceptionOfType(HookUpdateFailedException.class).isThrownBy(() ->
                hookRepository.updateHook(name, newHook)
        );
    }

    private List<Hook> createHooks(String userId) {
        return IntStream.range(0, 20)
                .mapToObj((index) -> getWebhook(userId))
                .map(hook -> hookRepository.saveHook(hook))
                .collect(Collectors.toList());
    }

}
