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
import org.citrusframework.playwright.actions.CredentialsAction;
import org.citrusframework.playwright.actions.NetworkAction;
import org.citrusframework.playwright.dsl.PageObjectFixtures;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

/**
 * Covers how the declarative wrappers reject incomplete or unknown input, and that commands are
 * applied independently of the order in which attributes are declared.
 */
class ActionWrapperTest {

    @Test
    void shouldRejectMissingCommand() {
        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class, () -> new Console().build());

        assertEquals(exception.getMessage(), "Missing Playwright console command");
    }

    @Test
    void shouldRejectUnsupportedCommand() {
        Network network = new Network();
        network.setCommand("rewind");

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class, network::build);

        assertEquals(exception.getMessage(), "Unsupported Playwright network command: rewind");
    }

    @Test
    void shouldAcceptUnderscoreCommandSpelling() {
        Network network = new Network();
        network.setCommand("WAIT_FOR_RESPONSE");

        assertEquals(network.build().getCommand(), NetworkAction.Command.WAIT_FOR_RESPONSE);
    }

    @Test
    void shouldApplyStatusAfterCommandWhateverTheDeclarationOrder() {
        Network network = new Network();
        network.setStatus(404);
        network.setCommand("wait-for-response");

        NetworkAction action = network.build();

        assertEquals(action.getResponseStatus(), Integer.valueOf(404));
        assertEquals(action.getStatus(), Integer.valueOf(200));
    }

    @Test
    void shouldRejectIncompleteViewport() {
        Emulate emulate = new Emulate();
        emulate.setWidth(1280);

        expectThrows(CitrusRuntimeException.class, emulate::build);
    }

    @Test
    void shouldRejectFrameWithoutElement() {
        Frame frame = new Frame();
        frame.setCommand("click");

        expectThrows(CitrusRuntimeException.class, frame::build);
    }

    @Test
    void shouldRejectValidatorOfWrongType() {
        PageObject pageObject = new PageObject();

        expectThrows(CitrusRuntimeException.class,
                () -> pageObject.setValidator(PageObjectFixtures.FixturePage.class.getName()));
    }

    @Test
    void shouldRejectUnknownType() {
        PageObject pageObject = new PageObject();

        expectThrows(CitrusRuntimeException.class, () -> pageObject.setType("org.example.Missing"));
    }

    @Test
    void shouldPassPageObjectArguments() {
        PageObject pageObject = new PageObject();
        pageObject.setType(PageObjectFixtures.FixturePage.class.getName());
        pageObject.setMethod("open");
        pageObject.setArguments(List.of("first", "second third"));

        assertEquals(pageObject.build().getArguments(), new String[] { "first", "second third" });
    }

    @Test
    void shouldRejectUnknownSameSite() {
        Cookies cookies = new Cookies();
        cookies.setCommand("add");
        cookies.setName("session");
        cookies.setValue("abc");
        cookies.setSameSite("lenient");

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class, cookies::build);

        assertEquals(exception.getMessage(), "Unsupported Playwright cookie same-site: lenient");
    }

    @Test
    void shouldRejectAmbiguousPageSwitch() {
        Page page = new Page();
        page.setCommand("switch");
        page.setAlias("popup");
        page.setIndex(0);

        expectThrows(CitrusRuntimeException.class, page::build);
    }

    @Test
    void shouldRejectPageSwitchWithoutSelector() {
        Page page = new Page();
        page.setCommand("switch");

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class, page::build);

        assertEquals(exception.getMessage(),
                "Missing Playwright page switch selector - use one of alias, index, title or url-contains");
    }

    @Test
    void shouldRejectTracePathOnStart() {
        Tracing tracing = new Tracing();
        tracing.setCommand("start");
        tracing.setPath("target/trace.zip");

        expectThrows(CitrusRuntimeException.class, tracing::build);
    }

    @Test
    void shouldRejectStorageStateOutsideCreate() {
        Context context = new Context();
        context.setCommand("switch");
        context.setAlias("admin");
        context.setStorageState("target/state.json");

        expectThrows(CitrusRuntimeException.class, context::build);
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

        assertEquals(credentials.build().getCommand(), CredentialsAction.Command.CREATE);
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

        assertEquals(drop.build().getFileName(), "note.txt");
    }

    @Test
    void shouldRejectUnknownStorageScope() {
        Storage storage = new Storage();

        expectThrows(CitrusRuntimeException.class, () -> storage.setScope("cookie"));
    }
}
