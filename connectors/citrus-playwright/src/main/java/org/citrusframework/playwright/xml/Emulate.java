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
import org.citrusframework.playwright.actions.EmulationAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Emulates viewport, geolocation, color scheme, locale, timezone or user agent.
 */
@XmlRootElement(name = "emulate")
public class Emulate extends AbstractPlaywrightAction.Builder<EmulationAction, Emulate> {

    private final EmulationAction.Builder delegate = new EmulationAction.Builder();

    private Integer width;
    private Integer height;
    private Double latitude;
    private Double longitude;

    @XmlAttribute
    public void setWidth(Integer width) {
        this.width = width;
    }

    @XmlAttribute
    public void setHeight(Integer height) {
        this.height = height;
    }

    @XmlAttribute
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @XmlAttribute
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @XmlAttribute
    public void setAccuracy(Double accuracy) {
        delegate.accuracy(accuracy);
    }

    @XmlAttribute(name = "color-scheme")
    public void setColorScheme(String colorScheme) {
        delegate.colorScheme(colorScheme);
    }

    @XmlAttribute
    public void setLocale(String locale) {
        delegate.locale(locale);
    }

    @XmlAttribute
    public void setTimezone(String timezone) {
        delegate.timezone(timezone);
    }

    @XmlAttribute(name = "user-agent")
    public void setUserAgent(String userAgent) {
        delegate.userAgent(userAgent);
    }

    @XmlAttribute
    public void setContext(String contextAlias) {
        delegate.contextAlias(contextAlias);
    }

    @Override
    public Emulate description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Emulate actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Emulate browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public EmulationAction build() {
        if (width != null || height != null) {
            if (width == null || height == null) {
                throw new CitrusRuntimeException("Missing Playwright viewport width or height");
            }
            delegate.viewport(width, height);
        }

        if (latitude != null || longitude != null) {
            if (latitude == null || longitude == null) {
                throw new CitrusRuntimeException("Missing Playwright geolocation latitude or longitude");
            }
            delegate.geolocation(latitude, longitude);
        }

        return delegate.build();
    }
}
