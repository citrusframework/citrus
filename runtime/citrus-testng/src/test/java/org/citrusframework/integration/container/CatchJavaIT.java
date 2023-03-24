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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.FailAction.Builder.fail;
import static org.citrusframework.container.Catch.Builder.catchException;

/**
 * @author Christoph Deppisch
 */
@Test
public class CatchJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void catchAction() {
        run(catchException().when(fail("Fail!")));

        run(catchException()
                .exception(CitrusRuntimeException.class.getName())
                .when(fail("Fail!")));

        run(catchException()
                .exception(CitrusRuntimeException.class)
                .when(fail("Fail!")));
    }
}
