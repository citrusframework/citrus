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
import org.citrusframework.playwright.actions.EmulationAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Emulates viewport, geolocation, color scheme, locale, timezone or user agent.
 */
public class Emulate extends AbstractPlaywrightAction.Builder<EmulationAction, Emulate> {

    private final EmulationAction.Builder delegate = new EmulationAction.Builder();

    private Integer width;
    private Integer height;
    private Double latitude;
    private Double longitude;

    @SchemaProperty(description = "Viewport width in pixels.")
    public void setWidth(Integer width) {
        this.width = width;
    }

    @SchemaProperty(description = "Viewport height in pixels.")
    public void setHeight(Integer height) {
        this.height = height;
    }

    @SchemaProperty(description = "Geolocation latitude.")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @SchemaProperty(description = "Geolocation longitude.")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @SchemaProperty(description = "Geolocation accuracy in meters.")
    public void setAccuracy(Double accuracy) {
        delegate.accuracy(accuracy);
    }

    @SchemaProperty(description = "Preferred color scheme to emulate.")
    public void setColorScheme(String colorScheme) {
        delegate.colorScheme(colorScheme);
    }

    @SchemaProperty(description = "Locale to emulate.")
    public void setLocale(String locale) {
        delegate.locale(locale);
    }

    @SchemaProperty(description = "Timezone to emulate.")
    public void setTimezone(String timezone) {
        delegate.timezone(timezone);
    }

    @SchemaProperty(description = "User agent string to emulate.")
    public void setUserAgent(String userAgent) {
        delegate.userAgent(userAgent);
    }

    @SchemaProperty(description = "Browser context alias to emulate.")
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
