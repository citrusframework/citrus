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

package com.consol.citrus.ws.config.xml;

import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultDetailValidationContext;
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
        Assert.assertEquals(action.getFaultCode(), "{http://www.citrusframework.org/faults}FAULT-1001");
        Assert.assertNull(action.getFaultString());
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertNull(action.getValidationContext());
        
        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://www.citrusframework.org/faults}FAULT-1002");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertNull(action.getValidationContext());
        
        // 3rd action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://www.citrusframework.org/faults}FAULT-1003");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "FaultDetail");
        Assert.assertEquals(((SoapFaultDetailValidationContext)action.getValidationContext()).getValidationContexts().size(), 1L);
        
        // 4th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("customSoapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://www.citrusframework.org/faults}FAULT-1004");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().get(0), "classpath:com/consol/citrus/ws/actions/test-fault-detail.xml");
        Assert.assertEquals(((SoapFaultDetailValidationContext)action.getValidationContext()).getValidationContexts().size(), 1L);
        
        // 5th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://www.citrusframework.org/faults}FAULT-1003");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "FaultDetail");
        
        Assert.assertEquals(((SoapFaultDetailValidationContext)action.getValidationContext()).getValidationContexts().size(), 1L);
        
        ValidationContext xmlValidationContext = ((SoapFaultDetailValidationContext)action.getValidationContext()).getValidationContexts().get(0);
        Assert.assertTrue(xmlValidationContext instanceof XmlMessageValidationContext);
        Assert.assertTrue(((XmlMessageValidationContext)xmlValidationContext).isSchemaValidationEnabled());
        Assert.assertEquals(((XmlMessageValidationContext)xmlValidationContext).getSchemaRepository(), "fooSchemaRepository");
        Assert.assertNull(((XmlMessageValidationContext)xmlValidationContext).getSchema());
        
        // 6th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://www.citrusframework.org/faults}FAULT-1003");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "FaultDetail");
        Assert.assertEquals(((SoapFaultDetailValidationContext)action.getValidationContext()).getValidationContexts().size(), 1L);
        
        xmlValidationContext = ((SoapFaultDetailValidationContext)action.getValidationContext()).getValidationContexts().get(0);
        Assert.assertTrue(xmlValidationContext instanceof XmlMessageValidationContext);
        Assert.assertTrue(((XmlMessageValidationContext)xmlValidationContext).isSchemaValidationEnabled());
        Assert.assertNull(((XmlMessageValidationContext)xmlValidationContext).getSchemaRepository());
        Assert.assertEquals(((XmlMessageValidationContext)xmlValidationContext).getSchema(), "fooSchema");
        
        // 7th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), beanDefinitionContext.getBean("soapFaultValidator"));
        Assert.assertEquals(action.getFaultCode(), "{http://www.citrusframework.org/faults}FAULT-1003");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "FaultDetail");
        Assert.assertEquals(((SoapFaultDetailValidationContext)action.getValidationContext()).getValidationContexts().size(), 1L);
        
        xmlValidationContext = ((SoapFaultDetailValidationContext)action.getValidationContext()).getValidationContexts().get(0);
        Assert.assertTrue(xmlValidationContext instanceof XmlMessageValidationContext);
        Assert.assertFalse(((XmlMessageValidationContext)xmlValidationContext).isSchemaValidationEnabled());
        Assert.assertNull(((XmlMessageValidationContext)xmlValidationContext).getSchemaRepository());
        Assert.assertNull(((XmlMessageValidationContext)xmlValidationContext).getSchema());
    }
}
