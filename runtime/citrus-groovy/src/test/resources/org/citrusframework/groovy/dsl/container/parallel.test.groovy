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

package org.citrusframework.groovy.dsl.container

import static org.citrusframework.actions.EchoAction.Builder.echo
import static org.citrusframework.container.Parallel.Builder.parallel

name "ParallelTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(parallel()
        .actions(
            echo().message("1"),
            echo().message("2"),
        ))

    $(parallel()
        .actions(
            parallel()
                .actions(
                        echo().message("1"),
                        echo().message("2"),
                ),
            echo().message("3"),
            echo().message("4")
        ))
}