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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import com.microsoft.playwright.Screencast;
import com.microsoft.playwright.ScreencastFrame;
import com.microsoft.playwright.options.AnnotatePosition;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

class ScreencastActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;
    private Screencast screencast;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        screencast = mock(Screencast.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().screencast()).thenReturn(screencast);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldStartRecordingToPath() {
        new ScreencastAction.Builder().start("target/playwright/run.webm").build().execute(context);

        ArgumentCaptor<Screencast.StartOptions> captor = ArgumentCaptor.forClass(Screencast.StartOptions.class);
        verify(screencast).start(captor.capture());
        assertEquals(Path.of("target/playwright/run.webm"), captor.getValue().path);
    }

    @Test
    void shouldResolveVariablesInRecordingPath() {
        context.setVariable("run", "checkout");

        new ScreencastAction.Builder().start("target/playwright/${run}.webm").build().execute(context);

        ArgumentCaptor<Screencast.StartOptions> captor = ArgumentCaptor.forClass(Screencast.StartOptions.class);
        verify(screencast).start(captor.capture());
        assertEquals(Path.of("target/playwright/checkout.webm"), captor.getValue().path);
    }

    @Test
    void shouldStopRecording() {
        new ScreencastAction.Builder().stop().build().execute(context);

        verify(screencast).stop();
    }

    @Test
    void shouldShowActionAnnotations() {
        new ScreencastAction.Builder().showActions().position("top-right").fontSize(18)
                .build().execute(context);

        ArgumentCaptor<Screencast.ShowActionsOptions> captor =
                ArgumentCaptor.forClass(Screencast.ShowActionsOptions.class);
        verify(screencast).showActions(captor.capture());
        assertEquals(AnnotatePosition.TOP_RIGHT, captor.getValue().position);
        assertEquals(Integer.valueOf(18), captor.getValue().fontSize);
    }

    @Test
    void shouldShowChapterOverlay() {
        new ScreencastAction.Builder().showChapter("Checkout").chapterDescription("Applies coupon")
                .duration(1000).build().execute(context);

        ArgumentCaptor<Screencast.ShowChapterOptions> captor =
                ArgumentCaptor.forClass(Screencast.ShowChapterOptions.class);
        verify(screencast).showChapter(eq("Checkout"), captor.capture());
        assertEquals("Applies coupon", captor.getValue().description);
    }

    @Test
    void shouldStreamFramesToJavaCallback() {
        AtomicReference<byte[]> received = new AtomicReference<>();

        new ScreencastAction.Builder().start("target/playwright/run.webm")
                .onFrame(frame -> received.set(frame.data())).build().execute(context);

        ArgumentCaptor<Screencast.StartOptions> captor = ArgumentCaptor.forClass(Screencast.StartOptions.class);
        verify(screencast).start(captor.capture());

        ScreencastFrame frame = mock(ScreencastFrame.class);
        when(frame.data()).thenReturn(new byte[]{1, 2, 3});
        captor.getValue().onFrame.accept(frame);

        assertEquals(3, received.get().length);
    }

    @Test
    void shouldRejectUnsupportedAnnotationPosition() {
        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().showActions().position("middle").build());

        assertTrue(exception.getMessage().contains("middle"), exception.getMessage());
    }

    @Test
    void shouldKeepActionDescriptionSeparateFromChapterDescription() {
        ScreencastAction action = new ScreencastAction.Builder()
                .showChapter("Checkout")
                .chapterDescription("Applies coupon")
                .description("citrus action description")
                .build();

        assertEquals("citrus action description", action.getDescription());
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new ScreencastAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenStartPathMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new ScreencastAction.Builder().start("").build());
    }

    @Test
    void shouldNotStartRecordingForAnnotationCommands() {
        new ScreencastAction.Builder().showChapter("Done").build().execute(context);

        verify(screencast, org.mockito.Mockito.never()).start(any(Screencast.StartOptions.class));
    }
}
