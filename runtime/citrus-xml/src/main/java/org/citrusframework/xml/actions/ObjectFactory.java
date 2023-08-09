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

package org.citrusframework.xml.actions;

import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.citrusframework.ftp.model package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.citrusframework.xml.actions
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AntRun }
     *
     */
    public AntRun createAntRun() {
        return new AntRun();
    }

    /**
     * Create an instance of {@link Echo }
     *
     */
    public Echo createEcho() {
        return new Echo();
    }

    /**
     * Create an instance of {@link Send }
     *
     */
    public Send createSend() {
        return new Send();
    }

    /**
     * Create an instance of {@link Receive }
     *
     */
    public Receive createReceive() {
        return new Receive();
    }

    /**
     * Create an instance of {@link Print }
     *
     */
    public Print createPrint() {
        return new Print();
    }

    /**
     * Create an instance of {@link Sleep }
     *
     */
    public Sleep createSleep() {
        return new Sleep();
    }

    /**
     * Create an instance of {@link Delay }
     *
     */
    public Delay createDelay() {
        return new Delay();
    }

    /**
     * Create an instance of {@link CreateVariables }
     *
     */
    public CreateVariables createCreateVariables() {
        return new CreateVariables();
    }

    /**
     * Create an instance of {@link StopTimer }
     *
     */
    public StopTimer createStopTimer() {
        return new StopTimer();
    }

    /**
     * Create an instance of {@link LoadProperties }
     *
     */
    public LoadProperties createLoadProperties() {
        return new LoadProperties();
    }

    /**
     * Create an instance of {@link StopTime }
     *
     */
    public StopTime createStopTime() {
        return new StopTime();
    }

    /**
     * Create an instance of {@link Start }
     *
     */
    public Start createStart() {
        return new Start();
    }

    /**
     * Create an instance of {@link Stop }
     *
     */
    public Stop createStop() {
        return new Stop();
    }

    /**
     * Create an instance of {@link TraceVariables }
     *
     */
    public TraceVariables createTraceVariables() {
        return new TraceVariables();
    }


    /**
     * Create an instance of {@link Trace }
     *
     */
    public Trace createTrace() {
        return new Trace();
    }

    /**
     * Create an instance of {@link PurgeEndpoint }
     *
     */
    public PurgeEndpoint createPurgeEndpoint() {
        return new PurgeEndpoint();
    }

    /**
     * Create an instance of {@link Action }
     */
    public Action createAction() {
        return new Action();
    }
}
