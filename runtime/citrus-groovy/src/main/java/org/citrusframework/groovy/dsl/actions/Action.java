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

package org.citrusframework.groovy.dsl.actions;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

public class Action implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private String reference;
    private ReferenceResolver referenceResolver;

    public static Action action() {
        return new Action();
    }

    public Action reference(String reference) {
        this.reference = reference;
        return this;
    }

    public Action ref(String reference) {
        this.reference = reference;
        return this;
    }

    @Override
    public TestAction build() {
        if (referenceResolver != null) {
            if (referenceResolver.isResolvable(reference, TestActionBuilder.class)) {
                return referenceResolver.resolve(reference, TestActionBuilder.class).build();
            }

            if (referenceResolver.isResolvable(reference, TestAction.class)) {
                return referenceResolver.resolve(reference, TestAction.class);
            }
        }

        throw new CitrusRuntimeException(String.format("Unable to resolve test action with reference '%s'", reference));
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
