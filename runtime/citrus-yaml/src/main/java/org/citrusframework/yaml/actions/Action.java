/*
 * Copyright 2021 the original author or authors.
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

package org.citrusframework.yaml.actions;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
public class Action implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private String reference;
    private ReferenceResolver referenceResolver;

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setRef(String reference) {
        this.reference = reference;
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
