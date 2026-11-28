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

package org.citrusframework.common;

import java.util.List;

import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MultiTestDefinitionTest {

    @Test
    void shouldAcceptValidDefinition() {
        MultiTestDefinition definition = new MultiTestDefinition.Builder()
                .name("orders")
                .test(test("ok"))
                .test(test("error"))
                .build();

        definition.validate("orders");

        Assertions.assertEquals(List.of("ok", "error"), definition.getTestNames());
        Assertions.assertEquals("orders", definition.getName());
    }

    @Test
    void shouldRejectEmptyDefinition() {
        MultiTestDefinition definition = new MultiTestDefinition.Builder().name("orders").build();

        CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> definition.validate("orders"));
        Assertions.assertEquals("Multi-test file 'orders' holds no test", error.getMessage());
    }

    @Test
    void shouldRejectTestWithoutName() {
        MultiTestDefinition definition = new MultiTestDefinition.Builder()
                .test(test("ok"))
                .test(new DefaultTestCase())
                .build();
        definition.getTests().get(1).setName(null);

        CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> definition.validate("orders"));
        Assertions.assertEquals("Multi-test file 'orders' has a test without name at position 2", error.getMessage());
    }

    @Test
    void shouldListAllDuplicateNames() {
        MultiTestDefinition definition = new MultiTestDefinition.Builder()
                .test(test("ok"))
                .test(test("error"))
                .test(test("ok"))
                .test(test("error"))
                .test(test("other"))
                .build();

        CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> definition.validate("orders"));
        Assertions.assertEquals("Multi-test file 'orders' has duplicate test names: error, ok", error.getMessage());
    }

    private static DefaultTestCase test(String name) {
        DefaultTestCase test = new DefaultTestCase();
        test.setName(name);
        return test;
    }
}
