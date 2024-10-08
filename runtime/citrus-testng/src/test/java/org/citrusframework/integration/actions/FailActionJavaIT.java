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

package org.citrusframework.integration.actions;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.FailAction.Builder.fail;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.container.Assert.Builder.assertException;

@Test
public class FailActionJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void failAction() {
        run(assertException()
            .exception(CitrusRuntimeException.class)
            .message("Failing IT")
            .when(fail("Failing IT")));

        run(assertException()
                .exception(CitrusRuntimeException.class).message("@startsWith('Missing asserted exception')@")
                .when(assertException()
                        .exception(CitrusRuntimeException.class)
                        .when(sleep().milliseconds(500L))
        ));
    }
}
