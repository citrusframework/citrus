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

package org.citrusframework.ws.config.xml;

import org.citrusframework.testng.AbstractActionParserTest;
import org.citrusframework.ws.actions.AssertSoapFault;
import org.citrusframework.ws.validation.SoapFaultDetailValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class AssertSoapFaultParserTest extends AbstractActionParserTest<AssertSoapFault> {

    @Test
    public void testAssertSoapFaultParser() {
        assertActionCount(7);
        assertActionClassAndName(AssertSoapFault.class, "soap-fault");

        // 1st action
        AssertSoapFault action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1001");
        Assert.assertNull(action.getFaultString());
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertNotNull(action.getValidationContext());

        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1002");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertNotNull(action.getValidationContext());

        // 3rd action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1003");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "FaultDetail");
        Assert.assertEquals(action.getValidationContext().getValidationContexts().size(), 1L);

        // 4th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("customSoapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1004");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().get(0), "classpath:org/citrusframework/ws/actions/test-fault-detail.xml");
        Assert.assertEquals(action.getValidationContext().getValidationContexts().size(), 1L);

        // 5th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1005");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "FaultDetail");

        Assert.assertEquals(action.getValidationContext().getValidationContexts().size(), 1L);

        SoapFaultDetailValidationContext detailValidationContext = action.getValidationContext().getValidationContexts().get(0);
        Assert.assertTrue(detailValidationContext.isSchemaValidationEnabled());
        Assert.assertEquals(detailValidationContext.getSchemaRepository(), "fooSchemaRepository");
        Assert.assertNull(detailValidationContext.getSchema());

        // 6th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1006");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "FaultDetail");
        Assert.assertEquals(action.getValidationContext().getValidationContexts().size(), 1L);

        detailValidationContext = action.getValidationContext().getValidationContexts().get(0);
        Assert.assertTrue(detailValidationContext.isSchemaValidationEnabled());
        Assert.assertNull(detailValidationContext.getSchemaRepository());
        Assert.assertEquals(detailValidationContext.getSchema(), "fooSchema");

        // 7th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1007");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "FaultDetail");
        Assert.assertEquals(action.getValidationContext().getValidationContexts().size(), 1L);

        detailValidationContext = action.getValidationContext().getValidationContexts().get(0);
        Assert.assertFalse(detailValidationContext.isSchemaValidationEnabled());
        Assert.assertNull(detailValidationContext.getSchemaRepository());
        Assert.assertNull(detailValidationContext.getSchema());
    }
}
