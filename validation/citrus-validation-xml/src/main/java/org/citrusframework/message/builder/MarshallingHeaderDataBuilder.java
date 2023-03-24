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

package org.citrusframework.message.builder;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.xml.Marshaller;
import org.citrusframework.xml.StringResult;

/**
 * @author Christoph Deppisch
 */
public class MarshallingHeaderDataBuilder extends DefaultHeaderDataBuilder {

    private final Marshaller marshaller;
    private final String marshallerName;

    /**
     * Default constructor using just model object.
     * @param model
     */
    public MarshallingHeaderDataBuilder(Object model) {
        super(model);

        this.marshaller = null;
        this.marshallerName = null;
    }

    /**
     * Default constructor using object marshaller and model object.
     * @param model
     * @param marshaller
     */
    public MarshallingHeaderDataBuilder(Object model, Marshaller marshaller) {
        super(model);

        this.marshaller = marshaller;
        this.marshallerName = null;
    }

    /**
     * Default constructor using object marshaller name and model object.
     * @param model
     * @param marshallerName
     */
    public MarshallingHeaderDataBuilder(Object model, String marshallerName) {
        super(model);

        this.marshallerName = marshallerName;
        this.marshaller = null;
    }

    @Override
    public String buildHeaderData(TestContext context) {
        if (getHeaderData() == null || getHeaderData() instanceof String) {
            return super.buildHeaderData(context);
        }

        if (marshaller != null) {
            return buildHeaderData(marshaller, getHeaderData(), context);
        }

        if (marshallerName != null) {
            if (context.getReferenceResolver().isResolvable(marshallerName)) {
                Marshaller objectMapper = context.getReferenceResolver().resolve(marshallerName, Marshaller.class);
                return buildHeaderData(objectMapper, getHeaderData(), context);
            } else {
                throw new CitrusRuntimeException(String.format("Unable to find proper object marshaller for name '%s'", marshallerName));
            }
        }

        Map<String, Marshaller> marshallerMap = context.getReferenceResolver().resolveAll(Marshaller.class);
        if (marshallerMap.size() == 1) {
            return buildHeaderData(marshallerMap.values().iterator().next(), getHeaderData(), context);
        } else {
            throw new CitrusRuntimeException(String.format("Unable to auto detect object marshaller - " +
                    "found %d matching marshaller instances in reference resolver", marshallerMap.size()));
        }
    }

    private String buildHeaderData(Marshaller marshaller, Object model, TestContext context) {
        final StringResult result = new StringResult();

        try {
            marshaller.marshal(model, result);
        } catch (final Exception e) {
            throw new CitrusRuntimeException("Failed to marshal object graph for message header data", e);
        }

        return context.replaceDynamicContentInString(result.toString());
    }


}
