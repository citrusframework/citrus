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

package org.citrusframework.camel.groovy

import static org.citrusframework.camel.dsl.CamelSupport.camel

name "CamelStopRouteTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(camel()
        .route()
        .stop("route_1"))

    $(camel()
        .camelContext(camelContext)
        .route()
        .stop("route_2", "route_3"))
}
