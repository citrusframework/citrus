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

package org.citrusframework.groovy.dsl.container

import static org.citrusframework.actions.EchoAction.Builder.echo
import static org.citrusframework.container.RepeatOnErrorUntilTrue.Builder.repeatOnError

name "RepeatOnErrorTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(repeatOnError()
        .until("i > 3")
        .actions(
            echo().message("Hello Citrus!")
        ))

    $(repeatOnError()
        .until("index >= 2")
        .index("index")
        .actions(
            echo().message("Hello Citrus!")
        ))

    $(repeatOnError()
        .until("i >= 10")
        .autoSleep(500L)
        .actions(
            echo().message("Hello Citrus!"),
            echo().message("Hello You!")
        ))

    $(repeatOnError()
        .until("i >= 5")
        .autoSleep(250L)
        .actions(
            echo().message("Hello Citrus!")
        ))
}
