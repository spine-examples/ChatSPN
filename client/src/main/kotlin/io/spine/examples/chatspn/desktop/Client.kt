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

package io.spine.examples.chatspn.desktop

import com.google.protobuf.Message
import io.grpc.ManagedChannelBuilder
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

/**
 * Provides API to interact with ChatSpn server via gRPC.
 *
 * By default, client will open channel to 'localhost: [50051]
 * [io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT]'.
 */
public class DesktopClient(
    address: String = "localhost",
    port: Int = DEFAULT_CLIENT_SERVICE_PORT,
) {
    public var authenticatedUser: UserProfile? = null
    private val client: Client
    private val userChatsSubscriptions = mutableListOf<Subscription>()
    private val messagesSubscriptions = mutableListOf<Subscription>()

    init {
        val channel = ManagedChannelBuilder.forAddress(
            address,
            port
        )
            .usePlaintext()
            .build()
        client = Client.usingChannel(channel).build()
    }

    /**
     * Registers a new user.
     *
     * @param name name of the user to register
     * @param email email of the user to register
     * @param onSuccess will be called when the user successfully passed registration
     * @param onFail will be called when the user failed the registration
     */
    public fun register(
        name: String,
        email: String,
        onSuccess: () -> Unit = {},
        onFail: () -> Unit = {}
    ) {
        val command = createAccount(name, email)

        var successSubscription: Subscription? = null
        var failSubscription: Subscription? = null
        successSubscription = subscribeToEvent(
            command.id,
            AccountCreated::class.java
        ) { event ->
            cancelSubscription(successSubscription!!)
            cancelSubscription(failSubscription!!)
            authenticatedUser = findUser(event.user)
            onSuccess()
        }
        failSubscription = subscribeToEvent(
            command.id,
            AccountNotCreated::class.java
        ) { event ->
            cancelSubscription(successSubscription)
            cancelSubscription(failSubscription!!)
            onFail()
        }
        clientRequest()
            .command(command)
            .postAndForget()
    }

    /**
     * Authenticates the user.
     *
     * @param email email of the user to authenticate
     * @param onSuccess will be called when the user successfully passed the authentication
     * @param onFail will be called when the user failed the authentication
     */
    public fun logIn(
        email: String,
        onSuccess: () -> Unit = {},
        onFail: () -> Unit = {}
    ) {
        val user = findUser(email)
        if (null == user) {
            onFail()
        } else {
            authenticatedUser = user
            onSuccess()
        }
    }

    /**
     * Finds user by ID.
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
        clientRequest()
            .command(command)
            .postAndForget()
    }

    /**
     * Sends message to the chat.
     */
    public fun sendMessage(chat: ChatId, content: String) {
        val command = sendMessageCommand(chat, authenticatedUser!!.id, content)
        clientRequest()
            .command(command)
            .postAndForget()
    }

    /**
     * Returns chats that `authenticatedUser` is a member of.
     */
    public fun readChats(): List<ChatPreview> {
        val chats = clientRequest()
            .select(UserChats::class.java)
            .byId(authenticatedUser!!.id)
            .run()[0]
        return chats.chatList
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
     * Cancel all subscriptions on `authenticatedUser`'s chats changes.
     */
    public fun cancelChatsSubscriptions() {
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
     * @param chat id of the chat to subscribe on messages in
     * @param updateAction an action that will be triggered when a new chat message is posted,
     *                     or when an existing message is updated
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
     * Cancel all subscriptions on messages in the chat.
     */
    public fun cancelMessagesSubscriptions() {
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
     * Subscribes an `action` to the provided event with ID.
     */
    public fun <E : EventMessage> subscribeToEvent(
        id: Message,
        event: Class<E>,
        action: (event: E) -> Unit
    ): Subscription {
        val subscription = clientRequest()
            .subscribeToEvent(event)
            .where(EventFilter.eq(EventMessageField(Field.named("id")), id))
            .observe(action)
            .post()
        return subscription
    }

    /**
     * Subscribes an `action` to the provided event.
     */
    public fun <E : EventMessage> subscribeToEvent(
        event: Class<E>,
        action: (event: E) -> Unit
    ): Subscription {
        val subscription = clientRequest()
            .subscribeToEvent(event)
            .observe(action)
            .post()
        return subscription
    }

    /**
     * Cancel the provided subscription.
     */
    public fun cancelSubscription(subscription: Subscription) {
        client.subscriptions()
            .cancel(subscription)
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
