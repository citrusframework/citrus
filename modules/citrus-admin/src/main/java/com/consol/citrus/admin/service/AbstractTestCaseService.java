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

package com.consol.citrus.admin.service;

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.model.TestCaseDetail;
import com.consol.citrus.admin.spring.model.SpringBeans;
import com.consol.citrus.model.testcase.core.Testcase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.StringUtils;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;

/**
 * Abstract test case service provides common implementations for filesystem and classpath service.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractTestCaseService implements TestCaseService {

    @Autowired
    @Qualifier("jaxbMarshaller")
    private Unmarshaller unmarshaller;

    /**
     * Gets test case details such as status, description, author.
     * @return
     */
    public TestCaseDetail getTestDetail(String packageName, String testName) {
        // TODO also get testng groups from java part
        TestCaseDetail testCase = new TestCaseDetail();
        testCase.setName(testName);
        testCase.setPackageName(packageName);

        String xmlPart = getSourceCode(packageName, testName, "xml");

        Testcase test;
        if (StringUtils.hasText(xmlPart)) {
            try {
                test = ((SpringBeans) unmarshaller.unmarshal(new StringSource(xmlPart))).getTestcase();
            } catch (IOException e) {
                throw new CitrusAdminRuntimeException("Failed to unmarshal test case from Spring XML bean definition", e);
            }
        } else {
            test = new Testcase();
            test.setName(testName);
        }

        testCase.setDetail(test);

        return testCase;
    }
}
