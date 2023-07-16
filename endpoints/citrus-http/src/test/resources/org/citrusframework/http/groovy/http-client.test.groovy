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

import static org.citrusframework.actions.SleepAction.Builder.delay
import static org.citrusframework.http.actions.HttpActionBuilder.http

import static org.citrusframework.dsl.PathExpressionSupport.path

name "HttpClientTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

variables {
    id="12345"
}

actions {
    $(http().client(httpClient)
        .send()
        .get())
    $(http().client("httpClient")
        .receive()
        .response())

    $(http().client("httpClient")
        .send()
        .get()
            .fork(true)
            .uri('http://localhost:${port}/test')
            .path('/order/${id}')
            .message()
                .contentType("application/xml")
                .accept("application/xml")
                .version("HTTP/1.1")
                .queryParam("id", '${id}')
                .queryParam("type", "gold")
    )

    $(delay().milliseconds(1000L))

    $(http().client("httpClient")
        .receive()
        .response()
            .message()
                .statusCode(200)
                .reasonPhrase("OK")
                .version("HTTP/1.1")
                .contentType("application/xml")
                .body('<order><id>${id}</id><item>foo</item></order>')
                .extract(path().expression("/order/id", "orderId"))
    )

    $(http().client("httpClient")
        .send()
        .post("/user")
            .message()
            .header("userId", "1001")
            .body("<user><id>1001</id><name>new_user</name></user>")
    )

    $(http().client("httpClient")
        .receive()
            .response()
            .timeout(2000L)
            .message()
                .statusCode(404)
                .reasonPhrase("NOT_FOUND")
                .header("userId", "1001")
    )

    $(http().client("httpClient")
        .send()
        .delete()
            .path('/user/${id}')
    )

    $(http().client("httpClient")
        .send()
        .head()
            .uri('http://localhost:${port}/test')
    )

    $(http().client('http://localhost:${port}/test')
        .send()
        .options()
        .actor(testActor)
    )
}
