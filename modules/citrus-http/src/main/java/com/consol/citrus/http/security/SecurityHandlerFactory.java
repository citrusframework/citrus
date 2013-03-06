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

package com.consol.citrus.http.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.MappedLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory bean constructs a security handler for usage in Jetty servlet container. Security handler
 * holds one to many constraints and a set of users known to a user login service for authentication.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class SecurityHandlerFactory implements InitializingBean, FactoryBean<SecurityHandler> {

    /** User credentials known to login service */
    private List<User> users = new ArrayList<User>();
    
    /** Realm name for this security handler */
    private String realm = "realm";
    
    /** List of constraints with mapping path as key */
    private Map<String, Constraint> constraints = new HashMap<String, Constraint>();
    
    /** User login service consolidated for user authentication */
    private MappedLoginService loginService;
    
    /** Authenticator implementation -  basic auth by default */
    private Authenticator authenticator = new BasicAuthenticator();
    
    /**
     * Construct new security handler for basic authentication.
     */
    public SecurityHandler getObject() throws Exception {
        
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticator(authenticator);
        securityHandler.setRealmName(realm);
        
        for (Entry<String, Constraint> constraint : constraints.entrySet()) {
            ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint.getValue());
            constraintMapping.setPathSpec(constraint.getKey());
            
            securityHandler.addConstraintMapping(constraintMapping);
        }
        
        for (User user : users) {
            loginService.putUser(user.getName(), Credential.getCredential(user.getPassword()), user.getRoles());
        }
        
        securityHandler.setLoginService(loginService);
        
        return securityHandler;
    }

    /**
     * Initialize member variables if not set by user in application context.
     */
    public void afterPropertiesSet() throws Exception {
        if (loginService == null) {
            loginService = new HashLoginService();
            ((HashLoginService)loginService).setName(realm);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Class<?> getObjectType() {
        return SecurityHandler.class;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * Gets the users.
     * @return the users the users to get.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Sets the users.
     * @param users the users to set
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Gets the realm.
     * @return the realm the realm to get.
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Sets the realm.
     * @param realm the realm to set
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Gets the constraints.
     * @return the constraints the constraints to get.
     */
    public Map<String, Constraint> getConstraints() {
        return constraints;
    }

    /**
     * Sets the constraints.
     * @param constraints the constraints to set
     */
    public void setConstraints(Map<String, Constraint> constraints) {
        this.constraints = constraints;
    }

    /**
     * Gets the loginService.
     * @return the loginService the loginService to get.
     */
    public MappedLoginService getLoginService() {
        return loginService;
    }

    /**
     * Sets the loginService.
     * @param loginService the loginService to set
     */
    public void setLoginService(MappedLoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Gets the authenticator.
     * @return the authenticator the authenticator to get.
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Sets the authenticator.
     * @param authenticator the authenticator to set
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

}
