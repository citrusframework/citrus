/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.kubernetes.config;

import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.util.StringUtils.isEmpty;

public class CredentialValidator {

    public CredentialValidator() {
        throw new IllegalArgumentException("Utility class shall not be instantiated!");
    }

    /**
     * Validates the given credentials based on the following rules. Returns {@code true} if and only if none of the
     * following conditions are met:
     * <ul>
     *     <li>{@code username} is set AND {@code oauthToken} is set</li>
     *     <li>{@code password} is set AND {@code oauthToken} is set</li>
     *     <li>{@code username} is <b>not</b> set AND {@code password} is set</li>
     * </ul>
     * All other combinations are valid.
     *
     * @param username   The username.
     * @param password   The password.
     * @param oauthToken The OAuth token.
     * @return true if the combination is valid, false otherwise.
     */
    public static boolean isValid(String username, String password, String oauthToken) {
        return (!hasText(username) || !hasText(oauthToken))
                && (!hasText(password) || !hasText(oauthToken))
                && (!isEmpty(username) || !hasText(password));
    }
}
