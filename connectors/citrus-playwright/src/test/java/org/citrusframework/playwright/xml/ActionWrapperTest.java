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
package org.citrusframework.playwright.xml;

import org.citrusframework.exceptions.CitrusRuntimeException;
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
        pageObject.setArguments("first, second third");

        assertEquals(pageObject.build().getArguments(), new String[] { "first", "second", "third" });
    }

    @Test
    void shouldRejectUnknownStorageScope() {
        Storage storage = new Storage();

        expectThrows(CitrusRuntimeException.class, () -> storage.setScope("cookie"));
    }
}
