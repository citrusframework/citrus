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

package org.citrusframework.container;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

class ReferenceResolverAwareTestAction implements TestAction, ReferenceResolverAware {

    private ReferenceResolver referenceResolver;

    @Override
    public void execute(TestContext context) {
        // do nothing
    }

    static class Builder implements TestActionBuilder<ReferenceResolverAwareTestAction>, ReferenceResolverAware {

        private ReferenceResolver referenceResolver;

        @Override
        public ReferenceResolverAwareTestAction build() {
            ReferenceResolverAwareTestAction action = new ReferenceResolverAwareTestAction();
            action.setReferenceResolver(referenceResolver);
            return action;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }
}
