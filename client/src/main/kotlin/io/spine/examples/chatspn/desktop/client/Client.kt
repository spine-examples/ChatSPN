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
import io.spine.base.EntityStateField
import io.spine.base.EventMessage
import io.spine.base.EventMessageField
import io.spine.base.Field
import io.spine.client.Client
import io.spine.client.ClientRequest
import io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT
import io.spine.client.EntityStateFilter
import io.spine.client.EventFilter
import io.spine.client.OrderBy
import io.spine.client.QueryFilter
import io.spine.client.Subscription
import io.spine.core.UserId
import io.spine.examples.chatspn.AccountCreationId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.account.UserChats
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.account.command.CreateAccount
import io.spine.examples.chatspn.account.event.AccountCreated
import io.spine.examples.chatspn.account.event.AccountNotCreated
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.chat.command.CreatePersonalChat
import io.spine.examples.chatspn.message.MessageView
import io.spine.examples.chatspn.message.command.SendMessage
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted
import io.spine.net.EmailAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Provides API to communicate with ChatSpn server.
 */
public class ClientFacade {
    public var authenticatedUser: UserProfile? = null
    private val client: Client
    private val userChatsSubscriptions = mutableListOf<Subscription>()
    private val messagesSubscriptions = mutableListOf<Subscription>()

    init {
        val channel = ManagedChannelBuilder.forAddress(
            "localhost",
            DEFAULT_CLIENT_SERVICE_PORT
        )
            .usePlaintext()
            .build()
        client = Client.usingChannel(channel).build()
    }

