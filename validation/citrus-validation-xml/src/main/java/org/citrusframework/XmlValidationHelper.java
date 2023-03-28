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

package org.citrusframework;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;

/**
 * @author Christoph Deppisch
 */
public class XmlValidationHelper {

    private static final NamespaceContextBuilder NAMESPACE_CONTEXT_BUILDER = new NamespaceContextBuilder();

    /**
     * Private constructor prevents instantiation of utility class.
     */
    private XmlValidationHelper() {
        //prevent instantiation.
    }

    /**
     * Consult reference resolver in given test context and resolve all available beans of type XsdSchemaRepository.
     * @param context
     * @return
     */
    public static List<XsdSchemaRepository> getSchemaRepositories(TestContext context) {
        if (context.getReferenceResolver() != null && context.getReferenceResolver()
                .isResolvable(XsdSchemaRepository.class)) {
            return new ArrayList<>(context.getReferenceResolver().resolveAll(XsdSchemaRepository.class).values());
        }

        return new ArrayList<>();
    }

    /**
     * Consult reference resolver in given test context and resolve bean of type NamespaceContextBuilder.
     * @param context the current test context.
     * @return resolved namespace context builder instance.
     */
    public static NamespaceContextBuilder getNamespaceContextBuilder(TestContext context) {
        if (context.getReferenceResolver() != null && context.getReferenceResolver()
                .isResolvable(NamespaceContextBuilder.class)) {
            return context.getReferenceResolver().resolve(NamespaceContextBuilder.class);
        }

        return NAMESPACE_CONTEXT_BUILDER;
    }
}
