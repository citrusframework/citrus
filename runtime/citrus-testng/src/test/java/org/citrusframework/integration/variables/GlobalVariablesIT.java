/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.integration.variables;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

/**
 * Test the use of property loader with global variables and general variable support.
 * Global variables are loaded in advance to this test. Variables are printed within this test
 * to show that global property loader works fine.
 *
 * @author Philipp Komninos
 * @since 2010
 */
public class GlobalVariablesIT extends TestNGCitrusSpringSupport {
    @Test
    @CitrusTestSource(type = TestLoader.SPRING)
    public void GlobalVariablesIT() {}
}
