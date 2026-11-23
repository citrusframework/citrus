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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import com.microsoft.playwright.Screencast;
import com.microsoft.playwright.ScreencastFrame;
import com.microsoft.playwright.options.AnnotatePosition;
import com.microsoft.playwright.options.ScreencastActionStyle;
import com.microsoft.playwright.options.ScreencastCursor;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ScreencastActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;
    private Screencast screencast;

    @BeforeEach
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        screencast = mock(Screencast.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().screencast()).thenReturn(screencast);
    }

    @AfterEach
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
    @SuppressWarnings("deprecation")
    void shouldShowActionAnnotations() {
        new ScreencastAction.Builder().showActions().position("top-right").fontSize(18)
                .build().execute(context);

        ArgumentCaptor<Screencast.ShowActionsOptions> captor =
                ArgumentCaptor.forClass(Screencast.ShowActionsOptions.class);
        verify(screencast).showActions(captor.capture());
        assertEquals(AnnotatePosition.TOP_RIGHT, captor.getValue().position);
        assertEquals("font-size: 18px", captor.getValue().style.title);
    }

    @Test
    void shouldStartRecordingWithFps() {
        ScreencastAction action = new ScreencastAction.Builder()
                .start("target/playwright/run.webm")
                .fps(24)
                .build();
        assertEquals(Integer.valueOf(24), action.getFps());

        action.execute(context);

        ArgumentCaptor<Screencast.StartOptions> captor = ArgumentCaptor.forClass(Screencast.StartOptions.class);
        verify(screencast).start(captor.capture());
        assertEquals(Integer.valueOf(24), captor.getValue().fps);
    }

    @Test
    void shouldShowActionAnnotationsWithStyleAndCursor() {
        context.setVariable("dotColor", "#ff3b30");

        ScreencastAction action = new ScreencastAction.Builder()
                .showActions()
                .position("bottom-left")
                .cursor("pointer")
                .stylePoint("background: ${dotColor}")
                .styleHighlight("border: 2px solid #007aff")
                .styleTitle("font-size: 20px; color: #ffffff")
                .build();
        assertEquals(ScreencastCursor.POINTER, action.getCursor());
        assertEquals("background: ${dotColor}", action.getStylePoint());
        assertEquals("border: 2px solid #007aff", action.getStyleHighlight());
        assertEquals("font-size: 20px; color: #ffffff", action.getStyleTitle());

        action.execute(context);

        ArgumentCaptor<Screencast.ShowActionsOptions> captor =
                ArgumentCaptor.forClass(Screencast.ShowActionsOptions.class);
        verify(screencast).showActions(captor.capture());
        assertEquals(AnnotatePosition.BOTTOM_LEFT, captor.getValue().position);
        assertEquals(ScreencastCursor.POINTER, captor.getValue().cursor);
        assertEquals("background: #ff3b30", captor.getValue().style.point);
        assertEquals("border: 2px solid #007aff", captor.getValue().style.highlight);
        assertEquals("font-size: 20px; color: #ffffff", captor.getValue().style.title);
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldSupportStyleObjectAndPrecedenceOverDeprecatedFontSize() {
        context.setVariable("alpha", "0.85");
        ScreencastActionStyle baseStyle = new ScreencastActionStyle()
                .setPoint("opacity: ${alpha}")
                .setHighlight("outline: 1px solid green")
                .setTitle("font-size: 16px");

        new ScreencastAction.Builder()
                .showActions()
                .cursor(ScreencastCursor.NONE)
                .style(baseStyle)
                .styleTitle("font-size: 22px")
                .fontSize(14)
                .build()
                .execute(context);

        ArgumentCaptor<Screencast.ShowActionsOptions> captor =
                ArgumentCaptor.forClass(Screencast.ShowActionsOptions.class);
        verify(screencast).showActions(captor.capture());
        assertEquals(ScreencastCursor.NONE, captor.getValue().cursor);
        assertEquals("opacity: 0.85", captor.getValue().style.point);
        assertEquals("outline: 1px solid green", captor.getValue().style.highlight);
        assertEquals("font-size: 22px", captor.getValue().style.title);
        assertEquals("opacity: ${alpha}", baseStyle.point, "caller style instance must not be mutated");
    }

    @Test
    void shouldRejectInvalidFpsStyleAndCursorOptions() {
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().stop().fps(24).build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().start("target/playwright/run.webm").fps(0).build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().start("target/playwright/run.webm").fps(-10).build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().start("target/playwright/run.webm")
                        .styleTitle("font-size: 18px").build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().stop()
                        .style(new ScreencastActionStyle().setPoint("color: red")).build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().stop().cursor("none").build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().showActions().cursor("crosshair").build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().showActions().cursor("  ").build());
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
        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class,
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
    void shouldShowCustomOverlay() {
        new ScreencastAction.Builder().showOverlay("<div>Step 3</div>").build().execute(context);

        verify(screencast).showOverlay(eq("<div>Step 3</div>"));
    }

    @Test
    void shouldResolveVariablesInOverlayHtml() {
        context.setVariable("step", "checkout");

        new ScreencastAction.Builder().showOverlay("<div>${step}</div>").build().execute(context);

        verify(screencast).showOverlay(eq("<div>checkout</div>"));
    }

    @Test
    void shouldShowAndHideOverlays() {
        new ScreencastAction.Builder().showOverlays().build().execute(context);
        new ScreencastAction.Builder().hideOverlays().build().execute(context);

        verify(screencast).showOverlays();
        verify(screencast).hideOverlays();
    }

    @Test
    void shouldStartRecordingWithSize() {
        new ScreencastAction.Builder().start("target/playwright/run.webm").size(1280, 720)
                .build().execute(context);

        ArgumentCaptor<Screencast.StartOptions> captor = ArgumentCaptor.forClass(Screencast.StartOptions.class);
        verify(screencast).start(captor.capture());
        assertEquals(1280, captor.getValue().size.width);
        assertEquals(720, captor.getValue().size.height);
    }

    @Test
    void shouldStartRecordingWithoutSizeByDefault() {
        new ScreencastAction.Builder().start("target/playwright/run.webm").build().execute(context);

        ArgumentCaptor<Screencast.StartOptions> captor = ArgumentCaptor.forClass(Screencast.StartOptions.class);
        verify(screencast).start(captor.capture());
        assertEquals(null, captor.getValue().size);
    }

    @Test
    void shouldFailFastWhenOverlayHtmlMissing() {
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().showOverlay("  ").build());
    }

    @Test
    void shouldFailFastWhenSizeWithoutStart() {
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().stop().size(1280, 720).build());
    }

    @Test
    void shouldFailFastWhenSizeNotPositive() {
        assertThrows(CitrusRuntimeException.class,
                () -> new ScreencastAction.Builder().start("target/playwright/run.webm").size(0, 720).build());
    }

    @Test
    void shouldFailFastWhenSizeIncomplete() throws Exception {
        ScreencastAction.Builder builder = new ScreencastAction.Builder()
                .start("target/playwright/run.webm");
        java.lang.reflect.Field field = ScreencastAction.Builder.class.getDeclaredField("sizeWidth");
        field.setAccessible(true);
        field.set(builder, Integer.valueOf(1280));

        assertThrows(CitrusRuntimeException.class, builder::build);
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        assertThrows(CitrusRuntimeException.class, () -> new ScreencastAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenStartPathMissing() {
        assertThrows(CitrusRuntimeException.class, () -> new ScreencastAction.Builder().start("").build());
    }

    @Test
    void shouldNotStartRecordingForAnnotationCommands() {
        new ScreencastAction.Builder().showChapter("Done").build().execute(context);

        verify(screencast, org.mockito.Mockito.never()).start(any(Screencast.StartOptions.class));
    }
}
