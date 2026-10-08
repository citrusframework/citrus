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
package org.citrusframework.dsl.graphql;

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GraphQlTestActionSupportTest {

    @Test
    public void shouldNameModuleWhenGraphQlIsMissing() {
        GraphQlTestActionSupport support = new GraphQlTestActionSupport() {};

        assertThatThrownBy(support::graphql)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("citrus-graphql");
    }

    @Test
    public void shouldExposeGraphQlOnTestActionSupport() {
        TestActionSupport support = new TestActionSupport() {};

        assertThatThrownBy(support::graphql)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("citrus-graphql");
    }
}
