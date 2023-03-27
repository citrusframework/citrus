/*
 * Copyright 2006-2011 the original author or authors.
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

package org.citrusframework.citrus.message;

import org.citrusframework.citrus.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class MessageTypeTest extends UnitTestSupport {

    @Test
    public void testKnowsMessageType() {
        Assert.assertEquals(MessageType.knows("xml"), true);
        Assert.assertEquals(MessageType.knows("XML"), true);

        Assert.assertEquals(MessageType.knows("PLAINTEXT"), true);
        Assert.assertEquals(MessageType.knows("plaintext"), true);
        Assert.assertEquals(MessageType.knows("json"), true);
        Assert.assertEquals(MessageType.knows("csv"), true);

        Assert.assertEquals(MessageType.knows("msexcel"), false);
        Assert.assertEquals(MessageType.knows(""), false);
    }
}
