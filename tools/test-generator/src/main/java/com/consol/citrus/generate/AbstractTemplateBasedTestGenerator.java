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

package com.consol.citrus.generate;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.PropertyUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Properties;

/**
 * CLI creating a new test case from a template.
 * 
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractTemplateBasedTestGenerator<T extends TestGenerator> extends AbstractTestGenerator<T> {

    /**
     * Create the test case.
     */
    public void create() {
        FileUtils.writeToFile(createContent(getTemplateProperties()),
                new File(getSrcDirectory() + File.separator + getTargetPackage().replace('.', File.separatorChar) + File.separator + getName() + getFileExtension()));
    }
    
    /**
     * Prepares the test case properties for dynamic property replacement in
     * test case templates.
     *
     * @return the prepared property set.
     */
    protected Properties getTemplateProperties() {
        Properties properties = new Properties();
        properties.put("test.name", getName());
        properties.put("test.author", getAuthor());
        properties.put("test.description", getDescription());

        properties.put("test.update.datetime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(GregorianCalendar.getInstance().getTime()));
        properties.put("test.creation.date", new SimpleDateFormat("yyyy-MM-dd").format(GregorianCalendar.getInstance().getTime()));

        properties.put("test.method.name", getName().substring(0,1).toLowerCase() + getName().substring(1));
        properties.put("test.package", getTargetPackage());

        properties.put("test.src.directory", getSrcDirectory());

        return properties;
    }

    /**
     * Read the given template file and replace all test case properties.
     * 
     * @param properties the dynamic test case properties.
     * @return the final rest file content.
     */
    private String createContent(Properties properties) {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new PathMatchingResourcePatternResolver().getResource(getTemplateFilePath()).getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(PropertyUtils.replacePropertiesInString(line, properties));
                contentBuilder.append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new CitrusRuntimeException("Failed to create test case, unable to find test case template", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create test case, error while accessing test case template file", e);
        }
        
        return contentBuilder.toString();
    }

    /**
     * Subclasses must provide proper template file path.
     * @return
     */
    protected abstract String getTemplateFilePath();
}
