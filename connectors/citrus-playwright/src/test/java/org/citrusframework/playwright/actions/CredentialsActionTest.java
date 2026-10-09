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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.expectThrows;

import java.util.List;

import com.microsoft.playwright.Credentials;
import com.microsoft.playwright.options.VirtualCredential;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

class CredentialsActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;
    private Credentials credentials;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        credentials = mock(Credentials.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        browser.createContext("admin");
        when(browser.context().credentials()).thenReturn(credentials);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldSeedPasskeyForOrigin() {
        new CredentialsAction.Builder().create("example.com")
                .id("cred-1").userHandle("user-1").privateKey("pk").publicKey("pub")
                .build().execute(context);

        ArgumentCaptor<Credentials.CreateOptions> captor = ArgumentCaptor.forClass(Credentials.CreateOptions.class);
        verify(credentials).create(org.mockito.ArgumentMatchers.eq("example.com"), captor.capture());
        assertEquals("cred-1", captor.getValue().id);
        assertEquals("user-1", captor.getValue().userHandle);
    }

    @Test
    void shouldResolveVariablesInCredentialValues() {
        context.setVariable("credId", "cred-42");

        new CredentialsAction.Builder().create("example.com").id("${credId}").build().execute(context);

        ArgumentCaptor<Credentials.CreateOptions> captor = ArgumentCaptor.forClass(Credentials.CreateOptions.class);
        verify(credentials).create(org.mockito.ArgumentMatchers.eq("example.com"), captor.capture());
        assertEquals("cred-42", captor.getValue().id);
    }

    @Test
    void shouldInstallVirtualAuthenticator() {
        new CredentialsAction.Builder().install().build().execute(context);

        verify(credentials).install();
    }

    @Test
    void shouldDeleteCredentialsForOrigin() {
        new CredentialsAction.Builder().delete("example.com").build().execute(context);

        verify(credentials).delete("example.com");
    }

    @Test
    void shouldStoreOnlyCredentialIdInVariable() {
        VirtualCredential registered = new VirtualCredential();
        registered.id = "cred-1";
        registered.privateKey = "super-secret-key";
        when(credentials.get()).thenReturn(List.of(registered));

        new CredentialsAction.Builder().read("storedCredential").build().execute(context);

        assertEquals("cred-1", context.getVariable("storedCredential"));
        assertFalse(context.getVariables().containsValue("super-secret-key"),
                "Private key material must not reach the test context");
    }

    @Test
    void shouldFailWhenNoCredentialRegistered() {
        when(credentials.get()).thenReturn(List.of());

        CredentialsAction action = new CredentialsAction.Builder().read("storedCredential").build();

        expectThrows(CitrusRuntimeException.class, () -> action.execute(context));
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new CredentialsAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenOriginMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new CredentialsAction.Builder().create("").build());
    }
}
