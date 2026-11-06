/*
 * Copyright the original author or authors.
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
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

        CitrusRuntimeException e = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> CitrusApp.main(new String[] { "-s", "true", "-d" }));
        Assertions.assertEquals("Missing parameter value for -d/--duration option", e.getMessage());
    }

    @Test
    public void testConfigClassOption() {
        CitrusApp.main(new String[] { "-s", "true", "-d", "200", "-c", CustomConfig.class.getName() });
        CitrusApp.main(new String[] { "-s", "true", "-d", "200", "--config", CustomConfig.class.getName() });

        CitrusRuntimeException e = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> CitrusApp.main(new String[] { "-s", "true", "-d", "200", "--config" }));
        Assertions.assertEquals("Missing parameter value for -c/--config option", e.getMessage());

        e = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> CitrusApp.main(new String[] { "-s", "true", "-c", "unknown.Class" }));
        Assertions.assertEquals(ClassNotFoundException.class, e.getCause().getClass());
    }
}
