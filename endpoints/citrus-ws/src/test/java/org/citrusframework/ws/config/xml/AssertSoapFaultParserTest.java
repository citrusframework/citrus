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

package org.citrusframework.ws.config.xml;

import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.ws.actions.AssertSoapFault;
import org.citrusframework.validation.context.ws.SoapFaultDetailValidationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertSoapFaultParserTest extends AbstractActionParserTest<AssertSoapFault> {

    @Test
    public void testAssertSoapFaultParser() {
        assertActionCount(7);
        assertActionClassAndName(AssertSoapFault.class, "soap-fault");

        // 1st action
        AssertSoapFault action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1001", action.getFaultCode());
        Assertions.assertNull(action.getFaultString());
        Assertions.assertEquals(0L, action.getFaultDetails().size());
        Assertions.assertNotNull(action.getValidationContext());

        // 2nd action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1002", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals(0L, action.getFaultDetails().size());
        Assertions.assertNotNull(action.getValidationContext());

        // 3rd action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1003", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals("FaultActor", action.getFaultActor());
        Assertions.assertEquals(1L, action.getFaultDetails().size());
        Assertions.assertEquals("FaultDetail", action.getFaultDetails().get(0));
        Assertions.assertEquals(1L, action.getValidationContext().getValidationContexts().size());

        // 4th action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), beanDefinitionContext.getBean("customSoapFaultValidator"));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1004", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals(0L, action.getFaultDetails().size());
        Assertions.assertEquals(1L, action.getFaultDetailResourcePaths().size());
        Assertions.assertEquals("classpath:org/citrusframework/ws/actions/test-fault-detail.xml", action.getFaultDetailResourcePaths().get(0));
        Assertions.assertEquals(1L, action.getValidationContext().getValidationContexts().size());

        // 5th action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1005", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals("FaultActor", action.getFaultActor());
        Assertions.assertEquals(1L, action.getFaultDetails().size());
        Assertions.assertEquals("FaultDetail", action.getFaultDetails().get(0));

        Assertions.assertEquals(1L, action.getValidationContext().getValidationContexts().size());

        SoapFaultDetailValidationContext detailValidationContext = action.getValidationContext().getValidationContexts().get(0).build();
        Assertions.assertTrue(detailValidationContext.isSchemaValidationEnabled());
        Assertions.assertEquals("fooSchemaRepository", detailValidationContext.getSchemaRepository());
        Assertions.assertNull(detailValidationContext.getSchema());

        // 6th action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1006", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals("FaultActor", action.getFaultActor());
        Assertions.assertEquals(1L, action.getFaultDetails().size());
        Assertions.assertEquals("FaultDetail", action.getFaultDetails().get(0));
        Assertions.assertEquals(1L, action.getValidationContext().getValidationContexts().size());

        detailValidationContext = action.getValidationContext().getValidationContexts().get(0).build();
        Assertions.assertTrue(detailValidationContext.isSchemaValidationEnabled());
        Assertions.assertNull(detailValidationContext.getSchemaRepository());
        Assertions.assertEquals("fooSchema", detailValidationContext.getSchema());

        // 7th action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getAction());
        Assertions.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assertions.assertEquals("{http://citrusframework.org/faults}FAULT-1007", action.getFaultCode());
        Assertions.assertEquals("FaultString", action.getFaultString());
        Assertions.assertEquals("FaultActor", action.getFaultActor());
        Assertions.assertEquals(1L, action.getFaultDetails().size());
        Assertions.assertEquals("FaultDetail", action.getFaultDetails().get(0));
        Assertions.assertEquals(1L, action.getValidationContext().getValidationContexts().size());

        detailValidationContext = action.getValidationContext().getValidationContexts().get(0).build();
        Assertions.assertFalse(detailValidationContext.isSchemaValidationEnabled());
        Assertions.assertNull(detailValidationContext.getSchemaRepository());
        Assertions.assertNull(detailValidationContext.getSchema());
    }
}
