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

package org.citrusframework.xml.container;

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
     * Create an instance of {@link Iterate }
     *
     */
    public Iterate createIterate() {
        return new Iterate();
    }

    /**
     * Create an instance of {@link Parallel }
     *
     */
    public Parallel createParallel() {
        return new Parallel();
    }

    /**
     * Create an instance of {@link Sequential }
     *
     */
    public Sequential createSequential() {
        return new Sequential();
    }

    /**
     * Create an instance of {@link Repeat }
     *
     */
    public Repeat createRepeat() {
        return new Repeat();
    }

    /**
     * Create an instance of {@link RepeatOnError }
     *
     */
    public RepeatOnError createRepeatOnError() {
        return new RepeatOnError();
    }

    /**
     * Create an instance of {@link Timer }
     *
     */
    public Timer createTimer() {
        return new Timer();
    }

    /**
     * Create an instance of {@link WaitFor }
     *
     */
    public WaitFor createWaitFor() {
        return new WaitFor();
    }

    /**
     * Create an instance of {@link Assert }
     *
     */
    public Assert createAssert() {
        return new Assert();
    }

    /**
     * Create an instance of {@link Catch }
     *
     */
    public Catch createCatch() {
        return new Catch();
    }

    /**
     * Create an instance of {@link Conditional }
     *
     */
    public Conditional createConditional() {
        return new Conditional();
    }

    /**
     * Create an instance of {@link Async }
     *
     */
    public Async createAsync() {
        return new Async();
    }

}
