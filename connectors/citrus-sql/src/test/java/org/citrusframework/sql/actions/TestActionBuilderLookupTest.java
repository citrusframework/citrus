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

package org.citrusframework.sql.actions;

import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestActionBuilderLookupTest {

    @Test
    public void shouldLookupTestActions() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("sql"));
        Assertions.assertTrue(endpointBuilders.containsKey("query"));
        Assertions.assertTrue(endpointBuilders.containsKey("plsql"));
    }

    @Test
    public void shouldLookupTestActionByName() {
        Assertions.assertTrue(TestActionBuilder.lookup("sql").isPresent());
        Assertions.assertEquals(ExecuteSQLAction.Builder.class, TestActionBuilder.lookup("sql").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("query").isPresent());
        Assertions.assertEquals(ExecuteSQLQueryAction.Builder.class, TestActionBuilder.lookup("query").get().getClass());
        Assertions.assertTrue(TestActionBuilder.lookup("plsql").isPresent());
        Assertions.assertEquals(ExecutePLSQLAction.Builder.class, TestActionBuilder.lookup("plsql").get().getClass());
    }
}
