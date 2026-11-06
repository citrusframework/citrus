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
package org.citrusframework.playwright.dsl;

import java.util.List;
import java.util.Map;

import com.microsoft.playwright.options.HarContentPolicy;

import org.citrusframework.TestCase;
import org.citrusframework.playwright.actions.ConsoleAction;
import org.citrusframework.playwright.actions.ContextAction;
import org.citrusframework.playwright.actions.CookieAction;
import org.citrusframework.playwright.actions.DialogAction;
import org.citrusframework.playwright.actions.DownloadAction;
import org.citrusframework.playwright.actions.EmulationAction;
import org.citrusframework.playwright.actions.FrameAction;
import org.citrusframework.playwright.actions.InputAction;
import org.citrusframework.playwright.actions.NetworkAction;
import org.citrusframework.playwright.actions.PageAction;
import org.citrusframework.playwright.actions.PageObjectAction;
import org.citrusframework.playwright.actions.PdfAction;
import org.citrusframework.playwright.actions.PermissionAction;
import org.citrusframework.playwright.actions.StorageAction;
import org.citrusframework.playwright.actions.TracingAction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Assertions shared by the XML and YAML loader tests for {@code playwright-actions.citrus.it}, so
 * both declarative flavours are held to the same mapping.
 */
public final class PlaywrightActionsAssertions {

    public static final long ACTION_COUNT = 22L;

    private PlaywrightActionsAssertions() {
    }

    public static void assertDiagnosticsAndContextActions(TestCase result) {
        assertEquals(ACTION_COUNT, result.getActionCount());

        ConsoleAction capture = (ConsoleAction) result.getTestAction(0);
        assertEquals("playwright:console", capture.getName());
        assertEquals(ConsoleAction.Command.CAPTURE, capture.getCommand());
        assertNotNull(capture.getBrowser());

        ConsoleAction verify = (ConsoleAction) result.getTestAction(1);
        assertEquals(ConsoleAction.Command.VERIFY_CONTAINS, verify.getCommand());
        assertEquals("Started", verify.getText());

        ContextAction context = (ContextAction) result.getTestAction(2);
        assertEquals(ContextAction.Command.CREATE, context.getCommand());
        assertEquals("admin", context.getAlias());
        assertEquals("target/state.json", context.getStorageStatePath());

        CookieAction add = (CookieAction) result.getTestAction(3);
        assertEquals(CookieAction.Command.ADD, add.getCommand());
        assertEquals("session", add.getCookie().getName());

        CookieAction read = (CookieAction) result.getTestAction(4);
        assertEquals(CookieAction.Command.READ, read.getCommand());
        assertEquals("session", read.getCookieName());
        assertEquals("sessionCookie", read.getVariable());

        DialogAction dialog = (DialogAction) result.getTestAction(5);
        assertEquals(DialogAction.Command.VERIFY_CLOSED, dialog.getCommand());
        assertEquals("window.alert('Saved')", dialog.getTriggerScript());

        DownloadAction download = (DownloadAction) result.getTestAction(6);
        assertEquals("#export", download.getTriggerLocator().getSelector());
        assertEquals("target/playwright/report.csv", download.getSaveAs());
        assertEquals("downloadPath", download.getPathVariable());

        EmulationAction emulation = (EmulationAction) result.getTestAction(7);
        assertEquals(Integer.valueOf(1280), emulation.getViewportWidth());
        assertEquals(Integer.valueOf(720), emulation.getViewportHeight());
        assertEquals(52.52, emulation.getLatitude());
        assertEquals(13.40, emulation.getLongitude());
        assertEquals("dark", emulation.getColorScheme());
        assertEquals("de-DE", emulation.getLocale());
    }

