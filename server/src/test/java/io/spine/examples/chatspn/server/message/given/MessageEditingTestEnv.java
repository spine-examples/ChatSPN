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

package io.spine.examples.chatspn.server.message.given;

import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatCardId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.ChatCard;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.command.EditMessage;
import io.spine.examples.chatspn.message.event.MessageContentUpdated;
import io.spine.examples.chatspn.message.event.MessageEdited;
import io.spine.examples.chatspn.message.event.MessageEditingFailed;
import io.spine.examples.chatspn.message.rejection.EditingRejections.MessageCannotBeEdited;
import io.spine.examples.chatspn.message.rejection.EditingRejections.MessageContentCannotBeUpdated;

import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_GROUP;
import static io.spine.testing.TestValues.randomString;

public final class MessageEditingTestEnv {

    /**
     * Prevents class instantiation.
     */
    private MessageEditingTestEnv() {
    }

    public static EditMessage editMessageCommand(Message message) {
        var command = EditMessage
                .newBuilder()
                .setId(message.getId())
                .setChat(message.getChat())
                .setUser(message.getUser())
                .setSuggestedContent(randomString())
                .vBuild();
        return command;
    }

    public static EditMessage editMessageCommandWith(Message message, UserId userId) {
        var command = EditMessage
                .newBuilder()
                .setId(message.getId())
                .setChat(message.getChat())
                .setUser(userId)
                .setSuggestedContent(randomString())
                .vBuild();
        return command;
    }

    public static EditMessage editMessageCommandWith(Message message, MessageId messageId) {
        var command = EditMessage
                .newBuilder()
                .setId(messageId)
                .setChat(message.getChat())
                .setUser(message.getUser())
                .setSuggestedContent(randomString())
                .vBuild();
        return command;
    }

    public static MessageEdited messageEditedFrom(EditMessage c) {
        var event = MessageEdited
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getSuggestedContent())
                .vBuild();
        return event;
    }

    public static MessageEditingFailed messageEditingFailedFrom(EditMessage c) {
        var event = MessageEditingFailed
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setSuggestedContent(c.getSuggestedContent())
                .vBuild();
        return event;
    }

    public static MessageContentUpdated messageContentUpdatedFrom(EditMessage c) {
        var event = MessageContentUpdated
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getSuggestedContent())
                .vBuild();
        return event;
    }

    public static Message messageFrom(EditMessage c) {
        var state = Message
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getSuggestedContent())
                .buildPartial();
        return state;
    }

    public static MessageView messageViewFrom(EditMessage c) {
        var state = MessageView
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getSuggestedContent())
                .buildPartial();
        return state;
    }

    public static MessageCannotBeEdited messageCannotBeEditedFrom(EditMessage c) {
        var rejection = MessageCannotBeEdited
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setSuggestedContent(c.getSuggestedContent())
                .vBuild();
        return rejection;
    }

    public static MessageContentCannotBeUpdated messageContentCannotBeUpdatedFrom(EditMessage c) {
        var rejection = MessageContentCannotBeUpdated
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setSuggestedContent(c.getSuggestedContent())
                .vBuild();
        return rejection;
    }

    public static ChatCard chatCardWithEditedMessage(Chat chat, EditMessage command, UserId user) {
        var messageView = MessageView
                .newBuilder()
                .setId(command.getId())
                .setChat(command.getChat())
                .setUser(command.getUser())
                .setContent(command.getSuggestedContent())
                .buildPartial();
        var chatCardId = ChatCardId
                .newBuilder()
                .setUser(user)
                .setChat(chat.getId())
                .vBuild();
        var state = ChatCard
                .newBuilder()
                .setCardId(chatCardId)
                .setChatId(chat.getId())
                .setViewer(user)
                .setName(chat.getName())
                .setType(CT_GROUP)
                .setLastMessage(messageView)
                .vBuild();
        return state;
    }
}
