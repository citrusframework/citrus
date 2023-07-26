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

name "SendSoapFaultTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(soap()
        .server("soapServer")
        .receive()
        .message()
            .body("<TestMessage>Hello Citrus</TestMessage>")
    )

    $(soap()
        .server("soapServer")
        .sendFault()
        .message()
            .faultCode("{http://citrusframework.org/faults}citrus-ns:FAULT-1000")
            .faultString("FaultString")
            .faultDetail("""<ns0:FaultDetail xmlns:ns0="http://citrusframework.org/schemas/samples/HelloService.xsd">
                            <ns0:DetailId>1000</ns0:DetailId>
                        </ns0:FaultDetail>""")
            .header("operation", "sendFault")
    )

    $(soap()
        .server("soapServer")
        .receive()
        .message()
            .body("<TestMessage>Hello Citrus</TestMessage>")
    )

    $(soap()
        .server("soapServer")
        .sendFault()
        .message()
            .faultCode("{http://citrusframework.org/faults}citrus-ns:FAULT-1001")
            .faultString("FaultString")
            .faultActor("FaultActor")
            .faultDetailResource("classpath:org/citrusframework/ws/actions/test-fault-detail.xml")
            .header("operation", "sendFault")
    )
}
