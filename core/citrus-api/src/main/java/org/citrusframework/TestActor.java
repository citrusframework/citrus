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

package org.citrusframework;

/**
 * Actor performs send/receive message actions. With send/receive actors we can enable/disable Citrus message simulation
 * very easily. This enables a fast switch in end-to-end testing when a simulated application suddenly is real and we have to disable
 * the simulated communication parts in a test.
 *  
 * @author Christoph Deppisch
 * @since 1.3
 */
public class TestActor {
    /** The name of this actor*/
    private String name;
    
    /** Marks if this test actor should not participate in tests */
    private boolean disabled = false;

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
     * Gets the disabled.
     * @return the disabled the disabled to get.
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Sets the disabled.
     * @param disabled the disabled to set
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    
}
