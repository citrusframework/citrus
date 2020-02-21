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

package com.consol.citrus.config;

import com.consol.citrus.TestCase;
import com.consol.citrus.report.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
@Configuration
public class CustomBeanConfig {

    @Bean(name = "customTestListener")
    public TestListener customTestListener() {
        return new PlusMinusTestReporter();
    }

    /**
     * Sample test reporter.
     */
    private static class PlusMinusTestReporter extends AbstractTestListener implements TestReporter {

        /** Logger */
        private Logger log = LoggerFactory.getLogger(CustomBeanConfig.class);

        private StringBuilder testReport = new StringBuilder();

        @Override
        public void onTestSuccess(TestCase test) {
            testReport.append("+");
        }

        @Override
        public void onTestFailure(TestCase test, Throwable cause) {
            testReport.append("-");
        }

        @Override
        public void generateTestResults() {
            log.info(testReport.toString());
        }

        @Override
        public void clearTestResults() {
            testReport = new StringBuilder();
        }
    }
}
