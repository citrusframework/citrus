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

package org.citrusframework.validation.hamcrest;

import java.util.Map;

import org.citrusframework.validation.DefaultHeaderValidator;
import org.citrusframework.validation.HeaderValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HeaderValidatorTest {

    @Test
    public void testLookup() {
        Map<String, HeaderValidator> validators = HeaderValidator.lookup();
        Assertions.assertEquals(2L, validators.size());
        Assertions.assertNotNull(validators.get("defaultHeaderValidator"));
        Assertions.assertEquals(DefaultHeaderValidator.class, validators.get("defaultHeaderValidator").getClass());
        Assertions.assertNotNull(validators.get("hamcrestHeaderValidator"));
        Assertions.assertEquals(HamcrestHeaderValidator.class, validators.get("hamcrestHeaderValidator").getClass());
    }

    @Test
    public void testTestLookup() {
        Assertions.assertTrue(HeaderValidator.lookup("default").isPresent());
        Assertions.assertTrue(HeaderValidator.lookup("hamcrest").isPresent());
    }
}
