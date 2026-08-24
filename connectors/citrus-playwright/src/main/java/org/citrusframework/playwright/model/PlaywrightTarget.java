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

package org.citrusframework.playwright.model;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Reusable, labeled Playwright locator target for Citrus tests.
 *
 * <p>The target is intentionally thin: it builds on {@link LocatorSpec} and can
 * be passed back into existing element actions through {@link #toLocatorSpec()}.
 * It adds human-readable labels, simple parameter substitution, frame paths,
 * and container/child composition without replacing the existing locator model.</p>
 */
public class PlaywrightTarget {

    private final String label;
    private final LocatorSpec locator;
    private final List<String> framePath;
    private final PlaywrightTarget container;

    private PlaywrightTarget(String label, LocatorSpec locator, List<String> framePath, PlaywrightTarget container) {
        this.label = label;
        this.locator = locator;
        this.framePath = List.copyOf(framePath);
        this.container = container;
    }

    /**
     * Starts a target builder with a human-readable label.
     *
     * @param label target label used in diagnostics and {@link #toString()}
     * @return target builder
     */
    public static Builder the(String label) {
        return new Builder(label, List.of());
    }

    /**
     * Creates a target from an existing locator specification.
     *
     * @param label target label
     * @param locator locator specification
     * @return target
     */
    public static PlaywrightTarget of(String label, LocatorSpec locator) {
        return new PlaywrightTarget(label, locator, List.of(), null);
    }

    /**
     * Returns the target label.
     *
     * @return target label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the underlying locator specification.
     *
     * @return locator specification
     */
    public LocatorSpec getLocator() {
        return locator;
    }

    /**
     * Returns frame selectors that must be traversed before resolving the locator.
     *
     * @return immutable frame path
     */
    public List<String> getFramePath() {
        return framePath;
    }

    /**
     * Returns the containing target when this target is nested.
     *
     * @return containing target, or {@code null}
     */
    public PlaywrightTarget getContainer() {
        return container;
    }

    /**
     * Creates a parameterized copy by replacing {@code {0}}, {@code {1}}, and
     * later placeholders in the target label, CSS selector, and frame path.
     *
     * @param parameters values used for placeholder replacement
     * @return parameterized target copy
     */
    public PlaywrightTarget of(String... parameters) {
        return new PlaywrightTarget(
                substitute(label, parameters),
                substitute(locator, parameters),
                framePath.stream().map(frame -> substitute(frame, parameters)).toList(),
                container == null ? null : container.of(parameters));
    }

    /**
     * Creates a target resolved inside the supplied container target.
     *
     * @param containerTarget container target
     * @return nested target
     */
    public PlaywrightTarget inside(PlaywrightTarget containerTarget) {
        return new PlaywrightTarget(label + " inside " + containerTarget.label, locator, framePath, containerTarget);
    }

    /**
     * Creates a child target resolved from this target.
     *
     * @param childTarget child target
     * @return child target with this target as container
     */
    public PlaywrightTarget find(PlaywrightTarget childTarget) {
        return new PlaywrightTarget(childTarget.label + " in " + label, childTarget.locator, childTarget.framePath, this);
    }

    /**
     * Creates a target resolved inside an iframe.
     *
     * @param frameSelector frame selector
     * @return target with appended frame path
     */
    public PlaywrightTarget inFrame(String frameSelector) {
        List<String> frames = new ArrayList<>(framePath);
        frames.add(frameSelector);
        return new PlaywrightTarget(label, locator, frames, container);
    }

    /**
     * Converts this target to a raw locator specification consumable by
     * existing element action builders.
     *
     * @return raw locator specification
     */
    public LocatorSpec toLocatorSpec() {
        return LocatorSpec.raw(this::resolveFor);
    }

    /**
     * Resolves this target against a Playwright page.
     *
     * @param page Playwright page
     * @return resolved locator
     */
    public Locator resolveFor(Page page) {
        if (!framePath.isEmpty()) {
            FrameLocator frame = page.frameLocator(framePath.get(0));
            for (int i = 1; i < framePath.size(); i++) {
                frame = frame.frameLocator(framePath.get(i));
            }
            return resolveWithinFrame(frame);
        }
        if (container != null) {
            return resolveInside(container.resolveFor(page));
        }
        return resolveLocator(page);
    }

    @Override
    public String toString() {
        return label;
    }

    private Locator resolveLocator(Page page) {
        Locator resolved = switch (locator.getType()) {
            case CSS -> page.locator(locator.getSelector());
            case XPATH -> page.locator("xpath=" + locator.getSelector());
            case TEXT -> page.getByText(locator.getSelector());
            case TEST_ID -> page.getByTestId(locator.getSelector());
            case ROLE -> page.getByRole(resolveRole(locator.getSelector()),
                    new Page.GetByRoleOptions().setName(locator.getName()));
            case RAW -> locator.getRawLocator().apply(page);
        };
        return applyPosition(resolved);
    }

    private Locator resolveWithinFrame(FrameLocator frame) {
        if (container != null) {
            return resolveInside(container.resolveWithinFrame(frame));
        }
        Locator resolved = switch (locator.getType()) {
            case CSS -> frame.locator(locator.getSelector());
            case XPATH -> frame.locator("xpath=" + locator.getSelector());
            case TEXT -> frame.getByText(locator.getSelector());
            case TEST_ID -> frame.getByTestId(locator.getSelector());
            case ROLE -> frame.getByRole(resolveRole(locator.getSelector()),
                    new FrameLocator.GetByRoleOptions().setName(locator.getName()));
            case RAW -> throw new CitrusRuntimeException("Raw page locators are not supported inside Playwright targets with frame paths");
        };
        return applyPosition(resolved);
    }

    private Locator resolveInside(Locator parent) {
        Locator resolved = switch (locator.getType()) {
            case CSS -> parent.locator(locator.getSelector());
            case XPATH -> parent.locator("xpath=" + locator.getSelector());
            case TEXT -> parent.getByText(locator.getSelector());
            case TEST_ID -> parent.getByTestId(locator.getSelector());
            case ROLE -> parent.getByRole(resolveRole(locator.getSelector()),
                    new Locator.GetByRoleOptions().setName(locator.getName()));
            case RAW -> throw new CitrusRuntimeException("Raw page locators are not supported inside nested Playwright targets");
        };
        return applyPosition(resolved);
    }

    private Locator applyPosition(Locator resolved) {
        if (locator.getNth() != null) {
            return resolved.nth(locator.getNth());
        }
        if (locator.isFirst()) {
            return resolved.first();
        }
        if (locator.isLast()) {
            return resolved.last();
        }
        return resolved;
    }

    private static LocatorSpec substitute(LocatorSpec source, String... parameters) {
        LocatorSpec target = switch (source.getType()) {
            case CSS -> LocatorSpec.css(substitute(source.getSelector(), parameters));
            case XPATH -> LocatorSpec.xpath(substitute(source.getSelector(), parameters));
            case TEXT -> LocatorSpec.text(substitute(source.getSelector(), parameters));
            case ROLE -> LocatorSpec.role(substitute(source.getSelector(), parameters))
                    .name(substitute(source.getName(), parameters));
            case TEST_ID -> LocatorSpec.testId(substitute(source.getSelector(), parameters));
            case RAW -> source;
        };
        if (source.getNth() != null) {
            target.nth(source.getNth());
        } else if (source.isFirst()) {
            target.first();
        } else if (source.isLast()) {
            target.last();
        }
        return target;
    }

    private static AriaRole resolveRole(String role) {
        try {
            return AriaRole.valueOf(role.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new CitrusRuntimeException("Unsupported Playwright ARIA role: " + role, e);
        }
    }

    private static String substitute(String template, String... parameters) {
        if (template == null) {
            return null;
        }
        String result = template;
        for (int i = 0; i < parameters.length; i++) {
            result = result.replace("{" + i + "}", parameters[i]);
        }
        return result;
    }

    /**
     * Builder for labeled Playwright targets.
     */
    public static class Builder {
        private final String label;
        private final List<String> framePath;

        private Builder(String label, List<String> framePath) {
            this.label = label;
            this.framePath = List.copyOf(framePath);
        }

        /**
         * Appends a frame selector to the target frame path.
         *
         * @param frameSelector frame selector
         * @return builder copy with appended frame selector
         */
        public Builder inFrame(String frameSelector) {
            List<String> frames = new ArrayList<>(framePath);
            frames.add(frameSelector);
            return new Builder(label, frames);
        }

        /**
         * Completes the target with a CSS selector.
         *
         * @param selector CSS selector
         * @return target
         */
        public PlaywrightTarget locatedBy(String selector) {
            return located(LocatorSpec.css(selector));
        }

        /**
         * Completes the target with an explicit locator specification.
         *
         * @param locator locator specification
         * @return target
         */
        public PlaywrightTarget located(LocatorSpec locator) {
            return new PlaywrightTarget(label, locator, framePath, null);
        }
    }
}
