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

package org.citrusframework;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.BindToRegistry;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class CitrusContextTest {

    @Test
    public void shouldParseConfiguration() {
        CitrusContext context = CitrusContext.create();
        context.parseConfiguration(FooConfig.class);

        Assert.assertTrue(context.getReferenceResolver().isResolvable("foo"));
        Assert.assertEquals(context.getReferenceResolver().resolve("foo"), "Foo");
        Assert.assertTrue(context.getReferenceResolver().isResolvable("bar"));
        Assert.assertEquals(context.getReferenceResolver().resolve("bar"), "Bar");
        Assert.assertTrue(context.getReferenceResolver().isResolvable("foobar"));
        Assert.assertEquals(context.getReferenceResolver().resolve("foobar"), "FooBar");
    }

    public static class FooConfig {
        @BindToRegistry
        String bar = "Bar";

        @BindToRegistry
        public String foo() {
            return "Foo";
        }

        @BindToRegistry(name = "foobar")
        public String bar() {
            return "FooBar";
        }
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing or non-accessible default constructor on custom configuration class")
    public void shouldRaiseErrorWithNoDefaultConstructor() {
        CitrusContext context = CitrusContext.create();
        context.parseConfiguration(NoDefaultConstructorConfig.class);
    }

    public static class NoDefaultConstructorConfig {
        public NoDefaultConstructorConfig(String config) {
            LoggerFactory.getLogger(NoDefaultConstructorConfig.class).info(config);
        }
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Failed to invoke configuration method")
    public void shouldRaiseErrorWithPrivateMethod() {
        CitrusContext context = CitrusContext.create();
        context.parseConfiguration(PrivateMethodConfig.class);
    }

    public static class PrivateMethodConfig {

        @BindToRegistry
        private String error() {
            return "should fail";
        }
    }
}
