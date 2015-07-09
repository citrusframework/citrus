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
import com.consol.citrus.dsl.definition.RepeatUntilTrueDefinition;
import com.consol.citrus.dsl.runner.TestActionConfigurer;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class RepeatUntilTrueTestRunnerITest extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void RepeatUntilTrueTestRunnerITest() {
        variable("max", "3");
        
        repeat(new TestActionConfigurer<RepeatUntilTrueDefinition>() {
            @Override
            public void configure(RepeatUntilTrueDefinition definition) {
                definition.until("i gt citrus:randomNumber(1)").index("i");
            }
        }).actions(echo("index is: ${i}"));
        
        repeat(new TestActionConfigurer<RepeatUntilTrueDefinition>() {
            @Override
            public void configure(RepeatUntilTrueDefinition definition) {
                definition.until("i gt= 5").index("i");
            }
        }).actions(echo("index is: ${i}"));
        
        repeat(new TestActionConfigurer<RepeatUntilTrueDefinition>() {
            @Override
            public void configure(RepeatUntilTrueDefinition definition) {
                definition.until("(i gt 5) or (i = 5)").index("i");
            }
        }).actions(echo("index is: ${i}"));
        
        repeat(new TestActionConfigurer<RepeatUntilTrueDefinition>() {
            @Override
            public void configure(RepeatUntilTrueDefinition definition) {
                definition.until("(i gt 5) and (i gt 3)").index("i");
            }
        }).actions(echo("index is: ${i}"));
        
        repeat(new TestActionConfigurer<RepeatUntilTrueDefinition>() {
            @Override
            public void configure(RepeatUntilTrueDefinition definition) {
                definition.until("i gt 0").index("i");
            }
        }).actions(echo("index is: ${i}"));
        
        repeat(new TestActionConfigurer<RepeatUntilTrueDefinition>() {
            @Override
            public void configure(RepeatUntilTrueDefinition definition) {
                definition.until("${max} lt i").index("i");
            }
        }).actions(echo("index is: ${i}"));
    }
}