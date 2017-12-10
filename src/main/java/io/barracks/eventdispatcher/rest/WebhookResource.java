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
import io.barracks.eventdispatcher.manager.DeviceEventDispatcherManager;
import io.barracks.eventdispatcher.model.Hook;
import io.barracks.eventdispatcher.rest.entity.HookEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

import static io.barracks.eventdispatcher.rest.entity.HookEntity.fromHook;
import static io.barracks.eventdispatcher.rest.entity.HookEntity.fromHookPage;

@RestController
@RequestMapping("/hooks")
public class WebhookResource {


    private final DeviceEventDispatcherManager deviceEventDispatcherManager;

    private final PagedResourcesAssembler<HookEntity> assembler;

    private final ObjectMapper objectMapper;

    @Autowired
    public WebhookResource(
            PagedResourcesAssembler<HookEntity> assembler,
            ObjectMapper objectMapper,
            DeviceEventDispatcherManager deviceEventDispatcherManager
    ) {
        this.assembler = assembler;
        this.objectMapper = objectMapper;
        this.deviceEventDispatcherManager = deviceEventDispatcherManager;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public HookEntity addWebhook(@Valid @RequestBody HookEntity entity, Principal principal) {
        final Hook hook = entity.toBuilder().userId(principal.getName()).build().toHook();
        return fromHook(deviceEventDispatcherManager.saveHook(hook));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{name}")
    public HookEntity getHook(@PathVariable("name") String name, Principal principal) {
        final Hook result = deviceEventDispatcherManager.getHook(principal.getName(), name);
        return fromHook(result);
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<Resource<HookEntity>> getHooks(Pageable pageable, Principal principal) {
        final Page<HookEntity> hooks = fromHookPage(deviceEventDispatcherManager.getHooks(principal.getName(), pageable));
        return assembler.toResource(hooks);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHook(@PathVariable("name") String name, Principal principal) {
        deviceEventDispatcherManager.deleteHook(principal.getName(), name);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT, path = "/{name}")
    public HookEntity updateHook(@PathVariable("name") String name, @Valid @RequestBody HookEntity entity, Principal principal) {
        final Hook hook = entity.toHook();
        final Hook result = deviceEventDispatcherManager.updateHook(principal.getName(), name, hook);
        return fromHook(result);
    }

}
