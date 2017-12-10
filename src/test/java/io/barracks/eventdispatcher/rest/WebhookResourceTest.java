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

package io.barracks.eventdispatcher.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.barracks.commons.test.PagedResourcesUtils;
import io.barracks.eventdispatcher.manager.DeviceEventDispatcherManager;
import io.barracks.eventdispatcher.model.Hook;
import io.barracks.eventdispatcher.rest.entity.HookEntity;
import io.barracks.eventdispatcher.utils.HookEntityUtils;
import io.barracks.eventdispatcher.utils.RandomPrincipal;
import io.barracks.eventdispatcher.utils.WebhookUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;

import java.security.Principal;
import java.util.UUID;

import static io.barracks.eventdispatcher.rest.entity.HookEntity.fromHook;
import static io.barracks.eventdispatcher.rest.entity.HookEntity.fromHookPage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WebhookResourceTest {

    private WebhookResource resource;
    private ObjectMapper mapper = new ObjectMapper();
    private PagedResourcesAssembler<HookEntity> hookPagedResourcesAssembler = PagedResourcesUtils.getPagedResourcesAssembler();

    @Mock
    private DeviceEventDispatcherManager manager;

    @Before
    public void setup() {
        resource = new WebhookResource(hookPagedResourcesAssembler, mapper, manager);
    }


    @Test
    public void addWebHook_whenAllIsFine_shouldCallManager() {
        // Given
        final HookEntity entity = HookEntityUtils.getWebHookEntity();
        final Principal principal = new RandomPrincipal();
        final Hook expected = entity.toBuilder().userId(principal.getName()).build().toHook();
        final HookEntity entityResponse = fromHook(expected);
        doReturn(expected).when(manager).saveHook(expected);

        // When
        final HookEntity result = resource.addWebhook(entity, principal);

        // Then
        verify(manager).saveHook(expected);
        assertThat(result).isEqualTo(entityResponse);
    }

    @Test
    public void getHooks_whenAllIsFine_shouldCallManager() {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final Principal principal = new RandomPrincipal();
        final Hook hook1 = WebhookUtils.getWebhook();
        final Hook hook2 = WebhookUtils.getWebhook();
        final Page<Hook> hookPage = new PageImpl<>(Lists.newArrayList(hook1, hook2), pageable, 2L);
        final Page<HookEntity> hookEntityPage = fromHookPage(hookPage);
        final PagedResources<Resource<HookEntity>> expected = hookPagedResourcesAssembler.toResource(hookEntityPage);

        doReturn(hookPage).when(manager).getHooks(principal.getName(), pageable);

        // When
        final PagedResources<Resource<HookEntity>> result = resource.getHooks(pageable, principal);

        // Then
        verify(manager).getHooks(principal.getName(), pageable);
        assertThat(result).isEqualTo(expected);

    }

    @Test
    public void deleteWebHook_whenAllIsFine_shouldCallManager() {
        // Given
        final String name = UUID.randomUUID().toString();
        final Principal principal = new RandomPrincipal();
        final String userId = principal.getName();

        // When
        resource.deleteHook(name, principal);

        // Then
        verify(manager).deleteHook(userId, name);
    }

    @Test
    public void getHook_whenAllIsFine_shouldCallManager() {
        // Given
        final String name = UUID.randomUUID().toString();
        final Principal principal = new RandomPrincipal();
        final String userId = principal.getName();
        final Hook hook = WebhookUtils.getWebhook();
        final HookEntity expected = fromHook(hook);
        doReturn(hook).when(manager).getHook(userId, name);

        // When
        final HookEntity entity = resource.getHook(name, principal);

        // Then
        verify(manager).getHook(userId, name);
        assertThat(entity).isEqualTo(expected);
    }

    @Test
    public void updateHook_whenAllIsFine_shouldCallManager() {
        // Given
        final HookEntity entity = HookEntityUtils.getWebHookEntity();
        final Hook hook = entity.toHook();
        final String name = UUID.randomUUID().toString();
        final Principal principal = new RandomPrincipal();
        final String userId = principal.getName();
        final HookEntity expected = fromHook(hook);
        doReturn(hook).when(manager).updateHook(userId, name, hook);

        // When
        final HookEntity responseEntity = resource.updateHook(name, entity, principal);

        // Then
        verify(manager).updateHook(userId, name, hook);
        assertThat(responseEntity).isEqualTo(expected);
    }

}
