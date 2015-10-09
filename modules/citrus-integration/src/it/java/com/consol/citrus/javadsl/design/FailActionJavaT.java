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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class FailActionJavaT extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void failAction() {
        assertException(fail("Failing IT"))
            .exception(CitrusRuntimeException.class)
            .message("Failing IT");
        
        assertException(
                assertException(sleep(500))
                    .exception(CitrusRuntimeException.class)
        ).exception(CitrusRuntimeException.class).message("@startsWith('Missing asserted exception')@");
    }
}