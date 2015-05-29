/*
 * Copyright 2006-2014 the original author or authors.
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

import com.consol.citrus.admin.service.GlobalVariablesService;
import com.consol.citrus.admin.service.ProjectService;
import com.consol.citrus.model.config.core.GlobalVariablesDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Controller
@RequestMapping("/global-variables")
public class GlobalVariablesController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private GlobalVariablesService globalVariablesService;

    @RequestMapping(method = {RequestMethod.GET})
    @ResponseBody
    public GlobalVariablesDefinition getGlobalVariables() {
        return globalVariablesService.getGlobalVariables(projectService.getProjectContextConfigFile());
    }

    @RequestMapping(method = {RequestMethod.PUT})
    @ResponseBody
    public void updateGlobalVariables(@RequestBody GlobalVariablesDefinition variables) {
        globalVariablesService.updateGlobalVariables(projectService.getProjectContextConfigFile(), variables);
    }
}
