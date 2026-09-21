/*
 * Copyright the original author or authors.
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

package org.citrusframework.playwright.actions;

import java.util.List;

import com.microsoft.playwright.Credentials;
import com.microsoft.playwright.options.VirtualCredential;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Manages the WebAuthn virtual authenticator on the current browser context.
 *
 * <p>Lets a test seed a passkey, install the authenticator so the page's
 * {@code navigator.credentials} ceremonies are answered, read a registered credential back, and
 * remove it again - all without real hardware.</p>
 *
 * <p>Private key material is a secret. Only the credential id is ever stored into a test
 * variable, and no key material is written to logs or failure evidence.</p>
 */
public class CredentialsAction extends AbstractPlaywrightAction {

    public enum Command {
        CREATE,
        INSTALL,
        READ,
        DELETE
    }

    private final Command command;
    private final String origin;
    private final String id;
    private final String userHandle;
    private final String privateKey;
    private final String publicKey;
    private final String variable;

    public CredentialsAction(Builder builder) {
        super("credentials", builder);
        this.command = builder.command;
        this.origin = builder.origin;
        this.id = builder.id;
        this.userHandle = builder.userHandle;
        this.privateKey = builder.privateKey;
        this.publicKey = builder.publicKey;
        this.variable = builder.variable;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Credentials credentials = browser.getCurrentContext().credentials();

        switch (command) {
            case CREATE -> credentials.create(LocatorResolver.resolve(origin, context), createOptions(context));
            case INSTALL -> credentials.install();
            case DELETE -> credentials.delete(LocatorResolver.resolve(origin, context));
            case READ -> readCredential(credentials, context);
        }
    }

    private Credentials.CreateOptions createOptions(TestContext context) {
        Credentials.CreateOptions options = new Credentials.CreateOptions();

        if (id != null) {
            options.setId(LocatorResolver.resolve(id, context));
        }
        if (userHandle != null) {
            options.setUserHandle(LocatorResolver.resolve(userHandle, context));
        }
        if (privateKey != null) {
            options.setPrivateKey(LocatorResolver.resolve(privateKey, context));
        }
        if (publicKey != null) {
            options.setPublicKey(LocatorResolver.resolve(publicKey, context));
        }

        return options;
    }

    /**
     * Stores the id of the first registered credential in a test variable. Key material is
     * deliberately not exposed to the test context.
     *
     * @param credentials virtual authenticator of the current context
     * @param context test context receiving the credential id
     */
    private void readCredential(Credentials credentials, TestContext context) {
        List<VirtualCredential> registered = credentials.get();

        if (registered.isEmpty()) {
            throw new CitrusRuntimeException("No Playwright virtual credential registered on the current context");
        }

        context.setVariable(variable, registered.get(0).id);
    }

    /**
     * Fluent builder for virtual WebAuthn credential commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<CredentialsAction, Builder> {

        private Command command;
        private String origin;
        private String id;
        private String userHandle;
        private String privateKey;
        private String publicKey;
        private String variable;

        /**
         * Seeds a passkey for the given relying party origin.
         *
         * @param origin relying party origin, for example {@code example.com}
         * @return this builder
         */
        public Builder create(String origin) {
            this.command = Command.CREATE;
            this.origin = origin;
            return this;
        }

        /**
         * Installs the virtual authenticator so the page's credential ceremonies are answered.
         *
         * @return this builder
         */
        public Builder install() {
            this.command = Command.INSTALL;
            return this;
        }

        /**
         * Reads the registered credential back and stores its id in a test variable.
         *
         * @param variable test variable receiving the credential id
         * @return this builder
         */
        public Builder read(String variable) {
            this.command = Command.READ;
            this.variable = variable;
            return this;
        }

        /**
         * Removes the credentials registered for the given origin.
         *
         * @param origin relying party origin
         * @return this builder
         */
        public Builder delete(String origin) {
            this.command = Command.DELETE;
            this.origin = origin;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userHandle(String userHandle) {
            this.userHandle = userHandle;
            return this;
        }

        public Builder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder publicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        @Override
        public CredentialsAction build() {
            if (command == null) {
                throw new CitrusRuntimeException("Missing Playwright credentials command");
            }
            if ((command == Command.CREATE || command == Command.DELETE)
                    && (origin == null || origin.isBlank())) {
                throw new CitrusRuntimeException("Missing Playwright credentials origin");
            }
            if (command == Command.READ && (variable == null || variable.isBlank())) {
                throw new CitrusRuntimeException("Missing Playwright credentials target variable");
            }
            return new CredentialsAction(this);
        }
    }
}
