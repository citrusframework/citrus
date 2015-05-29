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

import com.consol.citrus.admin.service.*;
import com.consol.citrus.model.config.core.ValidationMatcherLibraryDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Controller
@RequestMapping("/validation-matcher")
public class ValidationMatcherController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private ValidationMatcherService validationMatcherService;

    @RequestMapping(method = {RequestMethod.GET})
    @ResponseBody
    public List<ValidationMatcherLibraryDefinition> listFunctionLibraries() {
        return validationMatcherService.listValidationMatcherLibraries();
    }

    @RequestMapping(method = {RequestMethod.POST})
    @ResponseBody
    public void createValidationMatcherLibrary(@RequestBody ValidationMatcherLibraryDefinition library) {
        springBeanService.addBeanDefinition(projectService.getProjectContextConfigFile(), library);
    }

    @RequestMapping(value = "/default", method = {RequestMethod.GET})
    @ResponseBody
    public ValidationMatcherLibraryDefinition getDefaultValidationMatcherLibrary() {
        return validationMatcherService.getDefaultValidationMatcherLibrary();
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public ValidationMatcherLibraryDefinition getValidationMatcherLibrary(@PathVariable("id") String id) {
        return validationMatcherService.getValidationMatcherLibrary(id);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.POST})
    @ResponseBody
    public void updateValidationMatcherLibrary(@PathVariable("id") String id, @RequestBody ValidationMatcherLibraryDefinition library) {
        springBeanService.updateBeanDefinition(projectService.getProjectContextConfigFile(), id, library);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteValidationMatcherLibrary(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(projectService.getProjectContextConfigFile(), id);
    }
}
