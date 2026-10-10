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

import org.citrusframework.TestActionBuilder;
import org.citrusframework.api.actions.graphql.GraphQlActionBuilder;
import org.citrusframework.api.actions.graphql.GraphQlTestActions;
import org.citrusframework.exceptions.CitrusRuntimeException;

public interface GraphQlTestActionSupport extends GraphQlTestActions {

    @Override
    default GraphQlActionBuilder<?, ?> graphql() {
        return (GraphQlActionBuilder<?, ?>) TestActionBuilder.lookup("graphql")
                .orElseThrow(() -> new CitrusRuntimeException("Missing Citrus module 'citrus-graphql' for action 'graphql' - " +
                        "please add org.citrusframework:citrus-graphql to your project"));
    }
}
