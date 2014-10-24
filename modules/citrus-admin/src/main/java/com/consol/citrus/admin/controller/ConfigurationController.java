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

import com.consol.citrus.admin.configuration.RunConfiguration;
import com.consol.citrus.admin.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Configuration controller handles project choosing requests and project configuration setup
 * form submits. In addition to that manages spring bean configuration with adding/updating/deleting beans
 * from application context.
 * 
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/configuration")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpringBeanService springBeanService;

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public List<String> search(@RequestBody String key) {
        return springBeanService.getBeanNames(projectService.getProjectContextConfigFile(), key);
    }

    @RequestMapping(value = "/run", method = RequestMethod.GET)
    @ResponseBody
    public List<RunConfiguration> getRunConfigurations() {
        return projectService.getActiveProject().getRunConfigurations();
    }

    @RequestMapping(value = "/root", method = RequestMethod.GET)
    @ResponseBody
    public String getRootDirectory() {
        return configService.getRootDirectory();
    }
}
