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

package io.barracks.eventdispatcher.rest.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.barracks.commons.test.PagedResourcesUtils;
import io.barracks.commons.util.Endpoint;
import io.barracks.eventdispatcher.model.EventType;
import io.barracks.eventdispatcher.model.Hook;
import io.barracks.eventdispatcher.model.Webhook;
import io.barracks.eventdispatcher.rest.WebhookResource;
import io.barracks.eventdispatcher.rest.entity.HookEntity;
import io.barracks.eventdispatcher.utils.RandomPrincipal;
import io.barracks.eventdispatcher.utils.WebhookUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.FileCopyUtils;

import java.security.Principal;
import java.util.Arrays;
import java.util.UUID;

import static io.barracks.eventdispatcher.rest.entity.HookEntity.fromHook;
import static io.barracks.eventdispatcher.rest.entity.HookEntity.fromHookPage;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@BarracksResourceTest(controllers = WebhookResource.class, outputDir = "build/generated-snippets/hooks")
public class WebhookResourceConfigurationTest {

    private static final String baseUrl = "https://not.barracks.io";

    private static final Endpoint ADD_WEBHOOK_ENDPOINT = Endpoint.from(HttpMethod.POST, "/hooks");
    private static final Endpoint UPDATE_WEBHOOK_ENDPOINT = Endpoint.from(HttpMethod.PUT, "/hooks/{name}");
    private static final Endpoint GET_WEBHOOK_ENDPOINT = Endpoint.from(HttpMethod.GET, "/hooks/{name}");
    private static final Endpoint GET_WEBHOOKS_ENDPOINT = Endpoint.from(HttpMethod.GET, "/hooks");
    private static final Endpoint DELETE_WEBHOOK_ENDPOINT = Endpoint.from(HttpMethod.DELETE, "/hooks/{name}");

    @MockBean
    private WebhookResource webhookResource;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Value("classpath:io/barracks/eventdispatcher/resource/hook.json")
    private org.springframework.core.io.Resource hookResource;

    private RandomPrincipal principal;

    @Before
    public void setUp() throws Exception {
        this.principal = new RandomPrincipal();
    }

