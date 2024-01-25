/*
 *  Copyright 2024 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.ftp.message;

import org.citrusframework.message.MessageType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FtpMarshallerTest {

    private final FtpMarshaller marshaller = new FtpMarshaller();

    @Test
    public void shouldMarshalXml() {
        marshaller.setType(MessageType.XML.name());

        Assert.assertEquals(FtpMessage.put("some/local/path/file.txt").getPayload(String.class, marshaller),
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                        "<put-command xmlns=\"http://www.citrusframework.org/schema/ftp/message\">" +
                            "<signal>STOR</signal>" +
                            "<file path=\"some/local/path/file.txt\" type=\"ASCII\"/>" +
                            "<target path=\"file.txt\"/>" +
                        "</put-command>");
    }

    @Test
    public void shouldMarshalJson() {
        marshaller.setType(MessageType.JSON.name());

        Assert.assertEquals(FtpMessage.put("some/local/path/file.txt").getPayload(String.class, marshaller),
                "{" +
                            "\"signal\":\"STOR\"," +
                            "\"file\":{" +
                                "\"path\":\"some/local/path/file.txt\"," +
                                "\"type\":\"ASCII\"" +
                            "}," +
                            "\"target\":{" +
                                "\"path\":\"file.txt\"" +
                            "}" +
                        "}");
    }

}
