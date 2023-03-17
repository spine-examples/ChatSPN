/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.chatspn.server;

import com.google.common.collect.ImmutableSet;
import io.spine.base.EntityState;
import io.spine.client.ActorRequestFactory;
import io.spine.client.Query;
import io.spine.client.QueryFactory;
import io.spine.client.QueryResponse;
import io.spine.core.CommandContext;
import io.spine.grpc.MemoizingObserver;
import io.spine.server.stand.Stand;

import java.util.List;
import java.util.stream.Collectors;

import static io.spine.protobuf.AnyPacker.unpack;

/**
 * {@link Stand} wrapper to simplify the projections querying.
 *
 * @param <I>
 *         {@code Projection} id type.
 * @param <S>
 *         {@code Projection} state type.
 */
public final class ProjectionProvider<I, S extends EntityState> {

    private final Stand stand;
    private final Class<S> projectionStateClass;

    public ProjectionProvider(Stand stand, Class<S> projectionStateClass) {
        this.stand = stand;
        this.projectionStateClass = projectionStateClass;
    }

    /**
     * Querying projections by identifiers.
     */
    public List<S> getProjections(List<I> ids, CommandContext ctx) {
        QueryFactory queryFactory = ActorRequestFactory
                .fromContext(ctx.getActorContext())
                .query();
        Query query = queryFactory.byIds(
                projectionStateClass,
                ImmutableSet.of(ids)
        );
        return executeAndUnpackResponse(query);
    }

    private List<S> executeAndUnpackResponse(Query query) {
        MemoizingObserver<QueryResponse> observer = new MemoizingObserver<>();
        stand.execute(query, observer);
        QueryResponse response = observer.firstResponse();
        return response.getMessageList()
                       .stream()
                       .map(state -> unpack(state.getState(), projectionStateClass))
                       .collect(Collectors.toList());
    }
}
