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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.springframework.util.StringUtils;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.is;

/**
 * @author Christoph Deppisch
 */
@Test
public class RepeatUntilTrueTestRunnerIT extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void repeatUntilTrueContainer() {
        variable("max", "3");
        
        repeat().until("i gt citrus:randomNumber(1)").index("i")
                .actions(echo("index is: ${i}"));

        repeat().until((index, context) -> index >= 5 && StringUtils.hasText(context.getVariable("max")))
                .actions(echo("index is: ${i}"));

        repeat().until(is(5))
                .actions(echo("index is: ${i}"));
        
        repeat().until("i gt= 5").index("i")
                .actions(echo("index is: ${i}"));
        
        repeat().until("(i gt 5) or (i = 5)").index("i")
                .actions(echo("index is: ${i}"));
        
        repeat().until("(i gt 5) and (i gt 3)").index("i")
                .actions(echo("index is: ${i}"));
        
        repeat().until("i gt 0").index("i")
                .actions(echo("index is: ${i}"));
        
        repeat().until("${max} lt i").index("i")
                .actions(echo("index is: ${i}"));

        repeat().until((index, context) -> Integer.valueOf(context.getVariable("max")) > index)
                .actions(echo("index is: ${i}"));
    }
}