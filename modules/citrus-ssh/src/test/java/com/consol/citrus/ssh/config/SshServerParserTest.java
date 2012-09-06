/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ssh.config;

import static org.easymock.EasyMock.*;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

/**
 * @author roland
 * @since 05.09.12
 */
public class SshServerParserTest {

    @Test
    public void simple() {
        SshServerParser parser = new SshServerParser();

        Element element = createMock(Element.class);
        for (int i =0; i < SshServerParser.ATTRIBUTE_PROPERTY_MAPPING.length; i+=2) {
            expect(element.getAttribute(SshServerParser.ATTRIBUTE_PROPERTY_MAPPING[i])).andReturn("bla");
        }
        expect(element.getAttribute("message-handler-ref")).andReturn(null);
        replay(element);
        parser.parseInternal(element, null);
    }
}
