/*
 * Copyright 2020 the original author or authors.
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

package org.citrusframework.citrus.validation;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.spi.ReferenceResolverAware;

/**
 * Validation callback automatically extracts message payload and headers so we work with
 * Java code for validation.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractValidationProcessor<T> implements ValidationProcessor, GenericValidationProcessor<T>, ReferenceResolverAware {

    /** Bean reference resolver injected before validation callback is called */
    protected ReferenceResolver referenceResolver;

    @Override
    public void validate(Message message, TestContext context) {
        validate((T) message.getPayload(), message.getHeaders(), context);
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
