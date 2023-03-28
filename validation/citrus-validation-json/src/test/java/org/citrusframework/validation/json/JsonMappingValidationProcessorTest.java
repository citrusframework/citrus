/*
 * Copyright 2020 the original author or authors.
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

package org.citrusframework.validation.json;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.validation.json.JsonMappingValidationProcessor.Builder.validate;

/**
 * @author Christoph Deppisch
 */
public class JsonMappingValidationProcessorTest extends AbstractTestNGUnitTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Message message = new DefaultMessage("{\"name\": \"John\", \"age\": 23}");

    @Test
    public void shouldValidate() {
        JsonMappingValidationProcessor<Person> processor = validate(Person.class)
        .validator((person, headers, context) -> {
            Assert.assertEquals(person.getName(), "John");
            Assert.assertEquals(person.getAge(), 23);
        })
        .mapper(mapper)
        .build();

        processor.validate(message, context);
    }

    @Test
    public void shouldResolveMapper() {
        ReferenceResolver referenceResolver = new SimpleReferenceResolver();
        referenceResolver.bind("mapper", mapper);

        JsonMappingValidationProcessor<Person> processor = validate(Person.class)
        .validator((person, headers, context) -> {
            Assert.assertEquals(person.getName(), "John");
            Assert.assertEquals(person.getAge(), 23);
        })
        .withReferenceResolver(referenceResolver)
        .build();

        processor.validate(message, context);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void shouldFailValidation() {
        JsonMappingValidationProcessor<Person> processor = validate(Person.class)
        .validator((person, headers, context) -> Assert.assertEquals(person.getAge(), -1))
        .mapper(mapper)
        .build();

        processor.validate(message, context);
    }

    private final static class Person {
        private int age;
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
