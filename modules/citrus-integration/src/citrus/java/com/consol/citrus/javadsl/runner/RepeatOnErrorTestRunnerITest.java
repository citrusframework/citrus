/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.javadsl.runner;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.builder.RepeatOnErrorBuilder;
import com.consol.citrus.dsl.runner.TestActionConfigurer;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class RepeatOnErrorTestRunnerITest extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void RepeatOnErrorTestRunnerITest() {
        variable("message", "Hello TestFramework");
        
        repeatOnError(new TestActionConfigurer<RepeatOnErrorBuilder>() {
            @Override
            public void configure(RepeatOnErrorBuilder builder) {
                builder.until("i = 5").index("i");
            }
        }).actions(echo("${i}. Versuch: ${message}"));
        
        repeatOnError(new TestActionConfigurer<RepeatOnErrorBuilder>() {
            @Override
            public void configure(RepeatOnErrorBuilder builder) {
                builder.until("i = 5").index("i").autoSleep(500);
            }
        }).actions(echo("${i}. Versuch: ${message}"));
        
        assertException().when(
                repeatOnError(new TestActionConfigurer<RepeatOnErrorBuilder>() {
                    @Override
                    public void configure(RepeatOnErrorBuilder builder) {
                        builder.until("i = 3").index("i").autoSleep(200);
                    }
                }).actions(
                        echo("${i}. Versuch: ${message}"),
                        fail("")
                )
        );
        
    }
}