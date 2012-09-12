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
public class SshParserTest {

    @Test
    public void serverParser() {
        testParser(new SshServerParser());
    }

    @Test
     public void clientParser() {
        testParser(new SshClientParser());
    }

    @Test
    public void replyHandlerParser() {
        testParser(new SshReplyHandlerParser());
    }

    private void testParser(AbstractSshParser pParser) {
        Element element = prepareElementMock(pParser);
        pParser.parseInternal(element, null);
    }


    private Element prepareElementMock(AbstractSshParser pParser) {
        Element element = createMock(Element.class);
        for (int i =0; i < pParser.getAttributePropertyMapping().length; i+=2) {
            expect(element.getAttribute(pParser.getAttributePropertyMapping()[i])).andReturn("bla");
        }

        for (int i = 0; i < pParser.getAttributePropertyReferenceMapping().length; i+= 2) {
            expect(element.getAttribute(pParser.getAttributePropertyReferenceMapping()[i])).andReturn("ref");
        }
        replay(element);
        return element;
    }


}
