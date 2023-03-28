/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.rmi.model;

import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.citrusframework.rmi.model
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RmiServiceInvocation }
     *
     */
    public RmiServiceInvocation createRmiServiceInvocation() {
        return new RmiServiceInvocation();
    }

    /**
     * Create an instance of {@link RmiServiceInvocation.Args }
     *
     */
    public RmiServiceInvocation.Args createRmiServiceInvocationArgs() {
        return new RmiServiceInvocation.Args();
    }

    /**
     * Create an instance of {@link MethodArg }
     *
     */
    public MethodArg createMethodArg() {
        return new MethodArg();
    }

    /**
     * Create an instance of {@link RmiServiceResult }
     *
     */
    public RmiServiceResult createRmiServiceResult() {
        return new RmiServiceResult();
    }

    /**
     * Create an instance of {@link RmiServiceResult.Object }
     *
     */
    public RmiServiceResult.Object createRmiServiceResultObject() {
        return new RmiServiceResult.Object();
    }

}
