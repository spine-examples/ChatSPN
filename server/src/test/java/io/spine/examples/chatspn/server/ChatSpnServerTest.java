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

import io.grpc.ManagedChannel;
import io.spine.base.EventMessageField;
import io.spine.base.Field;
import io.spine.client.Client;
import io.spine.client.EventFilter;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.account.event.AccountCreated;
import io.spine.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static io.grpc.ManagedChannelBuilder.forAddress;
import static io.spine.client.Client.usingChannel;
import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.chatspn.server.ExpectedOnlyAssertions.assertExpectedFields;
import static io.spine.examples.chatspn.server.given.ChatSpnServerTestEnv.accountCreated;
import static io.spine.examples.chatspn.server.given.ChatSpnServerTestEnv.createAccount;
import static io.spine.examples.chatspn.server.given.ChatSpnServerTestEnv.userProfile;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.concurrent.TimeUnit.SECONDS;

@DisplayName("ChatSPN server should")
final class ChatSpnServerTest {

    private static final int TEST_SERVER_PORT = DEFAULT_CLIENT_SERVICE_PORT;
    private static final String ADDRESS = "localhost";

    private final Collection<ManagedChannel> channels = new ArrayList<>();
    private final Collection<Client> clients = new ArrayList<>();
    private Server server;

    /**
     * Starts the server before each test case.
     */
    @BeforeEach
    void startServer() throws IOException {
        server = ChatSpnServer.create();
        server.start();
    }

    /**
     * Shutdowns the server after each test case.
     */
    @AfterEach
    void stopServer() {
        clients.forEach(Client::close);
        channels.forEach(channel -> {
            channel.shutdown();
            try {
                channel.awaitTermination(1, SECONDS);
            } catch (InterruptedException e) {
                throw newIllegalStateException(e, "Channel can't be terminated");
            }
        });
        server.shutdown();
    }

    @Test
    @DisplayName("handle command and request for projection status")
    void commandAndQuery() {
        var client = createClient();
        var command = createAccount();
        client.asGuest()
              .command(command)
              .postAndForget();
        var userProfileResponse = client
                .asGuest()
                .select(UserProfile.class)
                .run();
        var expectedUserProfile = userProfile(command);

        assertExpectedFields(userProfileResponse.get(0), expectedUserProfile);
    }

    @Test
    @DisplayName("handle subscription on entity")
    void entitySubscription() throws ExecutionException, InterruptedException,
                                     TimeoutException {
        var client = createClient();
        var command = createAccount();
        var futureEntity = new CompletableFuture<UserProfile>();
        client.asGuest()
              .subscribeTo(UserProfile.class)
              .observe(futureEntity::complete)
              .post();
        client.asGuest()
              .command(command)
              .postAndForget();
        var expectedUserProfile = userProfile(command);

        assertExpectedFields(futureEntity.get(2, SECONDS), expectedUserProfile);
    }

    @Test
    @DisplayName("handle subscription on entity by ID")
    void entitySubscriptionById() throws ExecutionException, InterruptedException,
                                         TimeoutException {
        var client = createClient();
        var firstCommand = createAccount();
        var secondCommand = createAccount();
        var futureEntity = new CompletableFuture<UserProfile>();
        client.asGuest()
              .subscribeTo(UserProfile.class)
              .byId(secondCommand.getUser())
              .observe(futureEntity::complete)
              .post();
        client.asGuest()
              .command(firstCommand)
              .postAndForget();
        client.asGuest()
              .command(secondCommand)
              .postAndForget();
        var expectedUserProfile = userProfile(secondCommand);

        assertExpectedFields(futureEntity.get(2, SECONDS), expectedUserProfile);
    }

    @Test
    @DisplayName("handle subscription on event")
    void eventSubscription() throws ExecutionException, InterruptedException,
                                    TimeoutException {
        var client = createClient();
        var command = createAccount();
        var futureEvent = new CompletableFuture<AccountCreated>();
        client.asGuest()
              .subscribeToEvent(AccountCreated.class)
              .observe(futureEvent::complete)
              .post();
        client.asGuest()
              .command(command)
              .postAndForget();
        var expectedEvent = accountCreated(command);

        assertExpectedFields(futureEvent.get(2, SECONDS), expectedEvent);
    }

    @Test
    @DisplayName("handle subscription on event with filter by 'id' field")
    void eventSubscriptionById() throws ExecutionException, InterruptedException,
                                        TimeoutException {
        var client = createClient();
        var firstCommand = createAccount();
        var secondCommand = createAccount();
        var futureEvent = new CompletableFuture<AccountCreated>();
        client.asGuest()
              .subscribeToEvent(AccountCreated.class)
              .where(EventFilter.eq(new EventMessageField(Field.named("id")),
                                    secondCommand.getId()))
              .observe(futureEvent::complete)
              .post();
        client.asGuest()
              .command(firstCommand)
              .postAndForget();
        client.asGuest()
              .command(secondCommand)
              .postAndForget();
        var expectedEvent = accountCreated(secondCommand);

        assertExpectedFields(futureEvent.get(2, SECONDS), expectedEvent);
    }

    /**
     * Creates a new client with access to the server.
     */
    Client createClient() {
        var channel = forAddress(ADDRESS, TEST_SERVER_PORT)
                .usePlaintext()
                .build();
        var client = usingChannel(channel).build();
        channels.add(channel);
        clients.add(client);
        return client;
    }
}
