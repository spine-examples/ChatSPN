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

package io.spine.examples.chatspn.server.e2e;

import io.grpc.ManagedChannel;
import io.spine.client.Client;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.grpc.ManagedChannelBuilder.forAddress;
import static io.spine.client.Client.usingChannel;
import static io.spine.server.Server.atPort;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Abstract base for test suites that need the connection to the {@link Server}.
 */
abstract class ServerRunningTest {

    private static final int TEST_SERVER_PORT = 4242;
    private static final String ADDRESS = "localhost";

    private final List<ManagedChannel> channels = new ArrayList<>();
    private final List<Client> clients = new ArrayList<>();
    private Server server;

    /**
     * Starts the server before each test case.
     */
    @BeforeEach
    void startServer() throws IOException {
        channels.clear();
        clients.clear();
        server = atPort(TEST_SERVER_PORT)
                .add(ChatsContext.newBuilder())
                .build();
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

    protected Client createClient() {
        ManagedChannel channel = forAddress(ADDRESS, TEST_SERVER_PORT)
                .usePlaintext()
                .build();
        Client client = usingChannel(channel).build();
        channels.add(channel);
        clients.add(client);
        return client;
    }
}
