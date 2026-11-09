/*
 * Copyright the original author or authors.
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

import org.citrusframework.jmx.mbean.HelloBean;
import org.citrusframework.jmx.mbean.HelloBeanImpl;
import org.citrusframework.jmx.mbean.NewsBean;
import org.citrusframework.jmx.mbean.NewsBeanImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.management.MBeanInfo;
import javax.management.ObjectName;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * @since 2.5
 */
public class ManagedBeanDefinitionTest {

    @Test
    public void testObjectName() {
        ManagedBeanDefinition definition = new ManagedBeanDefinition();
        definition.setType(HelloBean.class);
        ObjectName objectName = definition.createObjectName();

        Assertions.assertEquals("org.citrusframework.jmx.mbean:type=HelloBean", objectName.toString());

        definition = new ManagedBeanDefinition();
        definition.setObjectDomain(HelloBean.class.getPackage().getName());
        definition.setObjectName("type=HelloBean,name=Hello");
        objectName = definition.createObjectName();

        Assertions.assertEquals("org.citrusframework.jmx.mbean:type=HelloBean,name=Hello", objectName.toString());

        definition = new ManagedBeanDefinition();
        definition.setObjectDomain(HelloBean.class.getPackage().getName());
        definition.setName(HelloBean.class.getSimpleName());
        objectName = definition.createObjectName();

        Assertions.assertEquals("org.citrusframework.jmx.mbean:name=HelloBean", objectName.toString());
    }

    @Test
    public void testBeanInfoEmpty() {
        ManagedBeanDefinition definition = new ManagedBeanDefinition();
        MBeanInfo info = definition.createMBeanInfo();

        Assertions.assertEquals("org.citrusframework.CitrusMBean", info.getClassName());
    }

    @Test
    public void testBeanInfoFromInterface() {
        ManagedBeanDefinition definition = new ManagedBeanDefinition();
        definition.setType(HelloBean.class);
        MBeanInfo info = definition.createMBeanInfo();

        Assertions.assertEquals("org.citrusframework.jmx.mbean.HelloBean", info.getClassName());
        Assertions.assertEquals(1, info.getAttributes().length);
        Assertions.assertEquals(String.class.getName(), info.getAttributes()[0].getType());
        Assertions.assertEquals("HelloMessage", info.getAttributes()[0].getName());
        Assertions.assertEquals(1, info.getOperations().length);
        Assertions.assertEquals("hello", info.getOperations()[0].getName());
        Assertions.assertEquals(1, info.getOperations()[0].getSignature().length);
        Assertions.assertEquals(String.class.getName(), info.getOperations()[0].getSignature()[0].getType());
        Assertions.assertEquals("p1", info.getOperations()[0].getSignature()[0].getName());
        Assertions.assertEquals(String.class.getName(), info.getOperations()[0].getReturnType());

        definition.setType(NewsBean.class);
        info = definition.createMBeanInfo();

        Assertions.assertEquals("org.citrusframework.jmx.mbean.NewsBean", info.getClassName());
        Assertions.assertEquals(1, info.getAttributes().length);
        Assertions.assertEquals(String.class.getName(), info.getAttributes()[0].getType());
        Assertions.assertEquals("News", info.getAttributes()[0].getName());
        Assertions.assertEquals(0, info.getOperations().length);
    }

    @Test
    public void testBeanInfoFromImpl() {
        ManagedBeanDefinition definition = new ManagedBeanDefinition();
        definition.setType(HelloBeanImpl.class);
        MBeanInfo info = definition.createMBeanInfo();

        Assertions.assertEquals("org.citrusframework.jmx.mbean.HelloBeanImpl", info.getClassName());
        Assertions.assertEquals(1, info.getAttributes().length);
        Assertions.assertEquals(String.class.getName(), info.getAttributes()[0].getType());
        Assertions.assertEquals("helloMessage", info.getAttributes()[0].getName());
        Assertions.assertEquals(1, info.getOperations().length);
        Assertions.assertEquals("hello", info.getOperations()[0].getName());
        Assertions.assertEquals(1, info.getOperations()[0].getSignature().length);
        Assertions.assertEquals(String.class.getName(), info.getOperations()[0].getSignature()[0].getType());
        Assertions.assertEquals("p1", info.getOperations()[0].getSignature()[0].getName());
        Assertions.assertEquals(String.class.getName(), info.getOperations()[0].getReturnType());

        definition.setType(NewsBeanImpl.class);
        info = definition.createMBeanInfo();

        Assertions.assertEquals("org.citrusframework.jmx.mbean.NewsBeanImpl", info.getClassName());
        Assertions.assertEquals(1, info.getAttributes().length);
        Assertions.assertEquals(String.class.getName(), info.getAttributes()[0].getType());
        Assertions.assertEquals("news", info.getAttributes()[0].getName());
        Assertions.assertEquals(0, info.getOperations().length);
    }

    @Test
    public void testBeanInfoFromGenericInfo() {
        ManagedBeanDefinition definition = new ManagedBeanDefinition();
        definition.setName("GenericBean");
        ManagedBeanInvocation.Attribute att1 = new ManagedBeanInvocation.Attribute();
        att1.setType(String.class.getName());
        att1.setName("message");
        ManagedBeanInvocation.Attribute att2 = new ManagedBeanInvocation.Attribute();
        att2.setType(Boolean.class.getName());
        att2.setName("standard");
        definition.setAttributes(List.of(att1, att2));

        ManagedBeanInvocation.Operation op1 = new ManagedBeanInvocation.Operation();
        op1.setName("operation");
        op1.setParameter(new ManagedBeanInvocation.Parameter());
        OperationParam p1 = new OperationParam();
        p1.setType(Integer.class.getName());
        op1.getParameter().getParameter().add(p1);
        definition.setOperations(singletonList(op1));

        MBeanInfo info = definition.createMBeanInfo();

        Assertions.assertEquals("GenericBean", info.getClassName());
        Assertions.assertEquals(2, info.getAttributes().length);
        Assertions.assertEquals(String.class.getName(), info.getAttributes()[0].getType());
        Assertions.assertEquals("message", info.getAttributes()[0].getName());
        Assertions.assertEquals(Boolean.class.getName(), info.getAttributes()[1].getType());
        Assertions.assertEquals("standard", info.getAttributes()[1].getName());
        Assertions.assertEquals(1, info.getOperations().length);
        Assertions.assertEquals("operation", info.getOperations()[0].getName());
        Assertions.assertEquals(1, info.getOperations()[0].getSignature().length);
        Assertions.assertEquals(Integer.class.getName(), info.getOperations()[0].getSignature()[0].getType());
        Assertions.assertEquals("p1", info.getOperations()[0].getSignature()[0].getName());
        Assertions.assertNull(info.getOperations()[0].getReturnType());
    }
}
