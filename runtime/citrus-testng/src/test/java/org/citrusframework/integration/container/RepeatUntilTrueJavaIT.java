/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.integration.container;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.RepeatUntilTrue.Builder.repeat;

/**
 * @author Christoph Deppisch
 */
@Test
public class RepeatUntilTrueJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void repeatContainer() {
        variable("max", "3");

        run(repeat().until("i gt citrus:randomNumber(1)").index("i").actions(echo("index is: ${i}")));

        run(repeat().until("i gt= 5").index("i").actions(echo("index is: ${i}")));

        run(repeat().until("(i gt 5) or (i = 5)").index("i").actions(echo("index is: ${i}")));

        run(repeat().until("(i gt 5) and (i gt 3)").index("i").actions(echo("index is: ${i}")));

        run(repeat().until("i gt 0").index("i").actions(echo("index is: ${i}")));

        run(repeat().until("${max} lt i").index("i").actions(echo("index is: ${i}")));
    }
}
