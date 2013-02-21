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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.admin.service.ConfigService;
import com.consol.citrus.admin.service.ProjectService;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/project")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ConfigService configService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String searchProjectHome(@RequestParam("dir") String dir) throws UnsupportedEncodingException {
        String directory = URLDecoder.decode(dir, "UTF-8"); // TODO use system default encoding?
        if (directory.equals("/")) {
            directory = configService.getRootDirectory();
        }
        
        if (directory.charAt(directory.length() - 1) == '\\') {
            directory = directory.substring(0, directory.length() - 1) + "/";
        } else if (directory.charAt(directory.length() - 1) != '/') {
            directory += "/";
        }
        
        String[] folders = projectService.getFolders(URLDecoder.decode(directory, "UTF-8"));
        
        StringBuilder structure = new StringBuilder();
        structure.append("<ul class=\"jqueryFileTree\" style=\"display: none;\">");
        for (String file : folders) {
            structure.append("<li class=\"directory collapsed\"><a href=\"#\" rel=\"" + directory + file + "/\">"
                + file + "</a></li>");
        }
        structure.append("</ul>");
        
        return structure.toString();
    }
    
    @RequestMapping(params = {"projecthome"}, method = RequestMethod.GET)
    public String setProjectHome(@RequestParam("projecthome") String projecthome) {
        if (!projectService.isProjectHome(projecthome)) {
            throw new IllegalArgumentException("Invalid project home - not a proper Citrus project");
        }
        
        configService.setProjectHome(projecthome);
        return "redirect:/";
    }
}
