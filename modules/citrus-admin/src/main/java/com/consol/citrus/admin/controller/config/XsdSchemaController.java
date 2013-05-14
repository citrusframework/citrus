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

package com.consol.citrus.admin.controller.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.admin.service.ProjectService;
import com.consol.citrus.admin.service.SpringConfigService;
import com.consol.citrus.model.config.core.XsdSchema;

/**
 * Controller manages all XSD Schema related requests
 *
 * @author Martin.Maher@consol.de
 * @since 2013.02.02
 */
@Controller
@RequestMapping("/config/xsd-schema")
public class XsdSchemaController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpringConfigService springBeanConfigService;

    /**
     * Returns a list of all XSD schemas configured in project
     *
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET})
    @ResponseBody
    public List<XsdSchema> list() {
        return springBeanConfigService.getBeanDefinitions(projectService.getProjectConfigFile(), XsdSchema.class);
    }
    
    @RequestMapping(value = "/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public XsdSchema getXsdSchema(@PathVariable("id") String id) {
        return springBeanConfigService.getBeanDefinition(projectService.getProjectConfigFile(), id, XsdSchema.class);
    }

    @RequestMapping(method = {RequestMethod.POST})
    @ResponseBody
    public void createXsdSchema(@RequestBody XsdSchema xsdSchema) {
        springBeanConfigService.addBeanDefinition(projectService.getProjectConfigFile(), xsdSchema);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateXsdSchema(@PathVariable("id") String id, @RequestBody XsdSchema xsdSchema) {
        springBeanConfigService.updateBeanDefinition(projectService.getProjectConfigFile(), id, xsdSchema);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteXsdSchema(@PathVariable("id") String id) {
        springBeanConfigService.removeBeanDefinition(projectService.getProjectConfigFile(), id);
    }
}
