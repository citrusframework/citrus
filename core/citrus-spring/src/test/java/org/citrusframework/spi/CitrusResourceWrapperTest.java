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

package org.citrusframework.spi;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileUrlResource;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CitrusResourceWrapperTest {

    @Test
    public void readUriFromClasspathResource() throws IOException {
        String resource = "citrus-application.properties";
        ClassPathResource classPathResource = mock(ClassPathResource.class);
        doReturn(URI.create(resource)).when(classPathResource).getURI();

        CitrusResourceWrapper fixture = new CitrusResourceWrapper(classPathResource);

        assertTrue(fixture.getLocation().endsWith(resource), "Fixture location should resolve URI from classpath resource!");
        verify(classPathResource).getURI();
    }

    @Test
    public void readFileFromOtherResources() throws IOException {
        String resource = "citrus-application.properties";
        FileUrlResource classPathResource = mock(FileUrlResource.class);
        doReturn(File.createTempFile(getClass().getSimpleName(), resource)).when(classPathResource).getFile();

        CitrusResourceWrapper fixture = new CitrusResourceWrapper(classPathResource);

        assertTrue(fixture.getLocation().endsWith(resource), "Fixture location should resolve URI from classpath resource!");
        verify(classPathResource).getFile();
    }
}
