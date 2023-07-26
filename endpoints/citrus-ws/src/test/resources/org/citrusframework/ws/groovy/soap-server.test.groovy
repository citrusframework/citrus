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

import org.citrusframework.util.FileUtils

import java.nio.charset.StandardCharsets

import static org.citrusframework.ws.actions.SoapActionBuilder.soap

name "SoapServerTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(soap()
        .server("soapServer")
        .receive()
        .message()
            .soapAction("myAction")
            .body("<TestMessage>Hello Citrus</TestMessage>")
            .attachment("MySoapAttachment", "text/plain", "This is an attachment!")
    )

    $(soap()
        .server("soapServer")
        .send()
        .message()
            .body('<?xml version="1.0" encoding="UTF-8"?><TestResponse>Hello User</TestResponse>')
            .attachment("MySoapAttachment", "text/plain", "This is an attachment!")
    )

    $(soap()
        .server("soapServer")
        .receive()
        .message()
            .attachmentValidatorName("mySoapAttachmentValidator")
            .body("<TestMessage>Hello Citrus</TestMessage>")
            .attachment("MySoapAttachment", "application/xml",
                    FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml"), StandardCharsets.UTF_8)
    )

    $(soap()
        .server("soapServer")
        .send()
        .message()
            .body('<?xml version="1.0" encoding="UTF-8"?><TestResponse>Hello User</TestResponse>')
            .attachment("MySoapAttachment", "application/xml",
                    FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml"), StandardCharsets.UTF_8)
    )

    $(soap()
        .server("soapServer")
        .receive()
        .message()
            .body("<TestMessage>Hello Citrus</TestMessage>")
            .attachment("FirstSoapAttachment", "text/plain", "This is an attachment!")
            .attachment("SecondSoapAttachment", "application/xml",
                    FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml"), StandardCharsets.UTF_8)
    )

    $(soap()
        .server("soapServer")
        .send()
        .message()
            .body('<?xml version="1.0" encoding="UTF-8"?><TestResponse>Hello User</TestResponse>')
            .attachment("FirstSoapAttachment", "text/plain", "This is an attachment!")
            .attachment("SecondSoapAttachment", "application/xml",
                    FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml"), StandardCharsets.UTF_8)
    )
}
