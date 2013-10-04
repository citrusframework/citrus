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

package com.consol.citrus.admin.executor;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusCliOptions;
import com.consol.citrus.admin.configuration.ClasspathRunConfiguration;
import com.consol.citrus.admin.service.ConfigurationService;
import com.consol.citrus.report.TestReporter;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Map;

/**
 * Executes a test case from direct classpath using same JVM in which this server web application is running.
 * @author Christoph Deppisch
 */
public class ClasspathTestExecutor implements TestExecutor<ClasspathRunConfiguration> {

    @Autowired
    private ApplicationContextHolder appContextHolder;
    
    @Autowired
    private ConfigurationService configService;

    /**
     * {@inheritDoc}
     */
    public void execute(String testName, ClasspathRunConfiguration configuration) throws ParseException {
        Citrus citrus = new Citrus(new GnuParser().parse(new CitrusCliOptions(), 
                new String[] { "-test", testName, "-testdir", new File(configService.getProjectHome()).getAbsolutePath() }));
        citrus.run();

        if (!appContextHolder.isApplicationContextLoaded()) {
            appContextHolder.loadApplicationContext();
        }
        
        Map<String, TestReporter> reporters = appContextHolder.getApplicationContext().getBeansOfType(TestReporter.class);
        for (TestReporter reporter : reporters.values()) {
            reporter.clearTestResults();
        }
    }

}
