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

package org.citrusframework.http.groovy


import static org.citrusframework.dsl.PathExpressionSupport.path
import static org.citrusframework.http.actions.HttpActionBuilder.http

name "HttpServerTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

variables {
    id="12345"
}

actions {
    $(http().server(httpServer)
        .receive()
        .get())
    $(http().server("httpServer")
        .send()
        .response())

    $(http().server("httpServer")
        .receive()
        .get()
            .path('/test/order/${id}')
            .timeout(2000L)
            .message()
                .contentType("application/xml")
                .accept("application/xml")
                .version("HTTP/1.1")
                .queryParam("id", '${id}')
                .queryParam("type", "gold")
    )

    $(http().server("httpServer")
        .send()
        .response()
            .message()
                .statusCode(200)
                .reasonPhrase("OK")
                .version("HTTP/1.1")
                .contentType("application/xml")
                .body('<order><id>${id}</id><item>foo</item></order>')
    )

    $(http().server("httpServer")
        .receive()
        .post("/user")
            .message()
            .header("userId", "1001")
            .body("<user><id>1001</id><name>new_user</name></user>")
            .extract(path().expression("/user/id", "userId"))
    )

    $(http().server("httpServer")
        .send()
            .response()
            .message()
                .statusCode(404)
                .reasonPhrase("NOT_FOUND")
                .header("userId", "1001")
    )

    $(http().server("httpServer")
        .receive()
        .delete()
            .path('/user/${id}')
    )

    $(http().server("httpServer")
        .receive()
        .head("/test")
    )

    $(http().server("httpServer")
        .receive()
        .options()
        .actor(testActor)
    )
}
