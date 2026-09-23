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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Assertions shared by the XML and YAML loader tests for {@code playwright-actions.citrus.it}, so
 * both declarative flavours are held to the same mapping.
 */
public final class PlaywrightActionsAssertions {

    public static final long ACTION_COUNT = 22L;

    private PlaywrightActionsAssertions() {
    }

    public static void assertDiagnosticsAndContextActions(TestCase result) {
        assertEquals(result.getActionCount(), ACTION_COUNT);

        ConsoleAction capture = (ConsoleAction) result.getTestAction(0);
        assertEquals(capture.getName(), "playwright:console");
        assertEquals(capture.getCommand(), ConsoleAction.Command.CAPTURE);
        assertNotNull(capture.getBrowser());

        ConsoleAction verify = (ConsoleAction) result.getTestAction(1);
        assertEquals(verify.getCommand(), ConsoleAction.Command.VERIFY_CONTAINS);
        assertEquals(verify.getText(), "Started");

        ContextAction context = (ContextAction) result.getTestAction(2);
        assertEquals(context.getCommand(), ContextAction.Command.CREATE);
        assertEquals(context.getAlias(), "admin");
        assertEquals(context.getOptions().storageStatePath.toString(), "target/state.json");

        CookieAction add = (CookieAction) result.getTestAction(3);
        assertEquals(add.getCommand(), CookieAction.Command.ADD);
        assertEquals(add.getCookie().getName(), "session");

        CookieAction read = (CookieAction) result.getTestAction(4);
        assertEquals(read.getCommand(), CookieAction.Command.READ);
        assertEquals(read.getCookieName(), "session");
        assertEquals(read.getVariable(), "sessionCookie");

        DialogAction dialog = (DialogAction) result.getTestAction(5);
        assertEquals(dialog.getCommand(), DialogAction.Command.VERIFY_CLOSED);
        assertEquals(dialog.getTriggerScript(), "window.alert('Saved')");

        DownloadAction download = (DownloadAction) result.getTestAction(6);
        assertEquals(download.getTriggerLocator().getSelector(), "#export");
        assertEquals(download.getSaveAs(), "target/playwright/report.csv");
        assertEquals(download.getPathVariable(), "downloadPath");

        EmulationAction emulation = (EmulationAction) result.getTestAction(7);
        assertEquals(emulation.getViewportWidth(), Integer.valueOf(1280));
        assertEquals(emulation.getViewportHeight(), Integer.valueOf(720));
        assertEquals(emulation.getLatitude(), 52.52);
        assertEquals(emulation.getLongitude(), 13.40);
        assertEquals(emulation.getColorScheme(), "dark");
        assertEquals(emulation.getLocale(), "de-DE");
    }

    public static void assertPageAndNetworkActions(TestCase result) {
        InputAction enter = (InputAction) result.getTestAction(8);
        assertEquals(enter.getCommand(), InputAction.Command.FILL);
        assertEquals(enter.getValue(), "${username}");

        FrameAction frame = (FrameAction) result.getTestAction(9);
        assertEquals(frame.getCommand(), FrameAction.Command.FILL);
        assertEquals(frame.getFrameSelector(), "#checkout");
        assertEquals(frame.getLocator().getSelector(), "#card");
        assertEquals(frame.getValue(), "4111");

        NetworkAction fulfill = (NetworkAction) result.getTestAction(10);
        assertEquals(fulfill.getCommand(), NetworkAction.Command.ROUTE_FULFILL);
        assertEquals(fulfill.getUrlPattern(), "**/api/users");
        assertEquals(fulfill.getBody(), "[]");
        assertEquals(fulfill.getContentType(), "application/json");
        assertEquals(fulfill.getStatus(), Integer.valueOf(201));
        assertEquals(fulfill.getHeaders(), Map.of("X-Test", "1"));

        NetworkAction waitForResponse = (NetworkAction) result.getTestAction(11);
        assertEquals(waitForResponse.getCommand(), NetworkAction.Command.WAIT_FOR_RESPONSE);
        assertEquals(waitForResponse.getResponseUrlContains(), "/api/users");
        assertEquals(waitForResponse.getResponseStatus(), Integer.valueOf(200),
                "status declared on wait-for-response must match the response, not a fulfilled route");
        assertEquals(waitForResponse.getTriggerLocator().getSelector(), "#load");
        assertEquals(waitForResponse.getVariable(), "response");

        PageAction create = (PageAction) result.getTestAction(12);
        assertEquals(create.getCommand(), PageAction.Command.CREATE);
        assertEquals(create.getAlias(), "popup");
        assertEquals(create.getContextAlias(), "admin");

        PageAction switchToIndex = (PageAction) result.getTestAction(13);
        assertEquals(switchToIndex.getCommand(), PageAction.Command.SWITCH_INDEX);
        assertEquals(switchToIndex.getIndex(), Integer.valueOf(1));

        PageObjectAction pageObject = (PageObjectAction) result.getTestAction(14);
        assertEquals(pageObject.getName(), "playwright:page-object");
        assertEquals(pageObject.getPageType(), PageObjectFixtures.FixturePage.class);
        assertEquals(pageObject.getMethod(), "open");
        assertEquals(pageObject.getValidatorType(), PageObjectFixtures.FixtureValidator.class);
    }

    public static void assertOutputAndTracingActions(TestCase result) {
        PdfAction pdf = (PdfAction) result.getTestAction(15);
        assertEquals(pdf.getPath(), "target/playwright/page.pdf");
        assertFalse(pdf.isPrintBackground());

        PermissionAction permissions = (PermissionAction) result.getTestAction(16);
        assertEquals(permissions.getCommand(), PermissionAction.Command.GRANT);
        assertEquals(permissions.getPermissions(), List.of("geolocation", "clipboard-read"));

        StorageAction storage = (StorageAction) result.getTestAction(17);
        assertEquals(storage.getScope(), StorageAction.Scope.SESSION);
        assertEquals(storage.getCommand(), StorageAction.Command.SAVE_STATE);
        assertEquals(storage.getPath(), "target/state.json");
        assertEquals(storage.getOpfs(), Boolean.TRUE);

        TracingAction start = (TracingAction) result.getTestAction(18);
        assertEquals(start.getCommand(), TracingAction.Command.START);
        assertEquals(start.getAriaSnapshots(), Boolean.TRUE);
        assertEquals(start.getScreenSnapshots(), Boolean.TRUE);

        TracingAction startHar = (TracingAction) result.getTestAction(19);
        assertEquals(startHar.getCommand(), TracingAction.Command.START_HAR);
        assertEquals(startHar.getHarPath(), "target/playwright/session.har");
        assertEquals(startHar.getHarContent(), HarContentPolicy.EMBED);
        assertEquals(startHar.getHarUrlFilter(), "**/api/**");

        TracingAction stop = (TracingAction) result.getTestAction(20);
        assertEquals(stop.getCommand(), TracingAction.Command.STOP);
        assertEquals(stop.getPath(), "target/playwright/trace.zip");
        assertEquals(stop.getVariable(), "tracePath");
        assertTrue(stop.getHarPath() == null);

        InputAction select = (InputAction) result.getTestAction(21);
        assertEquals(select.getCommand(), InputAction.Command.SELECT);
        assertEquals(select.getValues(), List.of("male", "female", "other"));
    }
}
