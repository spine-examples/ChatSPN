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

package io.spine.examples.chatspn

import io.spine.core.UserId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.net.EmailAddress

/**
 * `UserProvider` for the manual testing purposes.
 */
public class TestDataUserProvider : UserProvider {
    private val users: List<UserProfile>
    private val loggedUser: UserProfile

    init {
        val vlad = UserProfile
            .newBuilder()
            .setId(userId("vladId"))
            .setEmail(email("vlad@teamdev.com"))
            .setName("Vlad")
            .vBuild()
        val artem = UserProfile
            .newBuilder()
            .setId(userId("artemId"))
            .setEmail(email("artem@teamdev.com"))
            .setName("Artem")
            .vBuild()
        val alex = UserProfile
            .newBuilder()
            .setId(userId("alexId"))
            .setEmail(email("alex@teamdev.com"))
            .setName("Alex")
            .vBuild()

        loggedUser = vlad
        users = mutableListOf(vlad, artem, alex)
    }

    override fun findUser(id: UserId): UserProfile {
        return users.filter { user -> user.id == id }[0]
    }

    override fun findUser(email: String): UserProfile {
        return users.filter { user -> user.email.value == email }[0]
    }

    override fun loggedUser(): UserProfile {
        return loggedUser
    }

    private fun userId(id: String): UserId {
        return UserId
            .newBuilder()
            .setValue(id)
            .vBuild()
    }

    private fun email(email: String): EmailAddress {
        return EmailAddress
            .newBuilder()
            .setValue(email)
            .vBuild()
    }
}
