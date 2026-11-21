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

package org.citrusframework.junit.jupiter.source;

import java.util.stream.Stream;

import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.junit.jupiter.CitrusTestFactory;
import org.citrusframework.junit.jupiter.CitrusTestFactorySupport;
import org.junit.jupiter.api.DynamicNode;

/**
 * Fixture driven by CitrusTestFactorySupportTest through the JUnit Jupiter engine. The class name does not end in
 * "Test" so surefire and engine package scans do not pick it up on their own.
 */
@CitrusSupport
public class MultiTestFactoryFixture {

    @CitrusTestFactory
    public Stream<DynamicNode> nodes() {
        return CitrusTestFactorySupport.factory("fake-multi")
                .dynamicNodes("org.example", "multi-failing-disabled", "single", "broken", "multi");
    }
}