    @Test
    public void documentCreateHook() throws Exception {
        // Given
        final Endpoint endpoint = ADD_WEBHOOK_ENDPOINT;
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        final HookEntity entity = HookEntity.builder()
                .type("web")
                .eventType(EventType.PING)
                .name("hookName")
                .url("http://webhook/data")
                .userId("userName")
                .build();

        final Hook hook = entity.toHook();
        final HookEntity hookEntity = fromHook(hook);
        doReturn(hookEntity).when(webhookResource).addWebhook(eq(entity), any(Principal.class));

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.request(endpoint.getMethod(), endpoint.getPath())
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FileCopyUtils.copyToByteArray(hookResource.getInputStream()))
        );

        // Then
        verify(webhookResource).addWebhook(eq(entity), any(Principal.class));
        result.andExpect(status().isCreated())
                .andDo(document(
                        "create",
                        requestFields(
                                fieldWithPath("type").description("The type of hook we consider"),
                                fieldWithPath("eventType").description("The type of event we send to the hook"),
                                fieldWithPath("name").description("The name of this webhook"),
                                fieldWithPath("userId").description("The ID of the user"),
                                fieldWithPath("url").description("The url for this webhook")
                        ),
                        responseFields(
                                fieldWithPath("type").description("The type of hook we consider"),
                                fieldWithPath("userId").description("The ID of the user"),
                                fieldWithPath("name").description("The name of this webhook"),
                                fieldWithPath("eventType").description("The type of event we send to the hook"),
                                fieldWithPath("url").description("The url for this webhook")
                        )
                ));
    }

    @Test
    public void createHook_withValidParameters_shouldReturnHook() throws Exception {

        // Given
        final Endpoint endpoint = ADD_WEBHOOK_ENDPOINT;
        final HookEntity entity = HookEntity.builder()
                .type("web")
                .eventType(EventType.PING)
                .name("hookName")
                .url("http://webhook/data")
                .userId("userName")
                .build();

        final Hook hook = entity.toHook();
        final HookEntity hookEntity = fromHook(hook);
        doReturn(hookEntity).when(webhookResource).addWebhook(eq(entity), any(Principal.class));

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.request(endpoint.getMethod(), endpoint.getPath())
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FileCopyUtils.copyToByteArray(hookResource.getInputStream()))
        );

        // Then
        verify(webhookResource).addWebhook(eq(entity), any(Principal.class));
        result.andExpect(status().isCreated());
    }

    @Test
    public void documentUpdateHook() throws Exception {
        // Given
        final Endpoint endpoint = UPDATE_WEBHOOK_ENDPOINT;
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String name = "OldHookName";
        final HookEntity entity = HookEntity.builder()
                .type("web")
                .eventType(EventType.PING)
                .name("hookName")
                .url("http://webhook/data")
                .userId("userName")
                .build();

        final Hook hook = entity.toHook();
        final HookEntity hookEntity = fromHook(hook);
        doReturn(hookEntity).when(webhookResource).updateHook(eq(name), eq(entity), any(Principal.class));

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.request(endpoint.getMethod(), endpoint.getPath(), name)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FileCopyUtils.copyToByteArray(hookResource.getInputStream()))
        );

        // Then
        verify(webhookResource).updateHook(eq(name), eq(entity), any(Principal.class));
        result.andExpect(status().isOk())
                .andDo(document(
                        "update",
                        pathParameters(
                                parameterWithName("name").description("The name of this webhook")
                        ),
                        requestFields(
                                fieldWithPath("type").description("The type of hook we consider"),
                                fieldWithPath("eventType").description("The type of event we send to the hook"),
                                fieldWithPath("name").description("The name of this webhook"),
                                fieldWithPath("userId").description("The ID of the user"),
                                fieldWithPath("url").description("The url for this webhook")
                        ),
                        responseFields(
                                fieldWithPath("type").description("The type of hook we consider"),
                                fieldWithPath("userId").description("The ID of the user"),
                                fieldWithPath("name").description("The name of this webhook"),
                                fieldWithPath("eventType").description("The type of event we send to the hook"),
                                fieldWithPath("url").description("The url for this webhook")
                        )
                ));
    }

    @Test
    public void updateHook_withValidParameters_shouldReturnUpdatedHook() throws Exception {

        // Given
        final Endpoint endpoint = UPDATE_WEBHOOK_ENDPOINT;
        final String name = "OldHookName";
        final HookEntity entity = HookEntity.builder()
                .type("web")
                .eventType(EventType.PING)
                .name("hookName")
                .url("http://webhook/data")
                .userId("userName")
                .build();

        final Hook hook = entity.toHook();
        final HookEntity hookEntity = fromHook(hook);
        doReturn(hookEntity).when(webhookResource).updateHook(eq(name), eq(entity), any(Principal.class));

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.request(endpoint.getMethod(), endpoint.getPath(), name)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FileCopyUtils.copyToByteArray(hookResource.getInputStream()))
        );

        // Then
        verify(webhookResource).updateHook(eq(name), eq(entity), any(Principal.class));
        result.andExpect(status().isOk());
    }

    @Test
    public void documentGetHook() throws Exception {
        // Given
        final Endpoint endpoint = GET_WEBHOOK_ENDPOINT;
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String name = "Hook we want to retrieve";
        final Hook hook = Webhook.builder()
                .name("hookName")
                .url("http://webhook/data")
                .eventType(EventType.PING)
                .userId("userName")
                .build();
        final HookEntity hookEntity = fromHook(hook);
        doReturn(hookEntity).when(webhookResource).getHook(eq(name), any(Principal.class));

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.request(endpoint.getMethod(), endpoint.getPath(), name)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then
        verify(webhookResource).getHook(eq(name), any(Principal.class));
        result.andExpect(status().isOk())
                .andDo(document(
                        "get",
                        pathParameters(
                                parameterWithName("name").description("The name of this webhook")
                        ),
                        responseFields(
                                fieldWithPath("type").description("The type of hook considered."),
                                fieldWithPath("userId").description("The ID of the user"),
                                fieldWithPath("name").description("The name of this webhook"),
                                fieldWithPath("eventType").description("The type of event we send to the hook"),
                                fieldWithPath("url").description("The url for this webhook")
                        )
                ));
    }

    @Test
    public void getHook_withValidParameters_shouldReturnHook() throws Exception {
        // Given
        final Endpoint endpoint = GET_WEBHOOK_ENDPOINT;
        final String name = UUID.randomUUID().toString();
        final Hook hook = Webhook.builder()
                .name("hookName")
                .url("http://webhook/data")
                .userId("userName")
                .build();
        final HookEntity hookEntity = fromHook(hook);
        doReturn(hookEntity).when(webhookResource).getHook(eq(name), any(Principal.class));

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.request(endpoint.getMethod(), endpoint.getPath(), name)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then
        verify(webhookResource).getHook(eq(name), any(Principal.class));
        result.andExpect(status().isOk());
    }

    @Test
    public void documentGetHooks() throws Exception {
        //  Given
        final Endpoint endpoint = GET_WEBHOOKS_ENDPOINT;
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        final Pageable pageable = new PageRequest(0, 10);
        final Hook hook1 = WebhookUtils.getWebhook().toBuilder()
                .name("My first hook")
                .url("http://myfirstsite.com")
                .build();
        final Hook hook2 = WebhookUtils.getWebhook().toBuilder()
                .name("My second hook")
                .url("http://mysecondsite.com")
                .build();

        final Page<HookEntity> page = fromHookPage(new PageImpl<>(Arrays.asList(hook1, hook2), pageable, 2L));
        final PagedResources expected = PagedResourcesUtils.<HookEntity>getPagedResourcesAssembler().toResource(page);
        doReturn(expected).when(webhookResource).getHooks(pageable, principal);
        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.request(endpoint.getMethod(), endpoint.withBase(baseUrl).pageable(pageable).getURI())
                        .principal(principal)
        );

        // Then
        verify(webhookResource).getHooks(pageable, principal);
        result.andExpect(status().isOk())
                .andDo(document(
                        "list",
                        responseFields(
                                fieldWithPath("_embedded.hooks").description("The list of webhooks"),
                                fieldWithPath("_links").ignored(),
                                fieldWithPath("page").ignored()
                        )
                ));

    }

    @Test
    public void getHooks_shouldCallResource_andReturnResults() throws Exception {
        //Given
        //  Given
        final Endpoint endpoint = GET_WEBHOOKS_ENDPOINT;
        final Pageable pageable = new PageRequest(0, 10);
        final Hook hook1 = WebhookUtils.getWebhook();
        final Hook hook2 = WebhookUtils.getWebhook();

        final Page<HookEntity> page = fromHookPage(new PageImpl<>(Arrays.asList(hook1, hook2), pageable, 2L));
        final PagedResources expected = PagedResourcesUtils.<HookEntity>getPagedResourcesAssembler().toResource(page);

        doReturn(expected).when(webhookResource).getHooks(pageable, principal);

        // When
        final ResultActions result = mvc.perform(
                request(endpoint.getMethod(), endpoint.withBase(baseUrl).pageable(pageable).getURI())
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .principal(principal)
        );

        //Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.hooks", hasSize(page.getNumberOfElements())))
                .andExpect(jsonPath("$._embedded.hooks[0].name").value(hook1.getName()))
                .andExpect(jsonPath("$._embedded.hooks[1].name").value(hook2.getName()));
    }


    @Test
    public void documentDeleteHook() throws Exception {
        // Given
        final Endpoint endpoint = DELETE_WEBHOOK_ENDPOINT;
        final String name = UUID.randomUUID().toString();
        doNothing().when(webhookResource).deleteHook(name, principal);

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.request(endpoint.getMethod(), endpoint.getPath(), name)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then
        verify(webhookResource).deleteHook(name, principal);
        result.andExpect(status().isNoContent())
                .andDo(document(
                        "delete",
                        pathParameters(
                                parameterWithName("name").description("The name of this webhook")
                        )
                ));
    }

    @Test
    public void deleteHook_withValidParameters_shouldCallDeleteHook_andReturnNothing() throws Exception {

        // Given
        final Endpoint endpoint = DELETE_WEBHOOK_ENDPOINT;
        final String name = UUID.randomUUID().toString();
        doNothing().when(webhookResource).deleteHook(name, principal);

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.request(endpoint.getMethod(), endpoint.getPath(), name)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then
        verify(webhookResource).deleteHook(name, principal);
        result.andExpect(status().isNoContent());
    }

}
