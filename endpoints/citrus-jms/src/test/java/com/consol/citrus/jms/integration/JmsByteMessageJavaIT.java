/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.jms.integration;

import java.io.IOException;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.consol.citrus.util.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.validation.interceptor.BinaryMessageProcessor.Builder.toBinary;

/**
 * @author Christoph Deppisch
 */
@Test
public class JmsByteMessageJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void jmsByteMessage() throws IOException {
        when(send("jms:queue:jms.binary.queue")
                .message(new DefaultMessage(
                        FileCopyUtils.copyToByteArray(
                                FileUtils.getFileResource("com/consol/citrus/jms/integration/button.png")
                                 .getInputStream())))
                .process(toBinary()));

        then(receive("jms:queue:jms.binary.queue")
                .message()
                .type(MessageType.BINARY_BASE64)
                .body("citrus:readFile('classpath:com/consol/citrus/jms/integration/button.png', true)"));
    }
}
