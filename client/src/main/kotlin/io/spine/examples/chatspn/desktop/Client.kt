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
import io.spine.examples.chatspn.ChatDeletionId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.MessageRemovalId
import io.spine.examples.chatspn.account.UserChats
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.account.command.CreateAccount
import io.spine.examples.chatspn.account.event.AccountCreated
import io.spine.examples.chatspn.account.event.AccountNotCreated
import io.spine.examples.chatspn.chat.ChatMembers
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.chat.command.CreatePersonalChat
import io.spine.examples.chatspn.chat.command.DeleteChat
import io.spine.examples.chatspn.chat.event.PersonalChatCreated
import io.spine.examples.chatspn.message.MessageView
import io.spine.examples.chatspn.message.command.EditMessage
import io.spine.examples.chatspn.message.command.RemoveMessage
import io.spine.examples.chatspn.message.command.SendMessage
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted
import io.spine.net.EmailAddress

/**
 * Provides API to interact with ChatSpn server via gRPC.
 *
 * By default, client will open channel to 'localhost:[50051]
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
     * Registers a new user and authenticates it.
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
        val command = CreateAccount
            .newBuilder()
            .buildWith(email, name)
        observeCommandOutcome(
            command.id,
            AccountCreated::class.java,
            { event ->
                authenticatedUser = findUser(event.user)
                onSuccess()
            },
            AccountNotCreated::class.java,
            { onFail() }
        )
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
     * Forgets the credentials of the authenticated user.
     */
    public fun logOut() {
        authenticatedUser = null
    }

    /**
     * Finds user by ID.
     *
     * @param id ID of the user to find
     * @return found user profile or `null` if the user not found
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
     *
     * @param email email of the user to find
     * @return found user profile or `null` if the user not found
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
     * Reads members of the chat by the chat ID.
     *
     * @param chat ID of the chat to read members from
     * @return list of chat members, or an empty list if the chat does not exist
     */
    public fun readChatMembers(chat: ChatId): List<UserId> {
        val projections = clientRequest()
            .select(ChatMembers::class.java)
            .byId(chat)
            .run()
        if (projections.isEmpty()) {
            return listOf()
        }
        return projections[0].memberList
    }

    /**
     * Creates a new personal chat between authenticated and provided user.
     *
     * @param user user to create a personal chat with authenticated user
     * @param onSuccess will be called when the chat successfully created
     * @throws IllegalStateException if the user has not been authenticated
     */
    public fun createPersonalChat(
        user: UserId,
        onSuccess: (event: PersonalChatCreated) -> Unit = {}
    ) {
        checkNotNull(authenticatedUser) { "The user has not been authenticated" }
        val command = CreatePersonalChat
            .newBuilder()
            .buildWith(authenticatedUser!!.id, user)
        var subscription: Subscription? = null
        subscription = observeEvent(
            command.id,
            PersonalChatCreated::class.java
        )
        { event ->
            stopObservation(subscription!!)
            onSuccess(event)
        }
        clientRequest()
            .command(command)
            .postAndForget()
    }

    /**
     * Sends message to the chat.
     *
     * @param chat chat to which message will be sent
     * @param content message text content
     * @throws IllegalStateException if the user has not been authenticated
     */
    public fun sendMessage(chat: ChatId, content: String) {
        checkNotNull(authenticatedUser) { "The user has not been authenticated" }
        val command = SendMessage
            .newBuilder()
            .buildWith(chat, authenticatedUser!!.id, content)
        clientRequest()
            .command(command)
            .postAndForget()
    }

    /**
     * Removes message from the chat.
     *
     * @param chat сhat in which to remove the message
     * @param message ID of the message to remove
     * @throws IllegalStateException if the user has not been authenticated
     */
    public fun removeMessage(chat: ChatId, message: MessageId) {
        checkNotNull(authenticatedUser) { "The user has not been authenticated" }
        val command = RemoveMessage
            .newBuilder()
            .buildWith(chat, authenticatedUser!!.id, message)
        clientRequest()
            .command(command)
            .postAndForget()
    }

    /**
     * Deletes the chat.
     *
     * @param chat ID of the сhat to delete
     * @throws IllegalStateException if the user has not been authenticated
     */
    public fun deleteChat(chat: ChatId) {
        checkNotNull(authenticatedUser) { "The user has not been authenticated" }
        val command = DeleteChat
            .newBuilder()
            .buildWith(chat, authenticatedUser!!.id)
        clientRequest()
            .command(command)
            .postAndForget()
    }

    /**
     * Edits message in the chat.
     *
     * @param chat chat to edit the message in
     * @param message ID of the message to edit
     * @param newContent new text content for the message
     * @throws IllegalStateException if the user has not been authenticated
     */
    public fun editMessage(chat: ChatId, message: MessageId, newContent: String) {
        checkNotNull(authenticatedUser) { "The user has not been authenticated" }
        val command = EditMessage
            .newBuilder()
            .buildWith(chat, authenticatedUser!!.id, message, newContent)
        clientRequest()
            .command(command)
            .postAndForget()
    }

    /**
     * Returns chats where the authenticated user is a member.
     *
     * @throws IllegalStateException if the user has not been authenticated
     */
    public fun readChats(): List<ChatPreview> {
        checkNotNull(authenticatedUser) { "The user has not been authenticated" }
        val chats = clientRequest()
            .select(UserChats::class.java)
            .byId(authenticatedUser!!.id)
            .run()[0]
        return chats.chatList
    }

    /**
     * Observes chats of the authenticated user.
     *
     * @param onUpdate will be called when the user's chats updated
     * @throws IllegalStateException if the user has not been authenticated
     */
    public fun observeChats(onUpdate: (state: UserChats) -> Unit) {
        checkNotNull(authenticatedUser) { "The user has not been authenticated" }
        val subscription = clientRequest()
            .subscribeTo(UserChats::class.java)
            .byId(authenticatedUser!!.id)
            .observe(onUpdate)
            .post()
        userChatsSubscriptions.add(subscription)
    }

    /**
     * Stops chats observation.
     */
    public fun stopChatsObservation() {
        userChatsSubscriptions.forEach { subscription ->
            client.subscriptions().cancel(subscription)
        }
        userChatsSubscriptions.clear()
    }

    /**
     * Returns messages from the chat.
     *
     * @param chat ID of the chat to read messages from
     * @return list of messages in the chat
     * @throws IllegalStateException if the user has not been authenticated
     */
    public fun readMessages(chat: ChatId): List<MessageView> {
        checkNotNull(authenticatedUser) { "The user has not been authenticated" }
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
     * Observes messages in the chat.
     *
     * @param chat ID of the chat to observe messages in
     * @param onUpdate will be called when a new chat message is posted,
     *                     or when an existing message is updated
     * @param onDelete will be called when a message is deleted
     * @throws IllegalStateException if the user has not been authenticated
     */
    public fun observeMessages(
        chat: ChatId,
        onUpdate: (message: MessageView) -> Unit,
        onDelete: (messageDeleted: MessageMarkedAsDeleted) -> Unit
    ) {
        checkNotNull(authenticatedUser) { "The user has not been authenticated" }
        val updateSubscription = clientRequest()
            .subscribeTo(MessageView::class.java)
            .where(chat.stateFilter())
            .observe(onUpdate)
            .post()
        val deletionSubscription = clientRequest()
            .subscribeToEvent(MessageMarkedAsDeleted::class.java)
            .where(chat.eventFilter())
            .observe(onDelete)
            .post()
        messagesSubscriptions.add(updateSubscription)
        messagesSubscriptions.add(deletionSubscription)
    }

    /**
     * Stops messages observation.
     */
    public fun stopObservingMessages() {
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
     * Observes the provided event.
     *
     * @param id ID of the event state to observe
     * @param event type of the event to observe
     * @param onEmit will be called when the specified event emitted
     * @return subscription object to cancel observation
     */
    private fun <E : EventMessage> observeEvent(
        id: Message,
        event: Class<E>,
        onEmit: (event: E) -> Unit
    ): Subscription {
        val subscription = clientRequest()
            .subscribeToEvent(event)
            .where(EventFilter.eq(EventMessageField(Field.named("id")), id))
            .observe(onEmit)
            .post()
        return subscription
    }

    /**
     * Observes the outcome of the command.
     *
     * When a success or fail event is generated, subscriptions will be cancelled.
     *
     * @param id ID of the event state to observe
     * @param successEvent type of the success event to observe
     * @param onSuccess will be called when the specified success event emitted
     * @param failEvent type of the fail event to observe
     * @param onFail will be called when the specified fail event emitted
     */
    private fun <S : EventMessage, F : EventMessage> observeCommandOutcome(
        id: Message,
        successEvent: Class<S>,
        onSuccess: (event: S) -> Unit,
        failEvent: Class<F>,
        onFail: (event: F) -> Unit
    ) {
        var successSubscription: Subscription? = null
        var failSubscription: Subscription? = null
        successSubscription = observeEvent(
            id,
            successEvent
        ) { event ->
            stopObservation(successSubscription!!)
            stopObservation(failSubscription!!)
            onSuccess(event)
        }
        failSubscription = observeEvent(
            id,
            failEvent
        ) { event ->
            stopObservation(successSubscription)
            stopObservation(failSubscription!!)
            onFail(event)
        }
    }

    /**
     * Stops observation by provided subscription.
     *
     * @param subscription subscription to cancel observation
     */
    private fun stopObservation(subscription: Subscription) {
        client.subscriptions()
            .cancel(subscription)
    }
}

/**
 * Builds command to create an account for the user.
 *
 * @param email email of the user to create an account for
 * @param name name of the user to create an account for
 * @return command to create an account
 */
private fun CreateAccount.Builder.buildWith(email: String, name: String): CreateAccount {
    return this
        .setId(AccountCreationId.generate())
        .setUser(email.toUserId())
        .setEmail(email.toEmail())
        .setName(name)
        .vBuild()
}

/**
 * Builds command to create a personal chat between provided users.
 *
 * @param creator ID of the user who creates a personal chat
 * @param member ID of the user to create a personal chat with
 * @return command to create a personal chat
 */
private fun CreatePersonalChat.Builder.buildWith(
    creator: UserId,
    member: UserId
): CreatePersonalChat {
    return this
        .setId(ChatId.generate())
        .setCreator(creator)
        .setMember(member)
        .vBuild()
}

/**
 * Builds command to send a message to the chat.
 *
 * @param chat ID of the chat to send a message to
 * @param user ID of the user who wants to send a message
 * @param content message text content
 * @return command to send a message
 */
private fun SendMessage.Builder.buildWith(
    chat: ChatId,
    user: UserId,
    content: String
): SendMessage {
    return this
        .setId(MessageId.generate())
        .setChat(chat)
        .setUser(user)
        .setContent(content)
        .vBuild()
}

/**
 * Builds command to remove the message.
 *
 * @param chat ID of the chat to remove the message in
 * @param user ID of the user who wants to remove a message
 * @param message ID of the message to remove
 * @return command to remove the message
 */
private fun RemoveMessage.Builder.buildWith(
    chat: ChatId,
    user: UserId,
    message: MessageId,
): RemoveMessage {
    val removalId = MessageRemovalId
        .newBuilder()
        .setId(message)
        .vBuild()
    return this
        .setUser(user)
        .setChat(chat)
        .setId(removalId)
        .vBuild()
}

/**
 * Builds command to delete the chat.
 *
 * @param chat ID of the chat to delete
 * @param user ID of the user who wants to delete a chat
 * @return command to delete the chat
 */
private fun DeleteChat.Builder.buildWith(
    chat: ChatId,
    user: UserId
): DeleteChat {
    val deletionId = ChatDeletionId
        .newBuilder()
        .setId(chat)
        .vBuild()
    return this
        .setId(deletionId)
        .setWhoDeletes(user)
        .vBuild()
}

/**
 * Builds command to edit the message.
 *
 * @param chat ID of the chat to edit the message in
 * @param user ID of the user who wants to edit a message
 * @param message ID of the message to edit
 * @param newContent new text content for the message
 * @return command to edit the message
 */
private fun EditMessage.Builder.buildWith(
    chat: ChatId,
    user: UserId,
    message: MessageId,
    newContent: String,
): EditMessage {
    return this
        .setId(message)
        .setUser(user)
        .setChat(chat)
        .setSuggestedContent(newContent)
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
 * Creates a filter for the `QueryRequest` to match this chat.
 *
 * @see io.spine.client.QueryRequest.where
 */
private fun ChatId.queryFilter(): QueryFilter {
    val chatField = MessageView.Field
        .chat()
        .field
        .toString()
    return QueryFilter.eq(EntityColumn(chatField), this)
}

/**
 * Creates a filter for the `SubscriptionRequest` to match this chat.
 *
 * @see io.spine.client.SubscriptionRequest.where
 */
private fun ChatId.stateFilter(): EntityStateFilter {
    val chatField = MessageView.Field
        .chat()
        .field
    return EntityStateFilter.eq(EntityStateField(chatField), this)
}

/**
 * Creates a filter for the `EventSubscriptionRequest` to match this chat.
 *
 * @see io.spine.client.EventSubscriptionRequest.where
 */
private fun ChatId.eventFilter(): EventFilter {
    val chatField = MessageMarkedAsDeleted.Field
        .chat()
    return EventFilter.eq(chatField, this)
}