    public static void assertPageAndNetworkActions(TestCase result) {
        InputAction enter = (InputAction) result.getTestAction(8);
        assertEquals(InputAction.Command.FILL, enter.getCommand());
        assertEquals("${username}", enter.getValue());

        FrameAction frame = (FrameAction) result.getTestAction(9);
        assertEquals(FrameAction.Command.FILL, frame.getCommand());
        assertEquals("#checkout", frame.getFrameSelector());
        assertEquals("#card", frame.getLocator().getSelector());
        assertEquals("4111", frame.getValue());

        NetworkAction fulfill = (NetworkAction) result.getTestAction(10);
        assertEquals(NetworkAction.Command.ROUTE_FULFILL, fulfill.getCommand());
        assertEquals("**/api/users", fulfill.getUrlPattern());
        assertEquals("[]", fulfill.getBody());
        assertEquals("application/json", fulfill.getContentType());
        assertEquals(Integer.valueOf(201), fulfill.getStatus());
        assertEquals(Map.of("X-Test", "1"), fulfill.getHeaders());

        NetworkAction waitForResponse = (NetworkAction) result.getTestAction(11);
        assertEquals(NetworkAction.Command.WAIT_FOR_RESPONSE, waitForResponse.getCommand());
        assertEquals("/api/users", waitForResponse.getResponseUrlContains());
        assertEquals(Integer.valueOf(200), waitForResponse.getResponseStatus(),
                "status declared on wait-for-response must match the response, not a fulfilled route");
        assertEquals("#load", waitForResponse.getTriggerLocator().getSelector());
        assertEquals("response", waitForResponse.getVariable());

        PageAction create = (PageAction) result.getTestAction(12);
        assertEquals(PageAction.Command.CREATE, create.getCommand());
        assertEquals("popup", create.getAlias());
        assertEquals("admin", create.getContextAlias());

        PageAction switchToIndex = (PageAction) result.getTestAction(13);
        assertEquals(PageAction.Command.SWITCH_INDEX, switchToIndex.getCommand());
        assertEquals(Integer.valueOf(1), switchToIndex.getIndex());

        PageObjectAction pageObject = (PageObjectAction) result.getTestAction(14);
        assertEquals("playwright:page-object", pageObject.getName());
        assertEquals(PageObjectFixtures.FixturePage.class, pageObject.getPageType());
        assertEquals("open", pageObject.getMethod());
        assertEquals(PageObjectFixtures.FixtureValidator.class, pageObject.getValidatorType());
    }

    public static void assertOutputAndTracingActions(TestCase result) {
        PdfAction pdf = (PdfAction) result.getTestAction(15);
        assertEquals("target/playwright/page.pdf", pdf.getPath());
        assertFalse(pdf.isPrintBackground());

        PermissionAction permissions = (PermissionAction) result.getTestAction(16);
        assertEquals(PermissionAction.Command.GRANT, permissions.getCommand());
        assertEquals(List.of("geolocation", "clipboard-read"), permissions.getPermissions());

        StorageAction storage = (StorageAction) result.getTestAction(17);
        assertEquals(StorageAction.Scope.SESSION, storage.getScope());
        assertEquals(StorageAction.Command.SAVE_STATE, storage.getCommand());
        assertEquals("target/state.json", storage.getPath());
        assertEquals(Boolean.TRUE, storage.getOpfs());

        TracingAction start = (TracingAction) result.getTestAction(18);
        assertEquals(TracingAction.Command.START, start.getCommand());
        assertEquals(Boolean.TRUE, start.getAriaSnapshots());
        assertEquals(Boolean.TRUE, start.getScreenSnapshots());

        TracingAction startHar = (TracingAction) result.getTestAction(19);
        assertEquals(TracingAction.Command.START_HAR, startHar.getCommand());
        assertEquals("target/playwright/session.har", startHar.getHarPath());
        assertEquals(HarContentPolicy.EMBED, startHar.getHarContent());
        assertEquals("**/api/**", startHar.getHarUrlFilter());

        TracingAction stop = (TracingAction) result.getTestAction(20);
        assertEquals(TracingAction.Command.STOP, stop.getCommand());
        assertEquals("target/playwright/trace.zip", stop.getPath());
        assertEquals("tracePath", stop.getVariable());
        assertNull(stop.getHarPath());

        InputAction select = (InputAction) result.getTestAction(21);
        assertEquals(InputAction.Command.SELECT, select.getCommand());
        assertEquals(List.of("male", "female", "New York"), select.getValues());
    }
}