    /**
     * Sends command to register a new user.
     *
     * @throws UserAlreadyRegisteredException if user with provided email is already exist
     * @return profile of the registered user
     */
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
            authenticatedUser = findUser(command.user)!!
            return authenticatedUser!!
        }
        throw UserAlreadyRegisteredException()
    }

    /**
     * Authenticates the user.
     *
     * @throws AccountNotFoundException if the user with the provided credentials doesn't exist
     * @return profile of the authenticated user
     */
    public fun logIn(email: String): UserProfile {
        val user = findUser(email)
        if (null != user) {
            authenticatedUser = user
            return authenticatedUser!!
        }
        throw AccountNotFoundException()
    }

    /**
     * Finds user by id.
     */
    public fun findUser(id: UserId): UserProfile? {
        val profiles = clientRequest()
            .select(UserProfile::class.java)
            .byId(id)
            .run()
        if (profiles.isEmpty()) {
            return null
        }
        return profiles[0]
    }

    /**
     * Finds user by email.
     */
    public fun findUser(email: String): UserProfile? {
        val emailField = UserProfile.Field
            .email()
            .field
            .toString()
        val profiles = clientRequest()
            .select(UserProfile::class.java)
            .where(QueryFilter.eq(EntityColumn(emailField), email.toEmail()))
            .run()
        if (profiles.isEmpty()) {
            return null
        }
        return profiles[0]
    }

    /**
     * Creates a new personal chat.
     */
    public fun createPersonalChat(user: UserId) {
        val command = createPersonalChatCommand(user, authenticatedUser!!.id)
        sendCommand(command)
    }

    /**
     * Sends message to the chat.
     */
    public fun sendMessage(chat: ChatId, content: String) {
        val command = sendMessageCommand(chat, authenticatedUser!!.id, content)
        sendCommand(command)
    }

    /**
     * Returns chats that `authenticatedUser` is a member of.
     */
    public fun readChats(): List<ChatPreview> {
        val chats = clientRequest()
            .select(UserChats::class.java)
            .byId(authenticatedUser!!.id)
            .run()[0]
        return chats.chatList;
    }

    /**
     * Subscribes `action` on `authenticatedUser`'s chats changes.
     */
    public fun subscribeOnChats(action: (state: UserChats) -> Unit) {
        val subscription = clientRequest()
            .subscribeTo(UserChats::class.java)
            .byId(authenticatedUser!!.id)
            .observe(action)
            .post()
        userChatsSubscriptions.add(subscription)
    }

    /**
     * Clears all subscriptions on `authenticatedUser`'s chats changes.
     */
    public fun clearChatsSubscriptions() {
        userChatsSubscriptions.forEach { subscription ->
            client.subscriptions().cancel(subscription)
        }
        userChatsSubscriptions.clear()
    }

    /**
     * Returns messages from the provided chat.
     */
    public fun readMessages(chat: ChatId): List<MessageView> {
        val whenPostedField = MessageView.Field
            .whenPosted()
            .field
            .toString()
        val messages = clientRequest()
            .select(MessageView::class.java)
            .where(chat.queryFilter())
            .orderBy(EntityColumn(whenPostedField), OrderBy.Direction.ASCENDING)
            .run()
        return messages
    }

    /**
     * Subscribes actions on messages in the chat.
     *
     * @param chat chat to subscribe on messages in
     * @param updateAction an action that will be triggered when a new chat message is posted,
     *                     or when an existing message is updated.
     * @param deleteAction an action that will be triggered when a message is deleted
     */
    public fun subscribeOnMessages(
        chat: ChatId,
        updateAction: (message: MessageView) -> Unit,
        deleteAction: (messageDeleted: MessageMarkedAsDeleted) -> Unit
    ) {
        val updateSubscription = clientRequest()
            .subscribeTo(MessageView::class.java)
            .where(chat.stateFilter())
            .observe(updateAction)
            .post()
        val deletionSubscription = clientRequest()
            .subscribeToEvent(MessageMarkedAsDeleted::class.java)
            .where(chat.eventFilter())
            .observe(deleteAction)
            .post()
        messagesSubscriptions.add(updateSubscription)
        messagesSubscriptions.add(deletionSubscription)
    }

    /**
     * Clears all subscriptions on messages in the chat.
     */
    public fun clearMessagesSubscriptions() {
        messagesSubscriptions.forEach { subscription ->
            client.subscriptions().cancel(subscription)
        }
        messagesSubscriptions.clear()
    }

    /**
     * Provides `ClientRequest` on behalf of `authenticatedUser` if it exists,
     * or as guest if it doesn't.
     */
    private fun clientRequest(): ClientRequest {
        if (null == authenticatedUser) {
            return client.asGuest()
        }
        return client.onBehalfOf(authenticatedUser!!.id)

    }

    /**
     * Sends command.
     */
    private fun sendCommand(command: CommandMessage) {
        clientRequest()
            .command(command)
            .postAndForget()
    }

    /**
     * Subscribes a `CompletableFuture` to the provided event.
     *
     * `CompletableFuture` will be completed when the event is emitted.
     */
    private fun <E : EventMessage> subscribeToCommandOutcome(
        event: Class<E>,
        id: Message
    ): CompletableFuture<E> {
        val future: CompletableFuture<E> = CompletableFuture()
        var subscription: Subscription? = null
        subscription = clientRequest()
            .subscribeToEvent(event)
            .where(EventFilter.eq(EventMessageField(Field.named("id")), id))
            .observe { event ->
                future.complete(event)
                client.subscriptions().cancel(subscription!!)
            }
            .post()
        return future
    }

    /**
     * Subscribes a `CompletableFuture` to the provided events.
     *
     * `CompletableFuture` will be completed when at least one of the events is emitted.
     */
    private fun <S : EventMessage, F : EventMessage> subscribeToCommandOutcome(
        success: Class<S>,
        fail: Class<F>,
        id: Message
    ): CompletableFuture<Pair<S?, F?>> {
        val future: CompletableFuture<Pair<S?, F?>> = CompletableFuture()
        var successSubscription: Subscription? = null
        var failSubscription: Subscription? = null
        successSubscription = clientRequest()
            .subscribeToEvent(success)
            .where(EventFilter.eq(EventMessageField(Field.named("id")), id))
            .observe { event ->
                future.complete(Pair(event, null))
                client.subscriptions().cancel(successSubscription!!)
                client.subscriptions().cancel(failSubscription!!)
            }
            .post()

        failSubscription = clientRequest()
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
}

/**
 * Creates `CreateAccount` command.
 */
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
 * Creates `CreatePersonalChat` command.
 */
private fun createPersonalChatCommand(creator: UserId, member: UserId): CreatePersonalChat {
    return CreatePersonalChat
        .newBuilder()
        .setId(ChatId.generate())
        .setCreator(creator)
        .setMember(member)
        .vBuild()
}

/**
 * Creates `SendMessage` command.
 */
private fun sendMessageCommand(chatId: ChatId, userId: UserId, content: String): SendMessage {
    return SendMessage
        .newBuilder()
        .setId(MessageId.generate())
        .setChat(chatId)
        .setUser(userId)
        .setContent(content)
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

/**
 * Creates `QueryFilter` to filter `chat` field equality.
 */
private fun ChatId.queryFilter(): QueryFilter {
    val chatField = MessageView.Field
        .chat()
        .field
        .toString()
    return QueryFilter.eq(EntityColumn(chatField), this)
}

/**
 * Creates `EntityStateFilter` to filter `chat` field equality.
 */
private fun ChatId.stateFilter(): EntityStateFilter? {
    val chatField = MessageView.Field
        .chat()
        .field
    return EntityStateFilter.eq(EntityStateField(chatField), this)
}

/**
 * Creates `EventFilter` to filter `chat` field equality.
 */
private fun ChatId.eventFilter(): EventFilter? {
    val chatField = MessageMarkedAsDeleted.Field
        .chat()
    return EventFilter.eq(chatField, this)
}