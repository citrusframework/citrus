/*
 * Copyright 2022 the original author or authors.
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

package org.citrusframework.camel.xml;

import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.xml.CamelRouteContextFactoryBean;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.camel.actions.AbstractCamelRouteAction;
import org.citrusframework.camel.actions.CamelControlBusAction;
import org.citrusframework.camel.actions.CreateCamelRouteAction;
import org.citrusframework.camel.actions.RemoveCamelRouteAction;
import org.citrusframework.camel.actions.StartCamelRouteAction;
import org.citrusframework.camel.actions.StopCamelRouteAction;
import org.citrusframework.camel.util.CamelUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "camel")
public class Camel implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private AbstractCamelRouteAction.Builder<?, ?> builder;

    private String description;
    private String actor;
    private String camelContext;

    private ReferenceResolver referenceResolver;

    @XmlElement
    public Camel setDescription(String value) {
        this.description = value;
        return this;
    }

    @XmlAttribute(name = "actor")
    public Camel setActor(String actor) {
        this.actor = actor;
        return this;
    }

    @XmlAttribute(name = "camel-context")
    public Camel setCamelContext(String camelContext) {
        this.camelContext = camelContext;
        return this;
    }

    @XmlElement(name = "control-bus")
    public Camel setControlBus(ControlBus controlBus) {
        CamelControlBusAction.Builder builder = new CamelControlBusAction.Builder()
                .result(controlBus.getResult());

        if (controlBus.route != null) {
            builder.route(controlBus.getRoute().getId(), controlBus.getRoute().getAction());
        }

        if (controlBus.language != null) {
            builder.language(controlBus.getLanguage().getType(), controlBus.getLanguage().getExpression());
        }

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "create-routes")
    public Camel setCreateRoutes(CreateRoutes createRoutes) {
        CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder();

        if (createRoutes.routeContext != null) {
            try {
                CamelRouteContextFactoryBean factoryBean = (CamelRouteContextFactoryBean) CamelUtils.getJaxbContext().createUnmarshaller().unmarshal(createRoutes.routeContext);
                builder.routes(factoryBean.getRoutes());
            } catch (JAXBException e) {
                throw new CitrusRuntimeException("Failed to parse routes from given route context", e);
            }
        }

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "start-routes")
    public Camel setStartRoutes(Routes startRoutes) {
        StartCamelRouteAction.Builder builder = new StartCamelRouteAction.Builder();

        builder.routeIds(startRoutes.getRoutes().stream().map(Route::getId).collect(Collectors.toList()));

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "stop-routes")
    public Camel setStopRoutes(Routes stopRoutes) {
        StopCamelRouteAction.Builder builder = new StopCamelRouteAction.Builder();

        builder.routeIds(stopRoutes.getRoutes().stream().map(Route::getId).collect(Collectors.toList()));

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "remove-routes")
    public Camel setRemoveRoutes(Routes removeRoutes) {
        RemoveCamelRouteAction.Builder builder = new RemoveCamelRouteAction.Builder();

        builder.routeIds(removeRoutes.getRoutes().stream().map(Route::getId).collect(Collectors.toList()));

        this.builder = builder;
        return this;
    }

    @Override
    public TestAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Camel action - please provide proper action details");
        }

        builder.setReferenceResolver(referenceResolver);
        builder.description(description);

        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }

            if (camelContext != null) {
                builder.context(referenceResolver.resolve(camelContext, CamelContext.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

}
