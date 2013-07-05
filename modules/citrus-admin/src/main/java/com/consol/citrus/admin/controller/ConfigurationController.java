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

import com.consol.citrus.admin.service.SpringBeanService;
import com.consol.citrus.model.config.core.SchemaRepository;
import com.consol.citrus.model.config.core.XsdSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.consol.citrus.admin.service.ConfigurationService;

import java.util.List;

/**
 * Configuration controller handles project choosing requests and project configuration setup
 * form submits. In addition to that manages spring bean configuration with adding/updating/deleting beans
 * from application context.
 * 
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/config")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configService;

    @Autowired
    private SpringBeanService springBeanService;

    @RequestMapping(value = "/projecthome", method = RequestMethod.GET)
    @ResponseBody
    public String getProjectHome() {
        return configService.getProjectHome();
    }
    
    @RequestMapping(value = "/root", method = RequestMethod.GET)
    @ResponseBody
    public String getRootDirectory() {
        return configService.getRootDirectory();
    }

    @RequestMapping(value = "/xsd-schema-repository", method = {RequestMethod.GET})
    @ResponseBody
    public List<SchemaRepository> listXsdSchemaRepositories() {
        return springBeanService.getBeanDefinitions(configService.getProjectConfigFile(), SchemaRepository.class);
    }

    @RequestMapping(value = "/xsd-schema-repository/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public SchemaRepository getXsdSchemaRepository(@PathVariable("id") String id) {
        return springBeanService.getBeanDefinition(configService.getProjectConfigFile(), id, SchemaRepository.class);
    }

    @RequestMapping(value="/xsd-schema-repository", method = {RequestMethod.POST})
    @ResponseBody
    public void createXsdSchemaRepository(@RequestBody SchemaRepository xsdSchemaRepository) {
        springBeanService.addBeanDefinition(configService.getProjectConfigFile(), xsdSchemaRepository);
    }

    @RequestMapping(value = "/xsd-schema-repository/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateXsdSchemaRepository(@PathVariable("id") String id, @RequestBody SchemaRepository xsdSchemaRepository) {
        springBeanService.updateBeanDefinition(configService.getProjectConfigFile(), id, xsdSchemaRepository);
    }

    @RequestMapping(value = "/xsd-schema-repository/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteXsdSchemaRepository(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(configService.getProjectConfigFile(), id);
    }

    @RequestMapping(value = "/xsd-schema", method = {RequestMethod.GET})
    @ResponseBody
    public List<XsdSchema> listXsdSchemas() {
        return springBeanService.getBeanDefinitions(configService.getProjectConfigFile(), XsdSchema.class);
    }

    @RequestMapping(value = "/xsd-schema/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public XsdSchema getXsdSchema(@PathVariable("id") String id) {
        return springBeanService.getBeanDefinition(configService.getProjectConfigFile(), id, XsdSchema.class);
    }

    @RequestMapping(value="/xsd-schema", method = {RequestMethod.POST})
    @ResponseBody
    public void createXsdSchema(@RequestBody XsdSchema xsdSchema) {
        springBeanService.addBeanDefinition(configService.getProjectConfigFile(), xsdSchema);
    }

    @RequestMapping(value = "/xsd-schema/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateXsdSchema(@PathVariable("id") String id, @RequestBody XsdSchema xsdSchema) {
        springBeanService.updateBeanDefinition(configService.getProjectConfigFile(), id, xsdSchema);
    }

    @RequestMapping(value = "/xsd-schema/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteXsdSchema(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(configService.getProjectConfigFile(), id);
    }
}
