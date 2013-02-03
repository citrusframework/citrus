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

package com.consol.citrus.admin.controller;

import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/project")
public class ProjectController {

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String searchProjectHome(@RequestParam("dir") String directory) throws UnsupportedEncodingException {
        StringBuilder structure = new StringBuilder();
        
        if (directory.charAt(directory.length() - 1) == '\\') {
            directory = directory.substring(0, directory.length() - 1) + "/";
        } else if (directory.charAt(directory.length() - 1) != '/') {
            directory += "/";
        }
        
        directory = URLDecoder.decode(directory, "UTF-8"); 
        
        if (new File(directory).exists()) {
            String[] files = new File(directory).list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.charAt(0) != '.' && new File(dir, name).isDirectory();
                }
            });
            Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
            structure.append("<ul class=\"jqueryFileTree\" style=\"display: none;\">");
            // All dirs
            for (String file : files) {
                if (new File(directory, file).isDirectory()) {
                    structure.append("<li class=\"directory collapsed\"><a href=\"#\" rel=\"" + directory + file + "/\">"
                        + file + "</a></li>");
                }
            }
            // All files
            for (String file : files) {
                if (!new File(directory, file).isDirectory()) {
                    int dotIndex = file.lastIndexOf('.');
                    String ext = dotIndex > 0 ? file.substring(dotIndex + 1) : "";
                    structure.append("<li class=\"file ext_" + ext + "\"><a href=\"#\" rel=\"" + directory + file + "\">"
                        + file + "</a></li>");
                    }
            }
            structure.append("</ul>");
        }
        
        return structure.toString();
    }
}
