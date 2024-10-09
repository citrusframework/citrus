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

package org.citrusframework.testcontainers.actions;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTestcontainersAction extends AbstractTestAction implements TestcontainersAction {

    /** Logger */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public AbstractTestcontainersAction(String name, Builder<?, ?> builder) {
        super("testcontainers:" + name, builder);
    }

    /**
     * Action builder.
     */
    public static abstract class Builder<T extends TestcontainersAction, B extends Builder<T, B>> extends AbstractTestActionBuilder<T, B> implements ReferenceResolverAware {

        protected ReferenceResolver referenceResolver;

        public B withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        @Override
        public T build() {
            return doBuild();
        }

        protected abstract T doBuild();

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }
}
