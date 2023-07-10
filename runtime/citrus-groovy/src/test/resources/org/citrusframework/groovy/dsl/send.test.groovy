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

package org.citrusframework.groovy.dsl

import org.citrusframework.message.MessageType

import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders
import static org.citrusframework.dsl.PathExpressionSupport.path
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath

name "SendTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(send()
        .endpoint(helloEndpoint)
        .message()
        .header("operation", "sayHello")
        .body("Hello from Citrus!")
    )

    $(send()
        .endpoint("helloEndpoint")
        .message()
        .body("<TestMessage>Hello Citrus</TestMessage>")
        .header("operation", "sayHello")
    )

    $(send()
        .endpoint("helloEndpoint")
        .message()
        .body("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <TestMessage xmlns="http://citrusframework.org/test">Hello Citrus</TestMessage>
        """)
        .header("operation", "sayHello")
        .header("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <Header xmlns="http://citrusframework.org/test"><operation>hello</operation></Header>
        """)
    )

    $(send()
        .endpoint("helloEndpoint")
        .message()
        .body()
        .resource("classpath:org/citrusframework/groovy/test-request-payload.xml")
    )

    $(send()
        .endpoint("helloEndpoint")
        .message()
        .header("operation", "sayHello")
        .dictionary(myDataDictionary)
        .body("<TestMessage>Hello Citrus</TestMessage>")
        .process(path().expression("/TestMessage/text()", "newValue"))
        .extract(fromHeaders().header("operation", "operation"))
    )

    $(send()
        .endpoint("direct:helloQueue")
        .message()
        .body("<TestMessage>Hello Citrus</TestMessage>")
        .header("intValue", 5)
        .header("longValue", 10L)
        .header("floatValue", 10.0F)
        .header("doubleValue", 10.0D)
        .header("byteValue", (byte) 1)
        .header("shortValue", (short) 10)
        .header("boolValue", true)
        .header("stringValue", "Hello Citrus")
    )

    $(send()
        .endpoint("helloEndpoint")
        .message()
        .body('{ "FooMessage": { "foo": "Hello World!" }, { "bar": "@ignore@" }}')
        .type(MessageType.JSON)
        .process(jsonPath().expression('$.FooMessage.foo', "newValue"))
    )

    $(send()
        .endpoint("helloEndpoint")
        .message()
        .type(MessageType.JSON)
        .schemaValidation(true)
        .schema("fooSchema")
        .schemaRepository("fooRepository")
        .body('{ "FooMessage": { "foo": "Hello World!" }, { "bar": "@ignore@" }}')
    )

    $(send()
        .endpoint("helloEndpoint")
        .message()
        .type(MessageType.XML)
        .schemaValidation(true)
        .schema("fooSchema")
        .schemaRepository("fooRepository")
        .body('<TestMessage>Hello Citrus</TestMessage>')
    )

}
