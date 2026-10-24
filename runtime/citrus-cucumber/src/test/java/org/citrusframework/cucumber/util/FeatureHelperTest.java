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

package org.citrusframework.cucumber.util;

import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FeatureHelperTest {

    @Test
    public void extractFeatureFileName() {
        Assertions.assertEquals("", FeatureHelper.extractFeatureFileName((URI) null));
        Assertions.assertEquals("foo.feature", FeatureHelper.extractFeatureFileName(URI.create("foo.feature")));
        Assertions.assertEquals("foo.feature", FeatureHelper.extractFeatureFileName(URI.create("/foo.feature")));
        Assertions.assertEquals("foo.feature", FeatureHelper.extractFeatureFileName(URI.create("classpath:org/citrusframework/cucumber/steps/foo/foo.feature")));
    }

    @Test
    public void extractFeatureFile() {
        Assertions.assertEquals("", FeatureHelper.extractFeatureFile((URI) null));
        Assertions.assertEquals("foo.feature", FeatureHelper.extractFeatureFile(URI.create("foo.feature")));
        Assertions.assertEquals("/foo.feature", FeatureHelper.extractFeatureFile(URI.create("/foo.feature")));
        Assertions.assertEquals("classpath:org/citrusframework/cucumber/steps/foo/foo.feature", FeatureHelper.extractFeatureFile(URI.create("classpath:org/citrusframework/cucumber/steps/foo/foo.feature")));
    }

    @Test
    public void extractFeaturePackage() {
        Assertions.assertEquals("", FeatureHelper.extractFeaturePackage((URI) null));
        Assertions.assertEquals("", FeatureHelper.extractFeaturePackage(URI.create("foo.feature")));
        Assertions.assertEquals("", FeatureHelper.extractFeaturePackage(URI.create("/foo.feature")));
        Assertions.assertEquals("org.citrusframework.cucumber.steps.foo", FeatureHelper.extractFeaturePackage(URI.create("classpath:org/citrusframework/cucumber/steps/foo/foo.feature")));
    }
}
