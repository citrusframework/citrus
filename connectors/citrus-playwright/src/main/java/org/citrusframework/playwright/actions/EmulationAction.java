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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.Geolocation;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action for Playwright emulation settings.
 *
 * <p>Viewport, geolocation, and color-scheme settings mutate the active
 * page/context. Locale, timezone, and user-agent settings require a new
 * Playwright context, so the action creates and switches to that context.</p>
 */
public class EmulationAction extends AbstractPlaywrightAction {

    private final Integer viewportWidth;
    private final Integer viewportHeight;
    private final Double latitude;
    private final Double longitude;
    private final Double accuracy;
    private final String colorScheme;
    private final String locale;
    private final String timezone;
    private final String userAgent;
    private final String contextAlias;

    public EmulationAction(Builder builder) {
        super("emulate", builder);
        this.viewportWidth = builder.viewportWidth;
        this.viewportHeight = builder.viewportHeight;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.accuracy = builder.accuracy;
        this.colorScheme = builder.colorScheme;
        this.locale = builder.locale;
        this.timezone = builder.timezone;
        this.userAgent = builder.userAgent;
        this.contextAlias = builder.contextAlias;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        if (viewportWidth != null && viewportHeight != null) {
            browser.getCurrentPage().setViewportSize(viewportWidth, viewportHeight);
        }
        if (latitude != null && longitude != null) {
            Geolocation geolocation = new Geolocation(latitude, longitude);
            if (accuracy != null) {
                geolocation.setAccuracy(accuracy);
            }
            browser.getCurrentContext().setGeolocation(geolocation);
        }
        if (colorScheme != null) {
            browser.getCurrentPage().emulateMedia(new Page.EmulateMediaOptions()
                    .setColorScheme(ColorScheme.valueOf(LocatorResolver.resolve(colorScheme, context).toUpperCase().replace('-', '_'))));
        }
        if (locale != null || timezone != null || userAgent != null) {
            Browser.NewContextOptions options = new Browser.NewContextOptions();
            if (locale != null) {
                options.setLocale(LocatorResolver.resolve(locale, context));
            }
            if (timezone != null) {
                options.setTimezoneId(LocatorResolver.resolve(timezone, context));
            }
            if (userAgent != null) {
                options.setUserAgent(LocatorResolver.resolve(userAgent, context));
            }
            browser.createContext(contextAlias == null ? "emulation" : LocatorResolver.resolve(contextAlias, context), options);
            browser.createPage(contextAlias == null ? "emulation" : LocatorResolver.resolve(contextAlias, context));
        }
    }

    /**
     * Fluent builder for browser emulation settings.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<EmulationAction, Builder> {
        private Integer viewportWidth;
        private Integer viewportHeight;
        private Double latitude;
        private Double longitude;
        private Double accuracy;
        private String colorScheme;
        private String locale;
        private String timezone;
        private String userAgent;
        private String contextAlias;

        /**
         * Sets the current page viewport size.
         *
         * @param width viewport width in pixels
         * @param height viewport height in pixels
         * @return this builder
         */
        public Builder viewport(int width, int height) {
            this.viewportWidth = width;
            this.viewportHeight = height;
            return this;
        }

        /**
         * Sets geolocation on the current context.
         *
         * @param latitude latitude
         * @param longitude longitude
         * @return this builder
         */
        public Builder geolocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            return this;
        }

        /**
         * Sets geolocation accuracy in meters.
         *
         * @param accuracy accuracy in meters
         * @return this builder
         */
        public Builder accuracy(double accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        /**
         * Emulates the page color scheme.
         *
         * @param colorScheme Playwright color scheme, for example {@code dark} or {@code light}
         * @return this builder
         */
        public Builder colorScheme(String colorScheme) {
            this.colorScheme = colorScheme;
            return this;
        }

        /**
         * Sets locale on a newly created context and switches to it.
         *
         * @param locale locale identifier
         * @return this builder
         */
        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        /**
         * Sets timezone on a newly created context and switches to it.
         *
         * @param timezone timezone identifier
         * @return this builder
         */
        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        /**
         * Sets user agent on a newly created context and switches to it.
         *
         * @param userAgent browser user-agent value
         * @return this builder
         */
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Overrides the alias used for a newly created emulation context/page.
         *
         * @param contextAlias alias for the new context and page
         * @return this builder
         */
        public Builder contextAlias(String contextAlias) {
            this.contextAlias = contextAlias;
            return this;
        }

        @Override
        public EmulationAction build() {
            if (viewportWidth == null && latitude == null && colorScheme == null
                    && locale == null && timezone == null && userAgent == null) {
                throw new CitrusRuntimeException("Missing Playwright emulation setting");
            }
            return new EmulationAction(this);
        }
    }
}
