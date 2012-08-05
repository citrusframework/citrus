/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus;

/**
 * Actor performs send/receive message actions. With send/receive actors we can enable/disable Citrus message simulation
 * very easy. This enables a fast switch in end-to-end testing when a simulated application suddenly is real and we have to disable
 * the simulated communication parts in a test.
 *  
 * @author Christoph Deppisch
 */
public class TestActor {
    /** The name of this actor*/
    private String name;
    
    /** Marks if this test actor should participate in tests */
    private boolean enabled = true;

    /**
     * Gets the name.
     * @return the name the name to get.
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
     * Gets the enabled.
     * @return the enabled the enabled to get.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled.
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
