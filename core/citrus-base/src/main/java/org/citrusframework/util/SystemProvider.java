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

package org.citrusframework.util;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public final class SystemProvider {

    public Optional<String> getEnv(String envVarName) {
        return ofNullable(System.getenv(envVarName));
    }

    public Optional<String> getProperty(String propertyName) {
        return ofNullable(System.getProperty(propertyName));
    }

    public void setProperty(String propertyName, String propertyValue) {
        System.setProperty(propertyName, propertyValue);
    }
}
