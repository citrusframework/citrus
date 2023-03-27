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

package org.citrusframework.citrus.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.xml.actions.Action;
import org.citrusframework.citrus.xml.actions.ApplyTemplate;
import org.citrusframework.citrus.xml.actions.CreateVariables;
import org.citrusframework.citrus.xml.actions.Echo;
import org.citrusframework.citrus.xml.actions.ExpectTimeout;
import org.citrusframework.citrus.xml.actions.Fail;
import org.citrusframework.citrus.xml.actions.LoadProperties;
import org.citrusframework.citrus.xml.actions.Print;
import org.citrusframework.citrus.xml.actions.PurgeEndpoint;
import org.citrusframework.citrus.xml.actions.Receive;
import org.citrusframework.citrus.xml.actions.Sleep;
import org.citrusframework.citrus.xml.actions.Start;
import org.citrusframework.citrus.xml.actions.Stop;
import org.citrusframework.citrus.xml.actions.StopTime;
import org.citrusframework.citrus.xml.actions.StopTimer;
import org.citrusframework.citrus.xml.actions.TraceVariables;
import org.citrusframework.citrus.xml.actions.Transform;
import org.citrusframework.citrus.xml.actions.XmlTestActionBuilder;
import org.citrusframework.citrus.xml.container.Assert;
import org.citrusframework.citrus.xml.container.Async;
import org.citrusframework.citrus.xml.container.Catch;
import org.citrusframework.citrus.xml.container.Conditional;
import org.citrusframework.citrus.xml.container.Iterate;
import org.citrusframework.citrus.xml.container.Parallel;
import org.citrusframework.citrus.xml.container.Repeat;
import org.citrusframework.citrus.xml.container.RepeatOnError;
import org.citrusframework.citrus.xml.container.Sequential;
import org.citrusframework.citrus.xml.container.Timer;
import org.citrusframework.citrus.xml.container.WaitFor;
import org.w3c.dom.Node;

/**
 * @author Christoph Deppisch
 */
public class TestActions {

    /** Unmarshaller cache filled with instances created for custom action Xml element refs */
    private static final Map<String, Unmarshaller> UNMARSHALLER_CACHE = new HashMap<>();

    @XmlElementRefs({
            @XmlElementRef(name = "action", type = Action.class, required = false),
            @XmlElementRef(name = "echo", type = Echo.class, required = false),
            @XmlElementRef(name = "print", type = Print.class, required = false),
            @XmlElementRef(name = "sleep", type = Sleep.class, required = false),
            @XmlElementRef(name = "receive", type = Receive.class, required = false),
            @XmlElementRef(name = "create-variables", type = CreateVariables.class, required = false),
            @XmlElementRef(name = "load", type = LoadProperties.class, required = false),
            @XmlElementRef(name = "expect-timeout", type = ExpectTimeout.class, required = false),
            @XmlElementRef(name = "fail", type = Fail.class, required = false),
            @XmlElementRef(name = "iterate", type = Iterate.class, required = false),
            @XmlElementRef(name = "sequential", type = Sequential.class, required = false),
            @XmlElementRef(name = "parallel", type = Parallel.class, required = false),
            @XmlElementRef(name = "repeat", type = Repeat.class, required = false),
            @XmlElementRef(name = "repeat-on-error", type = RepeatOnError.class, required = false),
            @XmlElementRef(name = "conditional", type = Conditional.class, required = false),
            @XmlElementRef(name = "assert", type = Assert.class, required = false),
            @XmlElementRef(name = "catch", type = Catch.class, required = false),
            @XmlElementRef(name = "waitFor", type = WaitFor.class, required = false),
            @XmlElementRef(name = "async", type = Async.class, required = false),
            @XmlElementRef(name = "timer", type = Timer.class, required = false),
            @XmlElementRef(name = "stop-timer", type = StopTimer.class, required = false),
            @XmlElementRef(name = "stop-time", type = StopTime.class, required = false),
            @XmlElementRef(name = "start", type = Start.class, required = false),
            @XmlElementRef(name = "stop", type = Stop.class, required = false),
            @XmlElementRef(name = "trace-variables", type = TraceVariables.class, required = false),
            @XmlElementRef(name = "purge-endpoint", type = PurgeEndpoint.class, required = false),
            @XmlElementRef(name = "transform", type = Transform.class, required = false),
            @XmlElementRef(name = "apply-template", type = ApplyTemplate.class, required = false),
    })
    @XmlAnyElement(lax = true)
    private List<Object> actions;

    public List<Object> getActions() {
        return actions;
    }

    /**
     * Converts given XML actions to normal test action builder instances.
     * @return nested actions as test action builders.
     */
    public List<TestActionBuilder<?>> getActionBuilders() {
        List<TestActionBuilder<?>> builders = new ArrayList<>();

        for (Object object : getActions()) {
            Object action = object;

            if (object instanceof JAXBElement) {
                action = ((JAXBElement<?>) object).getValue();
            }

            if (object instanceof Node) {
                Node node = (Node) object;

                Optional<TestActionBuilder<?>> builder = XmlTestActionBuilder.lookup(node.getLocalName(), node.getNamespaceURI());
                if (builder.isPresent()) {
                    try {
                        Unmarshaller unmarshaller;
                        if (UNMARSHALLER_CACHE.containsKey(builder.get().getClass().getName())) {
                            unmarshaller = UNMARSHALLER_CACHE.get(builder.get().getClass().getName());
                        } else {
                            unmarshaller = JAXBContext.newInstance(builder.get().getClass()).createUnmarshaller();
                            UNMARSHALLER_CACHE.put(builder.get().getClass().getName(), unmarshaller);
                        }

                        action = unmarshaller.unmarshal(node, builder.get().getClass()).getValue();
                    } catch (JAXBException e) {
                        throw new CitrusRuntimeException("Failed to create XMLTestLoader instance", e);
                    }
                }
            }

            if (action instanceof TestActionBuilder<?>) {
                builders.add((TestActionBuilder<?>) action);
            }
        }

        return builders;
    }
}
