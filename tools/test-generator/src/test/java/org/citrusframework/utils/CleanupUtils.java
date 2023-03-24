/*
 *    Copyright 2019 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.citrusframework.utils;

import org.citrusframework.util.FileUtils;

import java.io.File;
import java.util.Set;

public class CleanupUtils {

    public void deleteFiles(final String startDir, final Set<String> fileNamePatterns){
        //noinspection ResultOfMethodCallIgnored
        FileUtils.findFiles(startDir, fileNamePatterns).
                forEach(File::delete);
    }

    public void deleteFile(File fileToDelete){
        if(fileToDelete.exists()){
            //noinspection ResultOfMethodCallIgnored
            fileToDelete.delete();
        }
    }
}
