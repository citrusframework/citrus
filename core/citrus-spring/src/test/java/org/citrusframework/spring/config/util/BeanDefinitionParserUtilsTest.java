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

package org.citrusframework.spring.config.util;

import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class BeanDefinitionParserUtilsTest extends AbstractUnitTest {

    @Test
    public void testSetPropertyReference() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

        BeanDefinitionParserUtils.setPropertyReference(builder, "beanref", "propertyname");

        Assertions.assertTrue(builder.getBeanDefinition().getPropertyValues().contains("propertyname"));
        Assertions.assertEquals(RuntimeBeanReference.class, builder.getBeanDefinition().getPropertyValues().getPropertyValue("propertyname").getValue().getClass());
        Assertions.assertEquals("beanref", ((RuntimeBeanReference)builder.getBeanDefinition().getPropertyValues().getPropertyValue("propertyname").getValue()).getBeanName());
    }

    @Test
    public void testSetPropertyValue() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

        BeanDefinitionParserUtils.setPropertyValue(builder, "propertyvalue", "propertyname");

        Assertions.assertTrue(builder.getBeanDefinition().getPropertyValues().contains("propertyname"));
        Assertions.assertEquals("propertyvalue", builder.getBeanDefinition().getPropertyValues().getPropertyValue("propertyname").getValue());
    }

    @Test
    public void testAddConstructorArgReference() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

        BeanDefinitionParserUtils.addConstructorArgReference(builder, "beanref");

        Assertions.assertEquals(1, builder.getBeanDefinition().getConstructorArgumentValues().getArgumentCount());
        Assertions.assertEquals(RuntimeBeanReference.class, builder.getBeanDefinition().getConstructorArgumentValues().getArgumentValue(0, String.class).getValue().getClass());
        Assertions.assertEquals("beanref", ((RuntimeBeanReference)builder.getBeanDefinition().getConstructorArgumentValues().getArgumentValue(0, String.class).getValue()).getBeanName());
    }

    @Test
    public void testSetPropertyNullReference() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

        BeanDefinitionParserUtils.setPropertyReference(builder, null, "propertyname");

        Assertions.assertFalse(builder.getBeanDefinition().getPropertyValues().contains("propertyname"));
    }

    @Test
    public void testSetPropertyNullValue() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

        BeanDefinitionParserUtils.setPropertyValue(builder, null, "propertyname");

        Assertions.assertFalse(builder.getBeanDefinition().getPropertyValues().contains("propertyname"));
    }

    @Test
    public void testAddConstructorArgNullReference() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

        BeanDefinitionParserUtils.addConstructorArgReference(builder, null);

        Assertions.assertEquals(0, builder.getBeanDefinition().getConstructorArgumentValues().getArgumentCount());
    }
}
