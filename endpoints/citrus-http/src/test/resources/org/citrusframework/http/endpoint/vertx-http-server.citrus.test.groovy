/*
 * Copyright the original author or authors.
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
package org.citrusframework.http.endpoint

name "VertxHttpServerGroovyEndpointIT"
author "Citrus"
status "FINAL"
description "Creates the Vert.x based Http server with the Groovy endpoint builder"

actions {
    $(groovy().endpoints().script("""
        http() {
            vertxServer {
                name = 'groovyVertxHttpServer'
                port = 19303
                autoStart = true
                timeout = 5000L
            }
        }
        http() {
            client {
                name = 'groovyVertxHttpClient'
                requestUrl = 'http://localhost:19303'
            }
        }
    """))

    $(http().client("groovyVertxHttpClient")
        .send()
        .get()
            .fork(true)
            .uri('http://localhost:19303')
            .path('/orders/42')
            .message()
                .contentType("text/plain")
    )

    $(http().server("groovyVertxHttpServer")
        .receive()
        .get()
            .path('/orders/42')
            .timeout(5000L)
    )

    $(http().server("groovyVertxHttpServer")
        .send()
        .response()
            .message()
                .statusCode(200)
                .contentType("text/plain")
                .body('Vert.x server response')
    )

    $(http().client("groovyVertxHttpClient")
        .receive()
        .response()
            .timeout(5000L)
            .message()
                .statusCode(200)
    )
}
