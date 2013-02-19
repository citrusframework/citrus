/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Project related activities like project home selection and 
 * project specific settings.
 * 
 * @author Christoph Deppisch
 */
@Component
public class ProjectService {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ProjectService.class);
    
    /**
     * Gets list of subfolder names and paths for given root directory.
     * @param directory
     * @return
     */
    public String[] getFolders(String directory) {
        if (new File(directory).exists()) {
            String[] files = new File(directory).list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.charAt(0) != '.' && new File(dir, name).isDirectory();
                }
            });
            Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
            
            return files;
        } else {
            throw new IllegalArgumentException("Could not open directory because it does not exist: " + directory);
        }
    }

    /**
     * Check if home directory is valid Citrus project home. 
     * @param directory
     */
    public boolean isProjectHome(String directory) {
        File homeDir = new File(directory);
        
        try {
            Assert.isTrue(homeDir.exists());
            Assert.isTrue(new File(homeDir, "src/citrus").exists());
            Assert.isTrue(new File(homeDir, "src/citrus/resources").exists());
            Assert.isTrue(new File(homeDir, "src/citrus/resources/citrus-context.xml").exists());
            Assert.isTrue(new File(homeDir, "src/citrus/tests").exists());
            Assert.isTrue(new File(homeDir, "src/citrus/java").exists());
        } catch (IllegalArgumentException e) {
            log.warn("Project home validation failed", e);
            return false;
        }
        
        return true;
    }
}
