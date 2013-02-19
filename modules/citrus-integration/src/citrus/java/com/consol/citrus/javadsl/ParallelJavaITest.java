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
public class ParallelJavaITest extends TestNGCitrusTestBuilder {
    
    @Override
    protected void configure() {
        parallel(
            sleep(1.5), 
            sequential(
                sleep(1.0),
                echo("1")
            ),
            echo("2"),
            echo("3"),
            iterate(
                echo("10")
            ).condition("i lt= 5").index("i")
        );
        
        assertException(
            parallel(
                sleep(1.5), 
                sequential(
                    sleep(1.0),
                    fail("This went wrong too"),
                    echo("1")
                ),
                echo("2"),
                fail("This went wrong too"),
                echo("3"),
                iterate(
                    echo("10")
                ).condition("i lt= 5").index("i")
            )
        ).exception(CitrusRuntimeException.class);
    }
    
    @Test
    public void parallelITest(ITestContext testContext) {
        executeTest(testContext);
    }
}