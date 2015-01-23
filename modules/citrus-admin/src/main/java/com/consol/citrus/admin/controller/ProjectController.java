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

import com.consol.citrus.admin.model.Project;
import com.consol.citrus.admin.model.TestReport;
import com.consol.citrus.admin.report.TestReportService;
import com.consol.citrus.admin.service.ConfigurationService;
import com.consol.citrus.admin.service.ProjectService;
import com.consol.citrus.admin.util.FileHelper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/project")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;

    @Autowired
    private TestReportService testReportService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private FileHelper fileHelper;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView browseProjectHome(@RequestParam("dir") String dir) {
        String directory = FilenameUtils.separatorsToSystem(fileHelper.decodeDirectoryUrl(dir, configurationService.getRootDirectory()));
        String[] folders = fileHelper.getFolders(new File(directory));

        ModelAndView view = new ModelAndView("FileTree");
        view.addObject("folders", folders);
        view.addObject("baseDir", FilenameUtils.separatorsToUnix(directory));

        return view;
    }

    @RequestMapping(value = "/active", method = RequestMethod.GET)
    @ResponseBody
    public Project getActiveProject() {
        return projectService.getActiveProject();
    }
    
    @RequestMapping(value = "/open", method = RequestMethod.POST)
    public String openProject(@RequestParam("projecthome") String projecthome) {
        projectService.load(projecthome);
        return "redirect:/";
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    public String openProject() {
        if (projectService.getActiveProject() != null) {
            return "redirect:/";
        } else {
            return "redirect:/setup";
        }
    }

    @RequestMapping(value = "/testreport", method = RequestMethod.GET)
    @ResponseBody
    public TestReport getTestReport() {
        return testReportService.loadReport(projectService.getActiveProject());
    }
}
