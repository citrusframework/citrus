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

import static org.citrusframework.dsl.MessageSupport.MessageBodySupport.fromBody
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders
import static org.citrusframework.dsl.PathExpressionSupport.path
import static org.citrusframework.validation.json.JsonMessageValidationContext.Builder.json
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath
import static org.citrusframework.validation.script.ScriptValidationContext.Builder.groovy
import static org.citrusframework.validation.xml.XmlMessageValidationContext.Builder.xml
import static org.citrusframework.validation.xml.XpathMessageValidationContext.Builder.xpath

name "ReceiveTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(receive()
            .endpoint(helloEndpoint)
            .timeout(10000L)
            .message()
            .header("operation", "sayHello")
            .body("Hello from Citrus!")
    )

    $(receive()
            .endpoint("helloEndpoint")
            .message()
            .body("<TestMessage>Hello Citrus</TestMessage>")
            .header("operation", "sayHello")
    )

    $(receive()
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

    $(receive()
            .endpoint("helloEndpoint")
            .timeout(1000L)
            .selector(["operation": "sayHello"])
            .message()
                .body()
                    .resource("classpath:org/citrusframework/groovy/test-request-payload.xml")
    )

    $(receive()
            .endpoint("helloEndpoint")
            .selector("operation = 'sayHello'")
            .message()
            .body("<TestMessage>Hello Citrus</TestMessage>")
    )

    $(receive()
            .endpoint("helloEndpoint")
            .message()
            .dictionary(myDataDictionary)
            .body("<TestMessage>Hello Citrus</TestMessage>")
            .extract(fromHeaders().header("operation", "operation"))
            .extract(fromBody().expression("/TestMessage/text()", "text"))
    )

    $(receive()
            .endpoint("helloEndpoint")
            .message()
            .body("<ns:TestMessage xmlns:ns=\"http://citrusframework.org\">Hello Citrus</ns:TestMessage>")
            .process(path().expression("/ns:TestMessage/", "newValue"))
            .validate(xml()
                    .ignore("/ns:TestMessage/ns:ignore")
                    .namespaceContext("ctx", "http://citrusframework.org/test")
                    .namespace("ns", "http://citrusframework.org")
                    .schemaValidation(false))
    )

    $(receive()
            .endpoint("direct:helloQueue")
            .message()
            .validate(path()
                    .expression("/TestMessage/text", "Hello Citrus")
                    .expression("/TestMessage/foo", true))
    )

    $(receive()
            .endpoint("direct:helloQueue")
            .message()
            .validate(xpath()
                    .expression("/TestMessage/text", "Hello Citrus")
                    .expression("/TestMessage/foo", true))
    )

    $(receive()
            .endpoint("direct:helloQueue")
            .message()
            .validate(groovy().script("assert true"))
            .validate(path().expression("/TestMessage/foo", true))
    )

    $(receive()
            .endpoint("direct:helloQueue")
            .message()
            .validate(groovy().scriptResource("classpath:org/citrusframework/groovy/test-validation-script.groovy"))
    )

    $(receive()
            .endpoint("direct:helloQueue")
            .message()
            .type(MessageType.JSON)
            .validate(path()
                    .expression('$.json.text', "Hello Citrus")
                    .expression('$..foo.bar', true))
    )

    $(receive()
            .endpoint("direct:helloQueue")
            .message()
            .type(MessageType.JSON)
            .validate(jsonPath()
                    .expression('$.json.text', "Hello Citrus")
                    .expression('$..foo.bar', true))
    )

    $(receive()
            .endpoint("direct:helloQueue")
            .message()
            .body('{ "FooMessage": { "foo": "Hello World!" }, { "bar": "@ignore@" }}')
            .type(MessageType.JSON)
            .validate(json().ignore('$.FooMessage.bar'))
            .process(jsonPath().expression('$.FooMessage.foo', "newValue"))
    )

    $(receive()
            .endpoint("helloEndpoint")
            .message()
            .type(MessageType.JSON)
            .body('{ "message": { "text": "Hello World!" }, { "bar": "@ignore@" }}')
            .extract(fromHeaders().expression("operation", "operation"))
            .extract(fromBody().expression('$.message.text', "text"))
    )

    $(receive()
            .endpoint("helloEndpoint")
            .message()
            .body('<TestMessage>Hello Citrus</TestMessage>')
            .validator(myValidator)
            .validator(myHeaderValidator)
            .header("operation", "sayHello")
    )

    $(receive()
            .endpoint("helloEndpoint")
            .message()
            .body('<TestMessage>Hello Citrus</TestMessage>')
            .validators(myValidator,defaultMessageValidator)
            .validators(myHeaderValidator,defaultHeaderValidator)
            .header("operation", "sayHello")
    )
}
