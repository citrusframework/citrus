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

package org.citrusframework.docs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import javax.xml.transform.TransformerException;

import org.citrusframework.generate.UnitFramework;
import org.citrusframework.generate.xml.XmlTestGenerator;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import static org.testng.Assert.assertEquals;

public class AbstractTestDocsGeneratorTest {

    private final AbstractTestDocsGenerator abstractTestDocsGenerator = new AbstractTestDocsGenerator("", "") {
        @Override
        public void doBody(final OutputStream buffered) throws TransformerException, IOException, SAXException {

        }

        @Override
        public void doHeader(final OutputStream buffered) throws TransformerException, IOException, SAXException {

        }

        @Override
        protected Properties getTestDocProperties() {
            return null;
        }
    };

    @Test
    public void testGetTestFiles() throws IOException {

        //GIVEN
        new XmlTestGenerator<>()
                .withAuthor("Christoph")
                .withDescription("This is a sample test")
                .withName("SampleIT")
                .usePackage("org.citrusframework.sample")
                .withFramework(UnitFramework.TESTNG)
                .create();

        //WHEN
        final List<File> testFiles = abstractTestDocsGenerator.getTestFiles();

        //THEN
        assertEquals(testFiles.size(), 1);
        assertEquals(testFiles.get(0).getName(), "SampleIT.xml");
    }
}
