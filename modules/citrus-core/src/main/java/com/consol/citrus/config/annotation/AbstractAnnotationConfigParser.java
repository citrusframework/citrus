/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.config.annotation;

import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.endpoint.Endpoint;

import java.lang.annotation.Annotation;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class AbstractAnnotationConfigParser<A extends Annotation, T extends Endpoint> implements AnnotationConfigParser<A, T> {

    private final ReferenceResolver referenceResolver;

    public AbstractAnnotationConfigParser(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Gets the value of the referenceResolver property.
     *
     * @return the referenceResolver
     */
    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }
}
