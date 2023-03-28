/*
 * Copyright 2006-2013 the original author or authors.
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

import org.eclipse.jetty.util.security.Constraint;

/**
 * Convenient constraint instantiation for basic authentication and multiple user roles.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class BasicAuthConstraint extends Constraint {

    /** Serialization thingy. */
    private static final long serialVersionUID = -2295787554785979668L;

    /**
     * Default constructor using fields.
     */
    public BasicAuthConstraint(String[] roles) {
        setName(Constraint.__BASIC_AUTH);
        setRoles(roles);
        setAuthenticate(true);
    }
}
