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

package org.citrusframework;

import java.util.Optional;

/**
 * Actor performs send/receive message actions. With send/receive actors we can enable/disable Citrus message simulation
 * very easily. This enables a fast switch in end-to-end testing when a simulated application suddenly is real and we have to disable
 * the simulated communication parts in a test.
 *
 * @since 1.3
 */
public class TestActor {

    private static final String TEST_ACTOR_ENABLED_PROPERTY = "citrus.test.actor.%s.enabled";
    private static final String TEST_ACTOR_ENABLED_ENV = "CITRUS_TEST_ACTOR_%s_ENABLED";

    /** The name of this actor */
    private String name;

    /** Marks if this test actor should not participate in tests */
    private boolean disabled = false;

    public TestActor() {
    }

    public TestActor(String name) {
        this.name = name;
    }

    public TestActor(String name, boolean disabled) {
        this.name = name;
        this.disabled = disabled;
    }

    /**
     * Gets the name.
     * @return the name to get.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the disabled.
     * @return the disabled to get.
     */
    public boolean isDisabled() {
        boolean enabled = true;
        if (name != null && !name.isBlank()) {
            // Check enabled state in System properties or environment variables for this test actor using its name
            enabled = Boolean.parseBoolean(System.getProperty(TEST_ACTOR_ENABLED_PROPERTY.formatted(name.trim().toLowerCase()),
                    Optional.ofNullable(System.getenv(TEST_ACTOR_ENABLED_ENV.formatted(
                            name.trim().replaceAll("\\W", "_").toUpperCase()))).orElse("true")));
        }

        return !enabled || disabled;
    }

    /**
     * Sets the disabled.
     * @param disabled the disabled to set
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

}
