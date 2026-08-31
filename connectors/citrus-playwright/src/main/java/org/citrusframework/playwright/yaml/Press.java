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

import org.citrusframework.playwright.actions.InputAction;

/**
 * Sends a keyboard key or shortcut to a locator.
 */
public class Press extends InputActionSupport<Press> {

    public Press() {
        super(InputAction.Command.PRESS);
    }

    /**
     * Alias property name for the pressed key.
     * @param key key or shortcut.
     */
    public void setKey(String key) {
        setValue(key);
    }
}
