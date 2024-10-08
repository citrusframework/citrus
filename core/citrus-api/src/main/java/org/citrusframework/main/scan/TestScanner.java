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

package org.citrusframework.main.scan;

import java.util.List;

import org.citrusframework.TestClass;

/**
 * @since 2.7.4
 */
public interface TestScanner {

    /**
     * Find classes in package suitable to running as test with given annotation.
     * @param packageName
     * @return
     */
    List<TestClass> findTestsInPackage(String packageName);
}
