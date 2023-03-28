/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.config.util;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class BeanDefinitionParserUtilsTest extends AbstractTestNGUnitTest {

    @Test
    public void testSetPropertyReference() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        
        BeanDefinitionParserUtils.setPropertyReference(builder, "beanref", "propertyname");
        
        Assert.assertTrue(builder.getBeanDefinition().getPropertyValues().contains("propertyname"));
        Assert.assertEquals(builder.getBeanDefinition().getPropertyValues().getPropertyValue("propertyname").getValue().getClass(), RuntimeBeanReference.class);
        Assert.assertEquals(((RuntimeBeanReference)builder.getBeanDefinition().getPropertyValues().getPropertyValue("propertyname").getValue()).getBeanName(), "beanref");
    }
    
    @Test
    public void testSetPropertyValue() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        
        BeanDefinitionParserUtils.setPropertyValue(builder, "propertyvalue", "propertyname");
        
        Assert.assertTrue(builder.getBeanDefinition().getPropertyValues().contains("propertyname"));
        Assert.assertEquals(builder.getBeanDefinition().getPropertyValues().getPropertyValue("propertyname").getValue(), "propertyvalue");
    }
    
    @Test
    public void testAddConstructorArgReference() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        
        BeanDefinitionParserUtils.addConstructorArgReference(builder, "beanref");
        
        Assert.assertEquals(builder.getBeanDefinition().getConstructorArgumentValues().getArgumentCount(), 1);
        Assert.assertEquals(builder.getBeanDefinition().getConstructorArgumentValues().getArgumentValue(0, String.class).getValue().getClass(), RuntimeBeanReference.class);
        Assert.assertEquals(((RuntimeBeanReference)builder.getBeanDefinition().getConstructorArgumentValues().getArgumentValue(0, String.class).getValue()).getBeanName(), "beanref");
    }
    
    @Test
    public void testSetPropertyNullReference() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        
        BeanDefinitionParserUtils.setPropertyReference(builder, null, "propertyname");
        
        Assert.assertFalse(builder.getBeanDefinition().getPropertyValues().contains("propertyname"));
    }
    
    @Test
    public void testSetPropertyNullValue() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        
        BeanDefinitionParserUtils.setPropertyValue(builder, null, "propertyname");
        
        Assert.assertFalse(builder.getBeanDefinition().getPropertyValues().contains("propertyname"));
    }
    
    @Test
    public void testAddConstructorArgNullReference() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        
        BeanDefinitionParserUtils.addConstructorArgReference(builder, null);
        
        Assert.assertEquals(builder.getBeanDefinition().getConstructorArgumentValues().getArgumentCount(), 0);
    }
}
