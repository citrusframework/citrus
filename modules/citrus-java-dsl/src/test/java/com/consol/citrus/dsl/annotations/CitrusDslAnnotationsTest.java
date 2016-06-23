/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.dsl.annotations;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusDslAnnotationsTest {

    private TestDesigner designer = Mockito.mock(TestDesigner.class);
    private TestRunner runner = Mockito.mock(TestRunner.class);

    @Test
    public void testInjectTestDesigner() throws Exception {
        TestDesignerClient client = new TestDesignerClient();

        Assert.assertNull(client.getDesigner());
        CitrusDslAnnotations.injectTestDesigner(client, designer);
        Assert.assertEquals(client.getDesigner(), designer);
        Assert.assertEquals(client.otherAnnotatedResource, "foo");
        Assert.assertNull(client.otherDesigner);
    }

    @Test
    public void testInjectTestRunner() throws Exception {
        TestRunnerClient client = new TestRunnerClient();

        Assert.assertNull(client.getRunner());
        CitrusDslAnnotations.injectTestRunner(client, runner);
        Assert.assertEquals(client.getRunner(), runner);
        Assert.assertEquals(client.otherAnnotatedResource, "foo");
        Assert.assertNull(client.otherRunner);
    }

    private static class TestDesignerClient {
        @CitrusResource
        private TestDesigner designer;

        protected String otherField = "foo";
        protected TestDesigner otherDesigner = null;

        @CitrusResource
        private String otherAnnotatedResource = "foo";

        /**
         * Gets the value of the designer property.
         *
         * @return the designer
         */
        public TestDesigner getDesigner() {
            return designer;
        }
    }

    private static class TestRunnerClient {
        @CitrusResource
        private TestRunner runner;

        protected String otherField = "foo";
        protected TestRunner otherRunner = null;

        @CitrusResource
        private String otherAnnotatedResource = "foo";

        /**
         * Gets the value of the runner property.
         *
         * @return the runner
         */
        public TestRunner getRunner() {
            return runner;
        }
    }
}