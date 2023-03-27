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

package org.citrusframework.citrus.junit.jupiter.integration.spring;

import java.util.stream.Stream;

import org.citrusframework.citrus.annotations.CitrusTestSource;
import org.citrusframework.citrus.annotations.CitrusXmlTest;
import org.citrusframework.citrus.common.TestLoader;
import org.citrusframework.citrus.config.CitrusSpringConfig;
import org.citrusframework.citrus.junit.jupiter.CitrusTestFactorySupport;
import org.citrusframework.citrus.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.citrus.junit.jupiter.spring.CitrusSpringXmlTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Christoph Deppisch
 */
@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class})
public class SpringBeanXml_IT {

    @Test
    @DisplayName("SpringBeanXml_IT")
    @CitrusXmlTest(name = "SpringBeanXml_IT")
    public void SpringBeanXml_0_IT() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.GROOVY, name = "echo.test", packageName = "org.citrusframework.citrus.junit.jupiter.simple")
    public void SpringGroovy_IT() {
    }

    @Test
    @CitrusXmlTest(name = "SampleIT")
    public void SpringBeanXml_1_IT() {
    }

    @CitrusSpringXmlTestFactory
    public Stream<DynamicTest> SpringBeanXml_2_IT() {
        return Stream.of(
                CitrusTestFactorySupport.springXml().dynamicTest("org.citrusframework.citrus.junit.jupiter.integration.actions", "EchoActionIT"),
                CitrusTestFactorySupport.springXml().dynamicTest("org.citrusframework.citrus.junit.jupiter.integration.actions", "FailActionIT"),
                CitrusTestFactorySupport.springXml().dynamicTest("org.citrusframework.citrus.junit.jupiter.integration.actions", "CreateVariablesIT")
        );
    }

    @CitrusSpringXmlTestFactory
    public Stream<DynamicTest> SpringBeanXml_3_IT() {
        return CitrusTestFactorySupport.springXml().packageScan("org.citrusframework.citrus.junit.jupiter.simple");
    }

    @Test
    @CitrusXmlTest(sources = "classpath:org/citrusframework/citrus/junit/jupiter/integration/spring/SampleIT.xml")
    public void SpringBeanXml_4_IT() {
    }
}
