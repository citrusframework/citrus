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

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.xml.ParserContext;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SchemaRepositoryParserTest {

    private XmlSchemaRepositoryParser xmlSchemaRepositoryParserMock = mock(XmlSchemaRepositoryParser.class);
    private JsonSchemaRepositoryParser jsonSchemaRepositoryParserMock = mock(JsonSchemaRepositoryParser.class);
    private SchemaRepositoryParser schemaRepositoryParser =
            new SchemaRepositoryParser(xmlSchemaRepositoryParserMock, jsonSchemaRepositoryParserMock);

    private Element elementMock = mock(Element.class);
    private ParserContext parserContextMock = mock(ParserContext.class);

    @Test
    public void xmlSchemaRepositoryIsDelegatedCorrectly(){

        //GIVEN
        when(elementMock.getAttribute("type")).thenReturn("xml");

        //WHEN
        schemaRepositoryParser.parse(elementMock,parserContextMock);

        //THEN
        verify(xmlSchemaRepositoryParserMock).parse(elementMock, parserContextMock);

    }

    @Test
    public void jsonSchemaRepositoryIsDelegatedCorrectly(){

        //GIVEN
        when(elementMock.getAttribute("type")).thenReturn("json");

        //WHEN
        schemaRepositoryParser.parse(elementMock,parserContextMock);

        //THEN
        verify(jsonSchemaRepositoryParserMock).parse(elementMock, parserContextMock);

    }
}
