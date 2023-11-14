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

package org.citrusframework.integration.groovy

import org.citrusframework.validation.DefaultTextEqualsMessageValidator


configuration {
    beans {
        bean(DefaultTextEqualsMessageValidator.class)
    }

    queues {
        queue('say-hello')
    }

    endpoints {
        direct('hello') {
            asynchronous()
                    .queue('say-hello')
        }
    }
}

variables {
    foo = "bar"
    num = 1
}

given {
    $(echo("Hello from Groovy!"))
}

when {
    $(send().endpoint(hello)
            .message()
            .body('${foo} #${num}')
    )
}

then {
    $(receive().endpoint(hello)
            .message()
            .body("bar #1")
    )
}

$(echo().message("Test finished successfully!"))

doFinally {
    $(delay().milliseconds(100))
}
