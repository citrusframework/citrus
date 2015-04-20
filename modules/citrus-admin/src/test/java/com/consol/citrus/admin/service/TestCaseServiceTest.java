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

import com.consol.citrus.admin.model.*;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(locations = { "classpath:com/consol/citrus/admin/citrus-admin-test-context.xml" })
public class TestCaseServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private TestCaseServiceImpl testCaseService;

    private Project project = EasyMock.createMock(Project.class);

    @Test
    public void testGetTests() throws IOException {
        reset(project);
        expect(project.getProjectHome()).andReturn(new ClassPathResource("test-project").getFile().getAbsolutePath()).atLeastOnce();
        replay(project);

        List<TestCaseData> tests = testCaseService.getTests(project);

        Assert.assertNotNull(tests);
        Assert.assertEquals(tests.size(), 4L);

        TestCaseData test = tests.get(0);
        Assert.assertEquals(test.getName(), "FooTest");
        Assert.assertEquals(test.getPackageName(), "");

        test = tests.get(1);
        Assert.assertEquals(test.getName(), "BarTest");
        Assert.assertEquals(test.getPackageName(), "com.consol.citrus");

        test = tests.get(2);
        Assert.assertEquals(test.getName(), "FooJavaTest");
        Assert.assertEquals(test.getPackageName(), "com.consol.citrus");

        test = tests.get(3);
        Assert.assertEquals(test.getName(), "FooTest");
        Assert.assertEquals(test.getPackageName(), "com.consol.citrus");

        verify(project);
    }
}
