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
package org.citrusframework.playwright.yaml;

import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.ConsoleAction;
import org.citrusframework.playwright.actions.CredentialsAction;
import org.citrusframework.playwright.actions.DropAction;
import org.citrusframework.playwright.actions.VerifyAction;
import org.citrusframework.playwright.actions.NetworkAction;
import org.citrusframework.playwright.dsl.PageObjectFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers how the declarative wrappers reject incomplete or unknown input, and that commands are
 * applied independently of the order in which attributes are declared.
 */
class ActionWrapperTest {

    @Test
    void shouldRejectMissingCommand() {
        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class, () -> new Console().build());

        assertEquals("Missing Playwright console command", exception.getMessage());
    }

    @Test
    void shouldBuildPageErrorCommands() {
        Console noErrors = new Console();
        noErrors.setCommand("verify-no-page-errors");
        assertEquals(ConsoleAction.Command.VERIFY_NO_PAGE_ERRORS, noErrors.build().getCommand());

        Console contains = new Console();
        contains.setCommand("verify-page-errors-contain");
        contains.setText("TypeError");
        assertEquals(ConsoleAction.Command.VERIFY_PAGE_ERRORS_CONTAIN, contains.build().getCommand());

        Console report = new Console();
        report.setCommand("page-errors-report");
        report.setVariable("pageErrors");
        assertEquals(ConsoleAction.Command.PAGE_ERRORS_REPORT, report.build().getCommand());
    }

    @Test
    void shouldPassConsoleFilter() {
        Console report = new Console();
        report.setCommand("report");
        report.setFilter("since-navigation");
        report.setVariable("consoleReport");

        assertEquals("since-navigation", report.build().getFilter());
    }

    @Test
    void shouldRejectUnsupportedCommand() {
        Network network = new Network();
        network.setCommand("rewind");

        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class, network::build);

        assertEquals("Unsupported Playwright network command: rewind", exception.getMessage());
    }

    @Test
    void shouldAcceptUnderscoreCommandSpelling() {
        Network network = new Network();
        network.setCommand("WAIT_FOR_RESPONSE");

        assertEquals(NetworkAction.Command.WAIT_FOR_RESPONSE, network.build().getCommand());
    }

    @Test
    void shouldApplyStatusAfterCommandWhateverTheDeclarationOrder() {
        Network network = new Network();
        network.setStatus(404);
        network.setCommand("wait-for-response");

        NetworkAction action = network.build();

        assertEquals(Integer.valueOf(404), action.getResponseStatus());
        assertEquals(Integer.valueOf(200), action.getStatus());
    }

    @Test
    void shouldApplyResponseAddressAndSecurityExpectations() {
        Network network = new Network();
        network.setCommand("wait-for-response");
        network.setUrlContains("/orders");
        network.setServerIp("93.184.216.34");
        network.setServerPort(443);
        network.setSecurityProtocol("TLSv1.3");

        NetworkAction action = network.build();

        assertEquals("93.184.216.34", action.getExpectedServerIp());
        assertEquals(Integer.valueOf(443), action.getExpectedServerPort());
        assertEquals("TLSv1.3", action.getExpectedSecurityProtocol());
    }

    @Test
    void shouldRejectIncompleteServerAddress() {
        Network network = new Network();
        network.setCommand("wait-for-response");
        network.setServerIp("93.184.216.34");

        assertThrows(CitrusRuntimeException.class, network::build);
    }

    @Test
    void shouldRejectIncompleteViewport() {
        Emulate emulate = new Emulate();
        emulate.setWidth(1280);

        assertThrows(CitrusRuntimeException.class, emulate::build);
    }

    @Test
    void shouldRejectFrameWithoutElement() {
        Frame frame = new Frame();
        frame.setCommand("click");

        assertThrows(CitrusRuntimeException.class, frame::build);
    }

    @Test
    void shouldRejectValidatorOfWrongType() {
        PageObject pageObject = new PageObject();

        assertThrows(CitrusRuntimeException.class,
                () -> pageObject.setValidator(PageObjectFixtures.FixturePage.class.getName()));
    }

    @Test
    void shouldRejectUnknownType() {
        PageObject pageObject = new PageObject();

        assertThrows(CitrusRuntimeException.class, () -> pageObject.setType("org.example.Missing"));
    }

    @Test
    void shouldPassPageObjectArguments() {
        PageObject pageObject = new PageObject();
        pageObject.setType(PageObjectFixtures.FixturePage.class.getName());
        pageObject.setMethod("open");
        pageObject.setArguments(List.of("first", "second third"));

        assertArrayEquals(new String[] { "first", "second third" }, pageObject.build().getArguments());
    }

    @Test
    void shouldRejectUnknownSameSite() {
        Cookies cookies = new Cookies();
        cookies.setCommand("add");
        cookies.setName("session");
        cookies.setValue("abc");
        cookies.setSameSite("lenient");

        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class, cookies::build);

        assertEquals("Unsupported Playwright cookie same-site: lenient", exception.getMessage());
    }

    @Test
    void shouldRejectAmbiguousPageSwitch() {
        Page page = new Page();
        page.setCommand("switch");
        page.setAlias("popup");
        page.setIndex(0);

        assertThrows(CitrusRuntimeException.class, page::build);
    }

    @Test
    void shouldRejectPageSwitchWithoutSelector() {
        Page page = new Page();
        page.setCommand("switch");

        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class, page::build);

        assertEquals("Missing Playwright page switch selector - use one of alias, index, title or url-contains", exception.getMessage());
    }

    @Test
    void shouldRejectTracePathOnStart() {
        Tracing tracing = new Tracing();
        tracing.setCommand("start");
        tracing.setPath("target/trace.zip");

        assertThrows(CitrusRuntimeException.class, tracing::build);
    }

    @Test
    void shouldRejectStorageStateOutsideCreate() {
        Context context = new Context();
        context.setCommand("switch");
        context.setAlias("admin");
        context.setStorageState("target/state.json");

        assertThrows(CitrusRuntimeException.class, context::build);
    }

    @Test
    void shouldCreateCredentialWhateverTheDeclarationOrder() {
        Credentials credentials = new Credentials();
        credentials.setCommand("create");
        credentials.setOrigin("example.com");
        credentials.setId("id");
        credentials.setUserHandle("user");
        credentials.setPrivateKey("private");
        credentials.setPublicKey("public");

        assertEquals(CredentialsAction.Command.CREATE, credentials.build().getCommand());
    }

    @Test
    void shouldApplyDropPayloadWhateverTheDeclarationOrder() {
        Drop drop = new Drop();
        drop.setValue("hello");
        drop.setFile("note.txt");
        drop.setContentType("text/plain");
        Element element = new Element();
        element.setCss("#dropzone");
        drop.setElement(element);

        assertEquals("note.txt", drop.build().getFileName());
    }

    @Test
    void shouldApplyDropOptions() {
        Drop drop = new Drop();
        drop.setValue("hello");
        drop.setFile("note.txt");
        drop.setContentType("text/plain");
        Element element = new Element();
        element.setCss("#dropzone");
        drop.setElement(element);
        drop.setTimeout(5000D);
        drop.setPositionX(10D);
        drop.setPositionY(20D);

        DropAction action = drop.build();
        assertEquals(Double.valueOf(5000), action.getTimeout());
        assertEquals(Double.valueOf(10), action.getPositionX());
        assertEquals(Double.valueOf(20), action.getPositionY());
    }

    @Test
    void shouldRejectIncompleteDropPosition() {
        Drop drop = new Drop();
        drop.setValue("hello");
        drop.setFile("note.txt");
        drop.setContentType("text/plain");
        Element element = new Element();
        element.setCss("#dropzone");
        drop.setElement(element);
        drop.setPositionX(10D);

        assertThrows(CitrusRuntimeException.class, drop::build);
    }

    @Test
    void shouldApplyAriaSnapshotTimeout() {
        Verify verify = new Verify();
        verify.setCheck("aria-snapshot-matches");
        verify.setExpected("- heading \"Title\"");
        verify.setTimeout(5000D);
        Element element = new Element();
        element.setCss("#panel");
        verify.setElement(element);

        VerifyAction action = verify.build();
        assertEquals(VerifyAction.Check.ARIA_SNAPSHOT_MATCHES, action.getCheck());
        assertEquals(Double.valueOf(5000), action.getTimeout());
    }

    @Test
    void shouldRejectUnknownStorageScope() {
        Storage storage = new Storage();

        assertThrows(CitrusRuntimeException.class, () -> storage.setScope("cookie"));
    }

    @Test
    void shouldPassStorageStateFlags() {
        Storage storage = new Storage();
        storage.setCommand("save-state");
        storage.setPath("target/state.json");
        storage.setIndexedDB(true);
        storage.setCredentials(true);

        assertEquals(Boolean.TRUE, storage.build().getIndexedDB());
        assertEquals(Boolean.TRUE, storage.build().getCredentials());
    }
}
