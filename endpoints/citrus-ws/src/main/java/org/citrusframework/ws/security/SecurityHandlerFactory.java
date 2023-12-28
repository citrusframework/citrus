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

package org.citrusframework.ws.security;

import org.citrusframework.common.InitializingPhase;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.PropertyUserStore;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Credential;
import org.springframework.beans.factory.FactoryBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.eclipse.jetty.util.security.Credential.getCredential;

/**
 * Factory bean constructs a security handler for usage in Jetty servlet container. Security handler
 * holds one to many constraints and a set of users known to a user login service for authentication.
 *
 * @author Christoph Deppisch
 * @since 1.3
 */
public class SecurityHandlerFactory implements FactoryBean<SecurityHandler>, InitializingPhase {

    /**
     * User credentials known to login service
     */
    private List<User> users = new ArrayList<>();

    /**
     * Realm name for this security handler
     */
    private String realm = "realm";

    /**
     * List of constraints with mapping path as key
     */
    private Map<String, Constraint> constraints = new HashMap<>();

    /**
     * User login service consolidated for user authentication
     */
    private LoginService loginService;

    /**
     * Authenticator implementation -  basic auth by default
     */
    private Authenticator authenticator = new BasicAuthenticator();

    /**
     * Construct new security handler for basic authentication.
     */
    public SecurityHandler getObject() {
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticator(authenticator);
        securityHandler.setRealmName(realm);

        for (Entry<String, Constraint> constraint : constraints.entrySet()) {
            ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint.getValue());
            constraintMapping.setPathSpec(constraint.getKey());

            securityHandler.addConstraintMapping(constraintMapping);
        }

        securityHandler.setLoginService(loginService);

        return securityHandler;
    }

    @Override
    public void initialize() {
        if (loginService == null) {
            loginService = new SimpleLoginService();
            ((SimpleLoginService) loginService).setName(realm);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return SecurityHandler.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Gets the users.
     *
     * @return the users to get.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Sets the users.
     *
     * @param users the users to set
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Gets the realm.
     *
     * @return the realm to get.
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Sets the realm.
     *
     * @param realm the realm to set
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Gets the constraints.
     *
     * @return the constraints to get.
     */
    public Map<String, Constraint> getConstraints() {
        return constraints;
    }

    /**
     * Sets the constraints.
     *
     * @param constraints the constraints to set
     */
    public void setConstraints(Map<String, Constraint> constraints) {
        this.constraints = constraints;
    }

    /**
     * Gets the loginService.
     *
     * @return the loginService to get.
     */
    public LoginService getLoginService() {
        return loginService;
    }

    /**
     * Sets the loginService.
     *
     * @param loginService the loginService to set
     */
    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Gets the authenticator.
     *
     * @return the authenticator to get.
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Sets the authenticator.
     *
     * @param authenticator the authenticator to set
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Simple login service adds known users.
     */
    private class SimpleLoginService extends HashLoginService {
        @Override
        protected void doStart() throws Exception {
            SimplePropertyUserStore userStore = new SimplePropertyUserStore();
            setUserStore(userStore);
            userStore.start();

            super.doStart();
        }
    }

    /**
     * Simple user store loads users from this factories user list.
     */
    private class SimplePropertyUserStore extends PropertyUserStore {

        @Override
        protected void loadUsers() {
            for (org.citrusframework.ws.security.User user : users) {
                Credential credential = getCredential(user.getPassword());

                String[] roles = isNotEmpty(user.getRoles()) ? user.getRoles() : new String[0];
                addUser(user.getName(), credential, roles);
            }
        }
    }
}
