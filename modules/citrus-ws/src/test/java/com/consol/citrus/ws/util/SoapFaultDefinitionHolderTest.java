/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ws.util;

import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class SoapFaultDefinitionHolderTest {

    @Test
    public void testFromString() {
        SoapFaultDefinitionHolder holder = SoapFaultDefinitionHolder.fromString("{TEC-1000}");
        
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getLocalPart(), "TEC-1000");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getNamespaceURI(), "");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getPrefix(), "");
        Assert.assertNull(holder.getSoapFaultDefinition().getFaultStringOrReason());
        Assert.assertEquals(holder.getSoapFaultDefinition().getLocale(), Locale.ENGLISH);
        Assert.assertNull(holder.getFaultActor());
        
        holder = SoapFaultDefinitionHolder.fromString("{{http://citrusframework.org}TEC-1000}");
        
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getLocalPart(), "TEC-1000");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getNamespaceURI(), "http://citrusframework.org");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getPrefix(), "");
        Assert.assertNull(holder.getSoapFaultDefinition().getFaultStringOrReason());
        Assert.assertEquals(holder.getSoapFaultDefinition().getLocale(), Locale.ENGLISH);
        Assert.assertNull(holder.getFaultActor());
        
        holder = SoapFaultDefinitionHolder.fromString("{{http://citrusframework.org}CITRUS:TEC-1000}");
        
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getLocalPart(), "TEC-1000");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getNamespaceURI(), "http://citrusframework.org");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getPrefix(), "CITRUS");
        Assert.assertNull(holder.getSoapFaultDefinition().getFaultStringOrReason());
        Assert.assertEquals(holder.getSoapFaultDefinition().getLocale(), Locale.ENGLISH);
        Assert.assertNull(holder.getFaultActor());
        
        holder = SoapFaultDefinitionHolder.fromString("{TEC-1000}{Internal server error}");
        
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getLocalPart(), "TEC-1000");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getNamespaceURI(), "");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getPrefix(), "");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultStringOrReason(), "Internal server error");
        Assert.assertEquals(holder.getSoapFaultDefinition().getLocale(), Locale.ENGLISH);
        Assert.assertNull(holder.getFaultActor());
        
        holder = SoapFaultDefinitionHolder.fromString("{{http://citrusframework.org}CITRUS:TEC-1000}{Internal server error}");
        
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getLocalPart(), "TEC-1000");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getNamespaceURI(), "http://citrusframework.org");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getPrefix(), "CITRUS");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultStringOrReason(), "Internal server error");
        Assert.assertEquals(holder.getSoapFaultDefinition().getLocale(), Locale.ENGLISH);
        Assert.assertNull(holder.getFaultActor());
        
        holder = SoapFaultDefinitionHolder.fromString("{TEC-1000}{Interner Fehler}{DE}");
        
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getLocalPart(), "TEC-1000");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getNamespaceURI(), "");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getPrefix(), "");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultStringOrReason(), "Interner Fehler");
        Assert.assertEquals(holder.getSoapFaultDefinition().getLocale(), Locale.GERMAN);
        Assert.assertNull(holder.getFaultActor());
        
        holder = SoapFaultDefinitionHolder.fromString("{TEC-1000}{Interner Fehler}{DE}{Actor}");
        
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getLocalPart(), "TEC-1000");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getNamespaceURI(), "");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getPrefix(), "");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultStringOrReason(), "Interner Fehler");
        Assert.assertEquals(holder.getSoapFaultDefinition().getLocale(), Locale.GERMAN);
        Assert.assertEquals(holder.getFaultActor(), "Actor");
        
        holder = SoapFaultDefinitionHolder.fromString("{TEC-1000}{Interner Fehler}{}{Actor}");
        
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getLocalPart(), "TEC-1000");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getNamespaceURI(), "");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultCode().getPrefix(), "");
        Assert.assertEquals(holder.getSoapFaultDefinition().getFaultStringOrReason(), "Interner Fehler");
        Assert.assertEquals(holder.getSoapFaultDefinition().getLocale(), Locale.ENGLISH);
        Assert.assertEquals(holder.getFaultActor(), "Actor");
    }
    
    @Test
    public void testToString() {
        SoapFaultDefinitionHolder holder = new SoapFaultDefinitionHolder();
        holder.setFaultCode("TEC-1000");
        
        Assert.assertEquals(holder.toString(), "{TEC-1000}");
        
        holder.setFaultStringOrReason("Internal server error");
        
        Assert.assertEquals(holder.toString(), "{TEC-1000}{Internal server error}{en}");
        
        holder.setFaultCode("{http://citrusframework.org}TEC-1000");
        
        Assert.assertEquals(holder.toString(), "{{http://citrusframework.org}TEC-1000}{Internal server error}{en}");
        
        holder.setLocale("DE");
        
        Assert.assertEquals(holder.toString(), "{{http://citrusframework.org}TEC-1000}{Internal server error}{de}");
        
        holder.setFaultActor("Actor");
        
        Assert.assertEquals(holder.toString(), "{{http://citrusframework.org}TEC-1000}{Internal server error}{de}{Actor}");
    }
    
    @Test
    public void testInvalidSyntax() {
        try {
            SoapFaultDefinitionHolder.fromString("{http://citrusframework.org}TEC-1000");
            Assert.fail("Missing exception due to invalid SOAP fault expression syntax");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid"));
            Assert.assertTrue(e.getMessage().contains("syntax"));
        }
        
        try {
            SoapFaultDefinitionHolder.fromString("");
            Assert.fail("Missing exception due to invalid SOAP fault expression syntax");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid"));
            Assert.assertTrue(e.getMessage().contains("syntax"));
        }
        
        try {
            SoapFaultDefinitionHolder.fromString("TEC-1000");
            Assert.fail("Missing exception due to invalid SOAP fault expression syntax");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid"));
            Assert.assertTrue(e.getMessage().contains("syntax"));
        }
    }
}
