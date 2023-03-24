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

import java.util.Arrays;

/**
 * User model object for easy instantiation in Spring application context.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class User {

    /** User credentials */
    private String name;
    private String password;
    private String[] roles;
    
    /**
     * Default constructor.
     */
    public User() {
        super();
    }
    
    /**
     * Default constructor using fields.
     * @param name
     * @param password
     * @param roles
     */
    public User(String name, String password, String[] roles) {
        super();
        this.name = name;
        this.password = password;
        this.roles = Arrays.copyOf(roles, roles.length);
    }

    /**
     * Gets the name.
     * @return the name the name to get.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the password.
     * @return the password the password to get.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the roles.
     * @return the roles the roles to get.
     */
    public String[] getRoles() {
        return Arrays.copyOf(roles, roles.length);
    }

    /**
     * Sets the roles.
     * @param roles the roles to set
     */
    public void setRoles(String[] roles) {
        this.roles = Arrays.copyOf(roles, roles.length);
    }

}
