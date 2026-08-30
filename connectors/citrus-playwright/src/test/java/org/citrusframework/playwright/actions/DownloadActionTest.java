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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.model.DownloadMetadata;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;

class DownloadActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldTriggerClickAndRecordMetadata() {
        Locator trigger = mock(Locator.class);
        when(browser.page().locator("#export")).thenReturn(trigger);
        Download download = browser.download();
        when(browser.page().waitForDownload(any())).thenAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return download;
        });
        when(download.path()).thenReturn(Path.of("/tmp/export.csv"));
        when(download.suggestedFilename()).thenReturn("export.csv");
        when(download.url()).thenReturn("http://localhost/export");
        when(download.failure()).thenReturn(null);

        new DownloadAction.Builder().click("#export").build().execute(context);

        verify(trigger).click();
        DownloadMetadata metadata = browser.getLatestDownloadMetadata().orElseThrow();
        assertEquals("/tmp/export.csv", metadata.path());
        assertEquals("export.csv", metadata.suggestedFilename());
    }

    @Test
    void shouldSaveDownloadAndExposePathAndFilenameVariables() {
        when(browser.page().locator("#export")).thenReturn(mock(Locator.class));
        when(browser.page().waitForDownload(any())).thenAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return browser.download();
        });
        when(browser.download().path()).thenReturn(Path.of("/tmp/export.csv"));
        when(browser.download().suggestedFilename()).thenReturn("export.csv");
        when(browser.download().url()).thenReturn("http://localhost/export");
        when(browser.download().failure()).thenReturn(null);

        new DownloadAction.Builder()
                .click("#export")
                .saveAs("target/out/export.csv")
                .pathVariable("downloadPath")
                .filenameVariable("downloadName")
                .build()
                .execute(context);

        assertEquals(Path.of("target/out/export.csv").toString(), context.getVariable("downloadPath"));
        assertEquals("export.csv", context.getVariable("downloadName"));
        verify(browser.download()).saveAs(Path.of("target/out/export.csv"));
    }

    @Test
    void shouldTriggerViaScriptAndRecordMetadata() {
        Download download = browser.download();
        when(browser.page().waitForDownload(any())).thenAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return download;
        });
        when(download.path()).thenReturn(Path.of("/tmp/export.bin"));
        when(download.suggestedFilename()).thenReturn("export.bin");
        when(download.url()).thenReturn("http://localhost/export");
        when(download.failure()).thenReturn(null);

        new DownloadAction.Builder().triggerScript("() => fetchExport()").build().execute(context);

        verify(browser.page()).evaluate("() => fetchExport()");
        assertTrue(browser.getLatestDownloadMetadata().isPresent());
    }

    @Test
    void shouldFailFastWhenNoTriggerConfigured() {
        expectThrows(CitrusRuntimeException.class, () -> new DownloadAction.Builder().build());
    }
}
