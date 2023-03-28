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

import org.citrusframework.jmx.mbean.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.management.*;
import java.util.Arrays;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ManagedBeanDefinitionTest {

    @Test
    public void testObjectName() {
        ManagedBeanDefinition definition = new ManagedBeanDefinition();
        definition.setType(HelloBean.class);
        ObjectName objectName = definition.createObjectName();

        Assert.assertEquals(objectName.toString(), "org.citrusframework.jmx.mbean:type=HelloBean");

        definition = new ManagedBeanDefinition();
        definition.setObjectDomain(HelloBean.class.getPackage().getName());
        definition.setObjectName("type=HelloBean,name=Hello");
        objectName = definition.createObjectName();

        Assert.assertEquals(objectName.toString(), "org.citrusframework.jmx.mbean:type=HelloBean,name=Hello");

        definition = new ManagedBeanDefinition();
        definition.setObjectDomain(HelloBean.class.getPackage().getName());
        definition.setName(HelloBean.class.getSimpleName());
        objectName = definition.createObjectName();

        Assert.assertEquals(objectName.toString(), "org.citrusframework.jmx.mbean:name=HelloBean");
    }

    @Test
    public void testBeanInfoEmpty() {
        ManagedBeanDefinition definition = new ManagedBeanDefinition();
        MBeanInfo info = definition.createMBeanInfo();

        Assert.assertEquals(info.getClassName(), "org.citrusframework.CitrusMBean");
    }

    @Test
    public void testBeanInfoFromInterface() {
        ManagedBeanDefinition definition = new ManagedBeanDefinition();
        definition.setType(HelloBean.class);
        MBeanInfo info = definition.createMBeanInfo();

        Assert.assertEquals(info.getClassName(), "org.citrusframework.jmx.mbean.HelloBean");
        Assert.assertEquals(info.getAttributes().length, 1);
        Assert.assertEquals(info.getAttributes()[0].getType(), String.class.getName());
        Assert.assertEquals(info.getAttributes()[0].getName(), "HelloMessage");
        Assert.assertEquals(info.getOperations().length, 1);
        Assert.assertEquals(info.getOperations()[0].getName(), "hello");
        Assert.assertEquals(info.getOperations()[0].getSignature().length, 1);
        Assert.assertEquals(info.getOperations()[0].getSignature()[0].getType(), String.class.getName());
        Assert.assertEquals(info.getOperations()[0].getSignature()[0].getName(), "p1");
        Assert.assertEquals(info.getOperations()[0].getReturnType(), String.class.getName());

        definition.setType(NewsBean.class);
        info = definition.createMBeanInfo();

        Assert.assertEquals(info.getClassName(), "org.citrusframework.jmx.mbean.NewsBean");
        Assert.assertEquals(info.getAttributes().length, 1);
        Assert.assertEquals(info.getAttributes()[0].getType(), String.class.getName());
        Assert.assertEquals(info.getAttributes()[0].getName(), "News");
        Assert.assertEquals(info.getOperations().length, 0);
    }

    @Test
    public void testBeanInfoFromImpl() {
        ManagedBeanDefinition definition = new ManagedBeanDefinition();
        definition.setType(HelloBeanImpl.class);
        MBeanInfo info = definition.createMBeanInfo();

        Assert.assertEquals(info.getClassName(), "org.citrusframework.jmx.mbean.HelloBeanImpl");
        Assert.assertEquals(info.getAttributes().length, 1);
        Assert.assertEquals(info.getAttributes()[0].getType(), String.class.getName());
        Assert.assertEquals(info.getAttributes()[0].getName(), "helloMessage");
        Assert.assertEquals(info.getOperations().length, 1);
        Assert.assertEquals(info.getOperations()[0].getName(), "hello");
        Assert.assertEquals(info.getOperations()[0].getSignature().length, 1);
        Assert.assertEquals(info.getOperations()[0].getSignature()[0].getType(), String.class.getName());
        Assert.assertEquals(info.getOperations()[0].getSignature()[0].getName(), "p1");
        Assert.assertEquals(info.getOperations()[0].getReturnType(), String.class.getName());

        definition.setType(NewsBeanImpl.class);
        info = definition.createMBeanInfo();

        Assert.assertEquals(info.getClassName(), "org.citrusframework.jmx.mbean.NewsBeanImpl");
        Assert.assertEquals(info.getAttributes().length, 1);
        Assert.assertEquals(info.getAttributes()[0].getType(), String.class.getName());
        Assert.assertEquals(info.getAttributes()[0].getName(), "news");
        Assert.assertEquals(info.getOperations().length, 0);
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
        definition.setAttributes(Arrays.asList(att1, att2));

        ManagedBeanInvocation.Operation op1 = new ManagedBeanInvocation.Operation();
        op1.setName("operation");
        op1.setParameter(new ManagedBeanInvocation.Parameter());
        OperationParam p1 = new OperationParam();
        p1.setType(Integer.class.getName());
        op1.getParameter().getParameter().add(p1);
        definition.setOperations(Arrays.asList(op1));

        MBeanInfo info = definition.createMBeanInfo();

        Assert.assertEquals(info.getClassName(), "GenericBean");
        Assert.assertEquals(info.getAttributes().length, 2);
        Assert.assertEquals(info.getAttributes()[0].getType(), String.class.getName());
        Assert.assertEquals(info.getAttributes()[0].getName(), "message");
        Assert.assertEquals(info.getAttributes()[1].getType(), Boolean.class.getName());
        Assert.assertEquals(info.getAttributes()[1].getName(), "standard");
        Assert.assertEquals(info.getOperations().length, 1);
        Assert.assertEquals(info.getOperations()[0].getName(), "operation");
        Assert.assertEquals(info.getOperations()[0].getSignature().length, 1);
        Assert.assertEquals(info.getOperations()[0].getSignature()[0].getType(), Integer.class.getName());
        Assert.assertEquals(info.getOperations()[0].getSignature()[0].getName(), "p1");
        Assert.assertNull(info.getOperations()[0].getReturnType());
    }

}
