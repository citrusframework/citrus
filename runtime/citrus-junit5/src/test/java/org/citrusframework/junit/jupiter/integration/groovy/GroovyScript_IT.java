/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.junit.jupiter.integration.groovy;

import java.util.stream.Stream;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.junit.jupiter.CitrusTestFactory;
import org.citrusframework.junit.jupiter.CitrusTestFactorySupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

/**
 * @author Christoph Deppisch
 */
@CitrusSupport
public class GroovyScript_IT {

    @Test
    @DisplayName("GroovyScript_IT")
    @CitrusTestSource(type = TestLoader.GROOVY)
    public void sample_test() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY, name = "sample.it")
    public void GroovyScript_1_IT() {
    }

    @CitrusTestFactory
    public Stream<DynamicTest> GroovyScript_2_IT() {
        return Stream.of(
                CitrusTestFactorySupport.groovy().dynamicTest("org.citrusframework.junit.jupiter.simple", "echo.test.groovy"),
                CitrusTestFactorySupport.groovy().dynamicTest("org.citrusframework.junit.jupiter.simple", "delay.test.groovy")
        );
    }

    @CitrusTestFactory
    public Stream<DynamicTest> GroovyScript_3_IT() {
        return CitrusTestFactorySupport.groovy().packageScan("org.citrusframework.junit.jupiter.simple");
    }

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY, sources = "classpath:org/citrusframework/junit/jupiter/integration/groovy/sample.it.groovy")
    public void GroovyScript_4_IT() {
    }
}
