/*
 * Copyright 2023 the original author or authors.
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

package org.citrusframework.openapi.groovy

import org.springframework.http.HttpStatus

import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi

name "OpenApiClientTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

variables {
    petId="12345"
}

actions {
    $(openapi().specification("classpath:org/citrusframework/openapi/petstore/petstore-v3.yaml")
        .client(httpClient)
        .send("getPetById"))
    $(openapi().specification("classpath:org/citrusframework/openapi/petstore/petstore-v3.yaml")
        .client("httpClient")
        .receive("getPetById", HttpStatus.OK))

    $(openapi().specification("classpath:org/citrusframework/openapi/petstore/petstore-v3.yaml")
        .client(httpClient)
        .send("addPet"))
    $(openapi().specification("classpath:org/citrusframework/openapi/petstore/petstore-v3.yaml")
        .client("httpClient")
        .receive("addPet", HttpStatus.CREATED))


}
