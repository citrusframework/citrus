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

package com.consol.citrus.javadsl;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class CatchJavaITest extends TestNGCitrusTestBuilder {
    
    @Override
    protected void configure() {
        catchException(fail("Fail!"));
        
        catchException("com.consol.citrus.exceptions.CitrusRuntimeException", fail("Fail!"));
        
        catchException(CitrusRuntimeException.class, fail("Fail!"));
    }
    
    @Test
    public void catchITest(ITestContext testContext) {
        executeTest(testContext);
    }
}