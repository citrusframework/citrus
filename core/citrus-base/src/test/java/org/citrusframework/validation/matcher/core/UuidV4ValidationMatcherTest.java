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

package org.citrusframework.validation.matcher.core;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

public class UuidV4ValidationMatcherTest extends UnitTestSupport {

    private UuidV4ValidationMatcher matcher = new UuidV4ValidationMatcher();

    @Test
    public void testValidateSuccess() {
        try {
            matcher.validate("field", "34d68c48-1455-43ac-a0d6-b7b894c7a7d2", null, context);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testInvalidUuid() {
        assertThatThrownBy(() ->
                matcher.validate("field", "34d68c48-1455-43ac-a0d6-b7b894c7a7d2uuidtoolong", null, context))
                .isInstanceOf(ValidationException.class)
                .hasMessage("UuidV4ValidationMatcher failed for field 'field'. Received value '34d68c48-1455-43ac-a0d6-b7b894c7a7d2uuidtoolong' is not a uuid v4.");
    }

    @Test
    public void testWrongUuidVersion() {
        assertThatThrownBy(() ->
                matcher.validate("field", "34be571d-7180-3bcf-bbda-20cffbfae9ed", null, context))
                .isInstanceOf(ValidationException.class)
                .hasMessage("UuidV4ValidationMatcher failed for field 'field'. Received value '34be571d-7180-3bcf-bbda-20cffbfae9ed' is not a uuid v4.");
    }
}
