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

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.DropAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Drops a file or clipboard-like payload onto an element.
 */
public class Drop extends AbstractPlaywrightAction.Builder<DropAction, Drop> {

    private final DropAction.Builder delegate = new DropAction.Builder();

    private String fileName;
    private String fileMimeType;
    private String value;
    private Double timeout;
    private Double positionX;
    private Double positionY;

    @SchemaProperty
    public void setElement(Element element) {
        delegate.locator(element.toLocatorSpec());
    }

    @SchemaProperty
    public void setFile(String fileName) {
        this.fileName = fileName;
    }

    @SchemaProperty
    public void setContentType(String contentType) {
        this.fileMimeType = contentType;
    }

    @SchemaProperty
    public void setValue(String value) {
        this.value = value;
    }

    @SchemaProperty
    public void setTimeout(Double timeout) {
        this.timeout = timeout;
    }

    @SchemaProperty
    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    @SchemaProperty
    public void setPositionY(Double positionY) {
        this.positionY = positionY;
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
        if (timeout != null) {
            delegate.timeout(timeout);
        }
        if (positionX != null || positionY != null) {
            if (positionX == null || positionY == null) {
                throw new CitrusRuntimeException(
                        "Incomplete Playwright drop position - set both position-x and position-y");
            }
            delegate.position(positionX, positionY);
        }
        return delegate.build();
    }
}
