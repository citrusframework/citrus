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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActor;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.DropAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Drops a file or clipboard-like payload onto an element.
 */
@XmlRootElement(name = "drop")
public class Drop extends AbstractPlaywrightAction.Builder<DropAction, Drop> {

    private final DropAction.Builder delegate = new DropAction.Builder();

    private String fileName;
    private String fileMimeType;
    private String value;

    @XmlElement
    public void setElement(Element element) {
        delegate.locator(element.toLocatorSpec());
    }

    @XmlAttribute(name = "file")
    public void setFile(String fileName) {
        this.fileName = fileName;
    }

    @XmlAttribute(name = "content-type")
    public void setContentType(String contentType) {
        this.fileMimeType = contentType;
    }

    @XmlAttribute
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Drop description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Drop actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Drop browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public DropAction build() {
        if (value != null) {
            if (fileName != null) {
                delegate.file(fileName, fileMimeType, value);
            } else {
                delegate.data(fileMimeType, value);
            }
        }
        return delegate.build();
    }
}
