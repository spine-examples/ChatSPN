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

package io.spine.examples.chatspn.desktop.client

import com.google.protobuf.Message
import io.grpc.ManagedChannelBuilder
import io.spine.base.CommandMessage
import io.spine.base.EntityColumn
import io.spine.base.EventMessage
import io.spine.base.EventMessageField
import io.spine.base.Field
import io.spine.client.Client
import io.spine.client.ClientRequest
import io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT
import io.spine.client.EventFilter
import io.spine.client.QueryFilter.eq
import io.spine.client.Subscription
import io.spine.core.UserId
import io.spine.examples.chatspn.AccountCreationId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.account.command.CreateAccount
import io.spine.examples.chatspn.account.event.AccountCreated
import io.spine.examples.chatspn.account.event.AccountNotCreated
import io.spine.net.EmailAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

public class ClientFacade private constructor() {
    public var authorizedUser: UserProfile? = null
    private val client: Client

    init {
        val channel = ManagedChannelBuilder.forAddress(
            "localhost",
            DEFAULT_CLIENT_SERVICE_PORT
        )
            .usePlaintext()
            .build()
        client = Client.usingChannel(channel).build()
    }

    public fun register(name: String, email: String): UserProfile {
        val command = createAccount(name, email)
        val future = subscribeToCommandOutcome(
            AccountCreated::class.java,
            AccountNotCreated::class.java,
            command.id
        )
        sendCommand(command)

        val pair = future.get(10, TimeUnit.SECONDS)
        if (null != pair.first) {
            authorizedUser = findUser(command.user)!!
            return authorizedUser!!
        }
        throw UserAlreadyRegisteredException()
    }

    public fun logIn(email: String): UserProfile {
        val user = findUser(email)
        if (null != user) {
            authorizedUser = user
            return authorizedUser!!
        }
        throw AccountNotFoundException()
    }

    private fun findUser(id: UserId): UserProfile? {
        val profiles = client
            .asGuest()
            .select(UserProfile::class.java)
            .byId(id)
            .run()
        if (profiles.isEmpty()) {
            return null
        }
        return profiles[0]
    }

    public fun findUser(email: String): UserProfile? {
        val emailField = UserProfile.Field
            .email()
            .field
            .toString()
        val profiles = client
            .asGuest()
            .select(UserProfile::class.java)
            .where(eq(EntityColumn(emailField), email.toEmail()))
            .run()
        if (profiles.isEmpty()) {
            return null
        }
        return profiles[0]
    }

    private fun sendCommand(command: CommandMessage) {
        val clientRequest: ClientRequest
        if (null == authorizedUser) {
            clientRequest = client.asGuest()
        } else {
            clientRequest = client.onBehalfOf(authorizedUser!!.id)
        }
        clientRequest
            .command(command)
            .postAndForget()
    }

    private fun <E : EventMessage> subscribeToCommandOutcome(
        event: Class<E>,
        id: Message
    ): CompletableFuture<E> {
        val future: CompletableFuture<E> = CompletableFuture()
        var subscription: Subscription? = null
        subscription = client
            .asGuest()
            .subscribeToEvent(event)
            .where(EventFilter.eq(EventMessageField(Field.named("id")), id))
            .observe { event ->
                future.complete(event)
                client.subscriptions().cancel(subscription!!)
            }
            .post()
        return future
    }

    private fun <S : EventMessage, F : EventMessage> subscribeToCommandOutcome(
        success: Class<S>,
        fail: Class<F>,
        id: Message
    ): CompletableFuture<Pair<S?, F?>> {
        val future: CompletableFuture<Pair<S?, F?>> = CompletableFuture()
        var successSubscription: Subscription? = null
        var failSubscription: Subscription? = null
        successSubscription = client
            .asGuest()
            .subscribeToEvent(success)
            .where(EventFilter.eq(EventMessageField(Field.named("id")), id))
            .observe { event ->
                future.complete(Pair(event, null))
                client.subscriptions().cancel(successSubscription!!)
                client.subscriptions().cancel(failSubscription!!)
            }
            .post()

        failSubscription = client
            .asGuest()
            .subscribeToEvent(fail)
            .where(EventFilter.eq(EventMessageField(Field.named("id")), id))
            .observe { event ->
                future.complete(Pair(null, event))
                client.subscriptions().cancel(successSubscription!!)
                client.subscriptions().cancel(failSubscription!!)

            }
            .post()

        return future
    }

    public companion object {
        public var client: ClientFacade = ClientFacade()
    }
}

private fun createAccount(name: String, email: String): CreateAccount {
    return CreateAccount
        .newBuilder()
        .setId(AccountCreationId.generate())
        .setUser(email.toUserId())
        .setEmail(email.toEmail())
        .setName(name)
        .vBuild()
}

/**
 * Creates `EmailAddress` with provided string as its value.
 */
private fun String.toEmail(): EmailAddress {
    return EmailAddress
        .newBuilder()
        .setValue(this)
        .vBuild()
}

/**
 * Creates `UserId` with provided string as its value.
 */
private fun String.toUserId(): UserId {
    return UserId
        .newBuilder()
        .setValue(this)
        .vBuild()
}
