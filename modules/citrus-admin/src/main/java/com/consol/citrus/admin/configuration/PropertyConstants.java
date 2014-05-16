/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.admin.configuration;

/**
 * Class holds system property keys as global constants.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public abstract class PropertyConstants {

    /** System property names */
    public static final String PROJECT_HOME = "project.home";
    public static final String ROOT_DIRECTORY = "root.directory";

    /** Base package for test cases to look for */
    public static final String BASE_PACKAGE = "test.base.package";

    /**
     * Prevent instantiation.
     */
    private PropertyConstants() {
    }
}
