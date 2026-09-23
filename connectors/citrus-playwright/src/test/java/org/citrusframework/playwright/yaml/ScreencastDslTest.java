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

import java.lang.reflect.Method;
import java.util.Arrays;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.ScreencastAction;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.expectThrows;

/**
 * Covers how the YAML screencast wrapper maps onto the action builder, including the deliberate
 * omission of the real-time frame callback.
 */
class ScreencastDslTest {

    @Test
    void shouldStartRecordingFromDeclaredPath() {
        Screencast screencast = new Screencast();
        screencast.setPath("target/playwright/run.webm");
        screencast.setCommand("start");

        assertEquals(ScreencastAction.Command.START, screencast.build().getCommand());
    }

    @Test
    void shouldStartRecordingWhateverTheDeclarationOrder() {
        Screencast screencast = new Screencast();
        screencast.setCommand("start");
        screencast.setPath("target/playwright/run.webm");

        assertEquals(screencast.build().getCommand(), ScreencastAction.Command.START);
    }

    @Test
    void shouldShowChapterFromDeclaredTitle() {
        Screencast screencast = new Screencast();
        screencast.setTitle("Checkout");
        screencast.setChapterDescription("Applies coupon");
        screencast.setCommand("show-chapter");

        assertEquals(ScreencastAction.Command.SHOW_CHAPTER, screencast.build().getCommand());
    }

    @Test
    void shouldAcceptUnderscoreCommandSpelling() {
        Screencast screencast = new Screencast();
        screencast.setCommand("show_actions");

        assertEquals(ScreencastAction.Command.SHOW_ACTIONS, screencast.build().getCommand());
    }

    @Test
    void shouldRejectUnsupportedCommand() {
        Screencast screencast = new Screencast();

        screencast.setCommand("rewind");

        expectThrows(CitrusRuntimeException.class, screencast::build);
    }

    @Test
    void shouldNotExposeRealTimeFrameCallback() {
        boolean exposesOnFrame = Arrays.stream(Screencast.class.getMethods())
                .map(Method::getName)
                .anyMatch(name -> name.toLowerCase().contains("onframe"));

        assertFalse(exposesOnFrame,
                "onFrame takes a callback and has no declarative representation - it must stay Java DSL only");
    }
}
