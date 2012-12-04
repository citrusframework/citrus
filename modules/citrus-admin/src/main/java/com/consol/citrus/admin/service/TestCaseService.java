/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.admin.service;

import java.util.*;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 *
 * @author Christoph Deppisch
 */
@Component
public class TestCaseService {

    /**
     * Finds all test cases in classpath starting in given base package. Searches for 
     * **.class files extending AbstractTestNGCitrusTest superclass.
     * 
     * @param basePackage
     * @return
     */
    public List<String> findTestsInClasspath(String basePackage) {
        List<String> testCaseNames = new ArrayList<String>();
        
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, new StandardServletEnvironment());
        
        scanner.addIncludeFilter(new CitrusTestTypeFilter());
        
        Set<BeanDefinition> findings = scanner.findCandidateComponents(basePackage);
        
        for (BeanDefinition bean : findings) {
            testCaseNames.add(bean.getBeanClassName());
        }
        
        return testCaseNames;
    }
    
    /**
     * Class type filter searches for subclasses of {@link AbstractTestNGCitrusTest}
     */
    private static final class CitrusTestTypeFilter extends AbstractClassTestingTypeFilter {
        @Override
        protected boolean match(ClassMetadata metadata) {
            return !metadata.getClassName().equals(TestNGCitrusTestBuilder.class.getName()) && 
                    metadata.getSuperClassName().equals(AbstractTestNGCitrusTest.class.getName());
        }
    }
}
