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

package io.spine.examples.chatspn.desktop.registration

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.spine.client.Subscription
import io.spine.examples.chatspn.account.event.AccountCreated
import io.spine.examples.chatspn.account.event.AccountNotCreated
import io.spine.examples.chatspn.desktop.ClientFacade

/**
 * UI Model for the `RegistrationPage`.
 */
public class RegistrationPageModel(
    private val client: ClientFacade,
    public val toLogin: () -> Unit,
    private val toChats: () -> Unit,
) {
    public val emailState: MutableState<String> = mutableStateOf("")
    public val emailErrorState: MutableState<Boolean> = mutableStateOf(false)
    public val emailErrorText: MutableState<String> = mutableStateOf("")
    public val nameState: MutableState<String> = mutableStateOf("")
    public val nameErrorState: MutableState<Boolean> = mutableStateOf(false)
    public val nameErrorText: MutableState<String> = mutableStateOf("")

    /**
     * Registers a new user with the credentials specified in the form fields.
     */
    public fun register() {
        val name = nameState.value
        val email = emailState.value
        var successSubscription: Subscription? = null
        var failSubscription: Subscription? = null
        successSubscription = client.subscribeToEvent(
            AccountCreated::class.java
        ) { event ->
            if (event.email.value != email) {
                return@subscribeToEvent
            }
            client.authenticatedUser = client.findUser(email)
            toChats()
            client.cancelSubscription(successSubscription!!)
            client.cancelSubscription(failSubscription!!)
        }
        failSubscription = client.subscribeToEvent(
            AccountNotCreated::class.java
        ) { event ->
            if (event.email.value != email) {
                return@subscribeToEvent
            }
            emailErrorState.value = true
            emailErrorText.value = "An account with this email already exists"
            client.cancelSubscription(successSubscription)
            client.cancelSubscription(failSubscription!!)
        }
        client.register(name, email)
    }
}
