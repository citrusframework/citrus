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

package org.citrusframework.ws.actions;

import java.util.Locale;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.citrusframework.TestAction;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.ws.UnitTestSupport;
import org.citrusframework.ws.validation.SoapFaultValidator;
import org.citrusframework.xml.StringSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.xml.namespace.QNameUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;

/**
 * @author Christoph Deppisch
 */
public class AssertSoapFaultTest extends UnitTestSupport {

    @Autowired
    private SaajSoapMessageFactory messageFactory;

    @Autowired
    private SoapFaultValidator soapFaultValidator;

    @Test
    public void testAssertSoapFault() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            Soap11Fault fault = ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            fault.setFaultActorOrRole("SERVER");

            throw new SoapFaultClientException(faultMessage);
        }).build();

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultActor("SERVER")
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .build();
        assertAction.execute(context);
    }

    @Test
    public void testAssertSoapFaultWithValidatorName() throws Exception {
        TestAction action = action(context -> {
                SoapMessage faultMessage;

                faultMessage = messageFactory.createWebServiceMessage();

                Soap11Fault fault = ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                        "Internal server error",
                        Locale.GERMANY);

                fault.setFaultActorOrRole("SERVER");

                throw new SoapFaultClientException(faultMessage);
        }).build();

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator("soapFaultValidator")
                .withReferenceResolver(new SpringBeanReferenceResolver(applicationContext))
                .when(action)
                .faultActor("SERVER")
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .build();
        assertAction.execute(context);
    }

    @Test
    public void testAssertSoapFaultWithValidationMatchers() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            Soap11Fault fault = ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            fault.setFaultActorOrRole("SERVER");

            throw new SoapFaultClientException(faultMessage);
        }).build();

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultActor("@equalsIgnoreCase('server')@")
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("@equalsIgnoreCase('internal server error')@")
                .build();
        assertAction.execute(context);
    }

    @Test
    public void testNoPrefix() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            throw new SoapFaultClientException(faultMessage);
        }).build();

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultCode("{http://citrusframework.org}TEC-1001")
                .faultString("Internal server error")
                .build();
        assertAction.execute(context);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "SOAP fault validation failed! Fault code does not match - expected: '\\{http://citrusframework.org}TEC-1001' but was: '\\{http://citrusframework.org}TEC-2002'")
    public void testWrongFaultCode() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-2002"),
                    "Internal server error",
                    Locale.GERMANY);

            throw new SoapFaultClientException(faultMessage);
        }).build();

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .build();
        assertAction.execute(context);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "SOAP fault validation failed! Fault actor does not match - expected: 'SERVER' but was: 'CLIENT'")
    public void testWrongFaultActor() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            Soap11Fault fault = ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            fault.setFaultActorOrRole("CLIENT");

            throw new SoapFaultClientException(faultMessage);
        }).build();

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultActor("SERVER")
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .build();
        assertAction.execute(context);
    }

    @Test
    public void testWrongFaultString() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            ((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            throw new SoapFaultClientException(faultMessage);
        }).build();

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Invalid request")
                .build();
        try {
            assertAction.execute(context);
        } catch(ValidationException e) {
            Assert.assertEquals(e.getMessage(), "SOAP fault validation failed! Fault string does not match - expected: 'Invalid request' but was: 'Internal server error'");
            return;
        }

        Assert.fail("Missing validation exception");
    }

    @Test
    public void testAssertSoapFaultDetail() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();

                transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), fault.addFaultDetail().getResult());
            } catch (TransformerException e) {
                throw new CitrusRuntimeException(e);
            }

            throw new SoapFaultClientException(faultMessage);
        }).build();


        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .faultDetail("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>")
                .build();
        assertAction.execute(context);
    }

    @Test
    public void testAssertSoapFaultDetailVariableSupport() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();

                transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), fault.addFaultDetail().getResult());
            } catch (TransformerException e) {
                throw new CitrusRuntimeException(e);
            }

            throw new SoapFaultClientException(faultMessage);
        }).build();

        context.setVariable("faultReason", "Invalid request");

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .faultDetail("<FaultDetail><Reason>${faultReason}</Reason></FaultDetail>")
                .build();
        assertAction.execute(context);
    }

    @Test
    public void testAssertSoapFaultDetailResource() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();

                transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), fault.addFaultDetail().getResult());
            } catch (TransformerException e) {
                throw new CitrusRuntimeException(e);
            }

            throw new SoapFaultClientException(faultMessage);
        }).build();

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .faultDetailResource("classpath:org/citrusframework/ws/actions/test-fault-detail.xml")
                .build();
        assertAction.execute(context);
    }

    @Test
    public void testAssertSoapFaultDetailResourceVariableSupport() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();

                transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), fault.addFaultDetail().getResult());
            } catch (TransformerException e) {
                throw new CitrusRuntimeException(e);
            }

            throw new SoapFaultClientException(faultMessage);
        }).build();

        context.setVariable("faultReason", "Invalid request");

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .faultDetailResource("classpath:org/citrusframework/ws/actions/test-fault-detail-with-variables.xml")
                .build();
        assertAction.execute(context);
    }

    @Test
    public void testAssertMultipleSoapFaultDetails() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            SoapFaultDetail faultDetail = fault.addFaultDetail();
            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();

                transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), faultDetail.getResult());
                transformer.transform(new StringSource("<ErrorDetail><Code>1001</Code></ErrorDetail>"), faultDetail.getResult());
            } catch (TransformerException e) {
                throw new CitrusRuntimeException(e);
            }

            throw new SoapFaultClientException(faultMessage);
        }).build();


        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .faultDetail("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>")
                .faultDetail("<ErrorDetail><Code>1001</Code></ErrorDetail>")
                .build();
        assertAction.execute(context);
    }

    @Test
    public void testAssertMultipleSoapFaultDetailsWithResource() throws Exception {
        TestAction action = action(context -> {
            SoapMessage faultMessage;

            faultMessage = messageFactory.createWebServiceMessage();

            SoapFault fault =((Soap11Body)faultMessage.getSoapBody()).addFault(QNameUtils.parseQNameString("{http://citrusframework.org}ws:TEC-1001"),
                    "Internal server error",
                    Locale.GERMANY);

            SoapFaultDetail faultDetail = fault.addFaultDetail();
            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();

                transformer.transform(new StringSource("<ErrorDetail><Code>1001</Code></ErrorDetail>"), faultDetail.getResult());
                transformer.transform(new StringSource("<FaultDetail><Reason>Invalid request</Reason></FaultDetail>"), faultDetail.getResult());
            } catch (TransformerException e) {
                throw new CitrusRuntimeException(e);
            }

            throw new SoapFaultClientException(faultMessage);
        }).build();

        AssertSoapFault assertAction = new AssertSoapFault.Builder()
                .validator(soapFaultValidator)
                .when(action)
                .faultCode("{http://citrusframework.org}ws:TEC-1001")
                .faultString("Internal server error")
                .faultDetail("<ErrorDetail><Code>1001</Code></ErrorDetail>")
                .faultDetailResource("classpath:org/citrusframework/ws/actions/test-fault-detail.xml")
                .build();
        assertAction.execute(context);
    }
}
