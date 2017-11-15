/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.junit.jupiter;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Christoph Deppisch
 */
@ExtendWith(CitrusExtension.class)
public class EchoActionJUnit5JavaIT {

    @Test
    @CitrusTest
    void echoJavaTest(@CitrusResource TestDesigner designer) {
        designer.variable("time", "citrus:currentDate()");

        designer.echo("Hello Citrus!");

        designer.echo("CurrentTime is: ${time}");
    }

    @Test
    @CitrusTest(name = "EchoSampleTest")
    void echoTest(@CitrusResource TestDesigner designer) {
        designer.variable("time", "citrus:currentDate()");

        designer.echo("Hello Citrus!");

        designer.echo("CurrentTime is: ${time}");
    }
}
