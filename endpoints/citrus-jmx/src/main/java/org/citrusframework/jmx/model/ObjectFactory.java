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

package org.citrusframework.jmx.model;

import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.citrusframework.jmx.model
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ManagedBeanInvocation }
     */
    public ManagedBeanInvocation createManagedBeanInvocation() {
        return new ManagedBeanInvocation();
    }

    /**
     * Create an instance of {@link ManagedBeanInvocation.Parameter }
     *
     */
    public ManagedBeanInvocation.Parameter createManagedBeanInvocationParameter() {
        return new ManagedBeanInvocation.Parameter();
    }

    /**
     * Create an instance of {@link OperationParam }
     *
     */
    public OperationParam createOperationParam() {
        return new OperationParam();
    }

    /**
     * Create an instance of {@link ManagedBeanResult }
     */
    public ManagedBeanResult createManagedBeanResult() {
        return new ManagedBeanResult();
    }

    /**
     * Create an instance of {@link ManagedBeanResult.Object }
     *
     */
    public ManagedBeanResult.Object createManagedBeanResultObject() {
        return new ManagedBeanResult.Object();
    }

}
