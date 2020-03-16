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

package com.consol.citrus.integration.container;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.FailAction.Builder.fail;
import static com.consol.citrus.actions.SleepAction.Builder.sleep;
import static com.consol.citrus.container.Assert.Builder.assertException;
import static com.consol.citrus.container.Iterate.Builder.iterate;
import static com.consol.citrus.container.Parallel.Builder.parallel;
import static com.consol.citrus.container.Sequence.Builder.sequential;

/**
 * @author Christoph Deppisch
 */
@Test
public class ParallelJavaIT extends TestNGCitrusSupport {

    @CitrusTest
    public void parallelContainer() {
        run(parallel().actions(
            sleep().milliseconds(150),
            sequential().actions(
                sleep().milliseconds(100),
                echo("1")
            ),
            echo("2"),
            echo("3"),
            iterate()
                .condition("i lt= 5").index("i")
                .actions(echo("10"))
        ));

        run(assertException()
            .exception(CitrusRuntimeException.class)
            .when(
                parallel().actions(
                    sleep().milliseconds(150),
                    sequential().actions(
                        sleep().milliseconds(100),
                        fail("This went wrong too"),
                        echo("1")
                    ),
                    echo("2"),
                    fail("This went wrong too"),
                    echo("3"),
                    iterate()
                        .condition("i lt= 5").index("i")
                        .actions(echo("10"))
                )
        ));
    }
}
