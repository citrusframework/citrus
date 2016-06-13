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

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.container.IteratingConditionExpression;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * @author Christoph Deppisch
 */
@Test
public class IterateTestRunnerIT extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void iterateContainer() {
        variable("max", "3");
        
        iterate().condition("i lt= citrus:randomNumber(1)").index("i")
                .actions(echo("index is: ${i}"));

        iterate().condition(lessThanOrEqualTo(20)).actions(echo("index is: ${i}"));

        iterate().condition(new IteratingConditionExpression() {
                    @Override
                    public boolean evaluate(int index, TestContext context) {
                        return index < 20;
                    }
                }).actions(echo("index is: ${i}"));
        
        iterate().condition("i lt 20").index("i")
                .actions(echo("index is: ${i}"));
        
        iterate().condition("(i lt 5) or (i = 5)").index("i")
                .actions(echo("index is: ${i}"));
        
        iterate().condition("(i lt 5) and (i lt 3)").index("i")
                .actions(echo("index is: ${i}"));
        
        iterate().condition("i = 0").index("i")
                .actions(echo("index is: ${i}"));
        
        iterate().condition("${max} gt= i").index("i")
                .actions(echo("index is: ${i}"));

        iterate().condition("i lt= 50").index("i")
                        .startsWith(0)
                        .step(5)
                .actions(echo("index is: ${i}"));

        iterate().condition(new IteratingConditionExpression() {
                            @Override
                            public boolean evaluate(int index, TestContext context) {
                                return index < 50;
                            }
                        })
                        .startsWith(0)
                        .step(5)
                .actions(echo("index is: ${i}"));

        AbstractTestAction anonymous = new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                log.info(context.getVariable("index"));
            }
        };

        iterate().condition("i lt 5").index("i")
                .actions(createVariable("index", "${i}"), anonymous);
    }
}