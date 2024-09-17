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

package org.citrusframework.kubernetes.integration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Condition;
import io.fabric8.kubernetes.api.model.DefaultKubernetesResourceList;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("citrus.dev")
@Version("v1")
public class Foo extends CustomResource<Foo.FooSpec, Foo.FooStatus> implements Namespaced {

    public static class FooSpec {
        @JsonProperty
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class FooStatus {
        @JsonProperty
        private List<Condition> conditions;

        public List<Condition> getConditions() {
            return conditions;
        }

        public void setConditions(List<Condition> conditions) {
            this.conditions = conditions;
        }
    }

    public static class FooList extends DefaultKubernetesResourceList<Foo> {
    }
}
