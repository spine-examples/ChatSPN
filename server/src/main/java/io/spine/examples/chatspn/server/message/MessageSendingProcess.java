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

package io.spine.examples.chatspn.server.message;

import io.spine.core.CommandContext;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.chat.ChatMembers;
import io.spine.examples.chatspn.message.MessageSending;
import io.spine.examples.chatspn.message.command.PostMessage;
import io.spine.examples.chatspn.message.command.SendMessage;
import io.spine.examples.chatspn.message.event.MessagePosted;
import io.spine.examples.chatspn.message.event.MessageSent;
import io.spine.examples.chatspn.message.rejection.MessageCannotBeSent;
import io.spine.examples.chatspn.server.ProjectionReader;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Coordinates the message sending to the chat.
 */
public final class MessageSendingProcess
        extends ProcessManager<MessageId, MessageSending, MessageSending.Builder> {

    /**
     * Reads chat members per chat.
     */
    @MonotonicNonNull
    private MemberChecker checker;

    /**
     * Issues a command to post message to the chat.
     *
     * @throws MessageCannotBeSent
     *         if the message sender is not a chat member,
     *         or chat does not exist
     */
    @Command
    PostMessage on(SendMessage c, CommandContext ctx) throws MessageCannotBeSent {
        builder().setId(c.getId());
        if (checker.checkMember(c.getChat(), c.getUser(), ctx)) {
            return PostMessage
                    .newBuilder()
                    .setId(c.getId())
                    .setChat(c.getChat())
                    .setUser(c.getUser())
                    .setContent(c.getContent())
                    .vBuild();
        }
        throw MessageCannotBeSent
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .build();
    }

    /**
     * Archives the process when the message was sent.
     */
    @React
    MessageSent on(MessagePosted e) {
        setArchived(true);
        return MessageSent
                .newBuilder()
                .setId(e.getId())
                .setChat(e.getChat())
                .setUser(e.getUser())
                .setContent(e.getContent())
                .vBuild();
    }

    void inject(ProjectionReader<ChatId, ChatMembers> reader) {
        checker = new MemberChecker(reader);
    }
}
