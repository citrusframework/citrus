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

package org.citrusframework.validation.json;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.citrusframework.validation.json.JsonMappingValidationProcessor.Builder.validate;

public class JsonMappingValidationProcessorTest extends AbstractUnitTest {

    private final JsonMapper mapper = JsonMapper.shared();
    private final Message message = new DefaultMessage("{\"name\": \"John\", \"age\": 23}");

    @Test
    public void shouldValidate() {
        JsonMappingValidationProcessor<Person> processor = validate(Person.class)
        .validator((person, headers, context) -> {
            Assertions.assertEquals("John", person.getName());
            Assertions.assertEquals(23, person.getAge());
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
            Assertions.assertEquals("John", person.getName());
            Assertions.assertEquals(23, person.getAge());
        })
        .withReferenceResolver(referenceResolver)
        .build();

        processor.validate(message, context);
    }

    @Test
    public void shouldFailValidation() {
        JsonMappingValidationProcessor<Person> processor = validate(Person.class)
        .validator((person, headers, context) -> Assertions.assertEquals(-1, person.getAge()))
        .mapper(mapper)
        .build();

        Assertions.assertThrows(AssertionError.class, () -> processor.validate(message, context));
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
