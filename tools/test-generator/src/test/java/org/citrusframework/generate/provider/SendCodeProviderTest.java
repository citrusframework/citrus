/*
 * Copyright 2006-2019 the original author or authors.
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

package org.citrusframework.generate.provider;

import org.citrusframework.message.DefaultMessage;
import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SendCodeProviderTest {

    private final SendCodeProvider sendCodeProvider = new SendCodeProvider();

    private final DefaultMessage message = new DefaultMessage();

    @Test
    void getCode() {

        //GIVEN
        final String endpoint = "foo";
        final String expectedString = "runner.run(send().endpoint(\"foo\")\n  .message()\n);";

        //WHEN
        final CodeBlock code = sendCodeProvider.getCode(endpoint, message);

        //THEN
        assertEquals(expectedString, code.toString());
    }
}
