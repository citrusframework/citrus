/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.ws.groovy


import static org.citrusframework.ws.actions.SoapActionBuilder.soap

name "AssertSoapFaultTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(soap()
        .client("soapClient")
        .assertFault()
            .faultCode("{http://citrusframework.org/faults}FAULT-1001")
            .when(
                soap()
                    .client("soapClient")
                    .send()
                    .message()
                        .body("<TestMessage>Hello Citrus</TestMessage>")
            )
    )

    $(soap()
        .client("soapClient")
        .assertFault()
            .faultCode("{http://citrusframework.org/faults}FAULT-1002")
            .faultString("FaultString")
            .when(
                soap()
                    .client("soapClient")
                    .send()
                    .message()
                        .body("<TestMessage>Hello Citrus</TestMessage>")
            )
    )

    $(soap()
        .client("soapClient")
        .assertFault()
            .faultCode("{http://citrusframework.org/faults}FAULT-1003")
            .faultString("FaultString")
            .faultActor("FaultActor")
            .faultDetail('<ns0:FaultDetail xmlns:ns0="http://citrusframework.org/schemas/samples/HelloService.xsd"><ns0:DetailId>1000</ns0:DetailId></ns0:FaultDetail>')
            .when(
                soap()
                    .client("soapClient")
                    .send()
                    .message()
                        .body("<TestMessage>Hello Citrus</TestMessage>")
            )
    )

    $(soap()
        .client("soapClient")
        .assertFault()
            .validator("customSoapFaultValidator")
            .faultCode("{http://citrusframework.org/faults}FAULT-1004")
            .faultString("FaultString")
            .faultDetailResource("classpath:org/citrusframework/ws/actions/test-fault-detail.xml")
            .when(
                soap()
                    .client("soapClient")
                    .send()
                    .message()
                        .body("<TestMessage>Hello Citrus</TestMessage>")
            )
    )


}
