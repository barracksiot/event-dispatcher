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

import io.barracks.eventdispatcher.model.Hook;
import io.barracks.eventdispatcher.repository.exception.HookCreationFailedException;
import io.barracks.eventdispatcher.repository.exception.HookNotFoundException;
import io.barracks.eventdispatcher.repository.exception.HookUpdateFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class HookRepositoryImpl implements HookRepositoryCustom {

    private static final String USER_ID_KEY = "userId";
    private static final String NAME_KEY = "name";
    private static final String EVENT_TYPE_KEY = "eventType";
    private final MongoOperations operations;

    @Autowired
    public HookRepositoryImpl(MongoOperations operations) {
        this.operations = operations;
    }

    @Override
    public Hook saveHook(Hook hook) {
        try {
            operations.insert(hook);
            return operations.findById(hook.getId(), hook.getClass());
        } catch (DuplicateKeyException e) {
            throw new HookCreationFailedException(hook, e.getCause());
        }
    }

    @Override
    public Optional<Hook> getHookByUserIdAndName(String userId, String name) {
        final Query query = query(where(USER_ID_KEY).is(userId).and(NAME_KEY).is(name));
        return Optional.ofNullable(operations.findOne(query, Hook.class));
    }

    @Override
    public void deleteHook(String userId, String name) {
        final Optional<Hook> hook = getHookByUserIdAndName(userId, name);
        if (hook.isPresent()) {
            operations.remove(hook.get());
        } else {
            throw new HookNotFoundException(userId, name);
        }
    }

    @Override
    public Page<Hook> getHooks(String userId, Pageable pageable) {
        final Query query = query(where(USER_ID_KEY).is(userId));
        final long count = operations.count(query, Hook.class);
        final List<Hook> hooks = operations.find(query.with(pageable), Hook.class);
        return new PageImpl<>(hooks, pageable, count);
    }

    @Override
    public Page<Hook> getHooksByEventType(String userId, Pageable pageable, String eventType) {
        final Query query = query(where(USER_ID_KEY).is(userId).and(EVENT_TYPE_KEY).is(eventType));
        final long count = operations.count(query, Hook.class);
        final List<Hook> hooks = operations.find(query.with(pageable), Hook.class);
        return new PageImpl<>(hooks, pageable, count);
    }

    @Override
    public Hook updateHook(String name, Hook hook) {
        if (name.equals(hook.getName()) || !getHookByUserIdAndName(hook.getUserId(), hook.getName()).isPresent()) {
            operations.save(hook);
            return operations.findById(hook.getId(), hook.getClass());
        } else {
            throw new HookUpdateFailedException(hook);
        }
    }
}

