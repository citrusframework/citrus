/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.citrus.main.scan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.citrusframework.citrus.TestClass;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JarFileTestScanner extends AbstractTestScanner {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(JarFileTestScanner.class);

    /** Jar file resource to search in */
    private final File artifact;

    public JarFileTestScanner(File artifact, String... includes) {
        super(includes);
        this.artifact = artifact;
    }

    @Override
    public List<TestClass> findTestsInPackage(String packageToScan) {
        List<TestClass> testClasses = new ArrayList<>();
        if (artifact != null && artifact.isFile()) {
            try (JarFile jar = new JarFile(artifact)) {
                for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
                    JarEntry entry = entries.nextElement();
                    String className = StringUtils.stripFilenameExtension(entry.getName()).replace( "/", "." );
                    if (new AntPathMatcher().matchStart(packageToScan.replace( ".", "/" ), entry.getName()) && isIncluded(className)) {
                        LOG.info("Found test class candidate in test jar file: " +  entry.getName());
                        testClasses.add(new TestClass(className));
                    }
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to access jar file artifact", e);
            }
        }

        return testClasses;
    }
}
