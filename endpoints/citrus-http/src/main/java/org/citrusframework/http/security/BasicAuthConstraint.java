/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.http.security;

import org.eclipse.jetty.security.Constraint;

import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.jetty.security.Authenticator.BASIC_AUTH;
import static org.eclipse.jetty.security.Constraint.Transport.INHERIT;

/**
 * Convenient constraint instantiation for basic authentication and multiple user roles. Access allowed only for
 * authenticated user with specific role(s).
 *
 * @author Christoph Deppisch
 * @since 1.3
 */
public class BasicAuthConstraint implements Constraint {

    private final Set<String> userRoles;

    public BasicAuthConstraint(String[] userRoles) {
        this.userRoles = stream(userRoles).collect(toSet());
    }

    @Override
    public String getName() {
        return BASIC_AUTH;
    }

    @Override
    public Transport getTransport() {
        return INHERIT;
    }

    @Override
    public Authorization getAuthorization() {
        return Authorization.SPECIFIC_ROLE;
    }

    @Override
    public Set<String> getRoles() {
        return userRoles;
    }
}
