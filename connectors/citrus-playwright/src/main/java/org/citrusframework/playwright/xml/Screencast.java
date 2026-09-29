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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.ScreencastAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Records and annotates a screencast of the current page.
 *
 * <p>The real-time frame callback of the Java DSL has no declarative equivalent and is
 * deliberately not exposed here.</p>
 */
@XmlRootElement(name = "screencast")
public class Screencast extends AbstractPlaywrightAction.Builder<ScreencastAction, Screencast> {

    private final ScreencastAction.Builder delegate = new ScreencastAction.Builder();

    private String command;
    private String path;
    private String title;
    private String html;
    private Integer sizeWidth;
    private Integer sizeHeight;

    @XmlAttribute
    public void setPath(String path) {
        this.path = path;
    }

    @XmlAttribute
    public void setTitle(String title) {
        this.title = title;
    }

    @XmlAttribute
    public void setHtml(String html) {
        this.html = html;
    }

    @XmlAttribute(name = "size-width")
    public void setSizeWidth(Integer sizeWidth) {
        this.sizeWidth = sizeWidth;
    }

    @XmlAttribute(name = "size-height")
    public void setSizeHeight(Integer sizeHeight) {
        this.sizeHeight = sizeHeight;
    }

    @XmlAttribute
    public void setCommand(String command) {
        this.command = command;
    }

    @XmlAttribute(name = "chapter-description")
    public void setChapterDescription(String description) {
        delegate.chapterDescription(description);
    }

    @XmlAttribute
    public void setPosition(String position) {
        delegate.position(position);
    }

    @XmlAttribute(name = "font-size")
    public void setFontSize(Integer fontSize) {
        delegate.fontSize(fontSize);
    }

    @XmlAttribute
    public void setDuration(Double duration) {
        delegate.duration(duration);
    }

    @XmlAttribute
    public void setQuality(Integer quality) {
        delegate.quality(quality);
    }

    @XmlAttribute
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @Override
    public Screencast description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Screencast actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Screencast browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public ScreencastAction build() {
        if (sizeWidth != null || sizeHeight != null) {
            if (sizeWidth == null || sizeHeight == null) {
                throw new CitrusRuntimeException(
                        "Incomplete Playwright screencast size - set both size-width and size-height");
            }
            delegate.size(sizeWidth, sizeHeight);
        }
        switch (DslCommands.normalize(command)) {
            case "start" -> delegate.start(path);
            case "stop" -> delegate.stop();
            case "show-actions" -> delegate.showActions();
            case "hide-actions" -> delegate.hideActions();
            case "show-chapter" -> delegate.showChapter(title);
            case "show-overlay" -> delegate.showOverlay(html);
            case "show-overlays" -> delegate.showOverlays();
            case "hide-overlays" -> delegate.hideOverlays();
            default -> throw DslCommands.unsupported("screencast", command);
        }
        return delegate.build();
    }
}
