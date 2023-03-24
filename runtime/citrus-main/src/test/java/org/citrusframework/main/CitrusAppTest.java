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

package org.citrusframework.main;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class CitrusAppTest {

    @Test
    public void testHelpOption() {
        CitrusApp.main(new String[] { "-h" });
        CitrusApp.main(new String[] { "--help" });
    }

    @Test
    public void testDurationOption() {
        CitrusApp.main(new String[] { "-s", "true", "-d", "200" });
        CitrusApp.main(new String[] { "-s", "true", "--duration", "200" });

        try {
            CitrusApp.main(new String[] { "-s", "true", "-d" });
            Assert.fail("Missing exception due to invalid option parameter usage");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Missing parameter value for -d/--duration option");
        }
    }

    @Test
    public void testConfigClassOption() {
        CitrusApp.main(new String[] { "-s", "true", "-d", "200", "-c", CustomConfig.class.getName() });
        CitrusApp.main(new String[] { "-s", "true", "-d", "200", "--config", CustomConfig.class.getName() });

        try {
            CitrusApp.main(new String[] { "-s", "true", "-d", "200", "--config" });
            Assert.fail("Missing exception due to invalid option parameter usage");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Missing parameter value for -c/--config option");
        }

        try {
            CitrusApp.main(new String[] { "-s", "true", "-c", "unknown.Class" });
            Assert.fail("Missing exception due to invalid option parameter usage");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getCause().getClass(), ClassNotFoundException.class);
        }
    }
}
