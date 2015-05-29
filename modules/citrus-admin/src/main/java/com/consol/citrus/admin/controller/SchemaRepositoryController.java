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
import com.consol.citrus.model.config.core.SchemaRepositoryDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Controller
@RequestMapping("/schema-repository")
public class SchemaRepositoryController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private SchemaRepositoryService schemaRepositoryService;

    @RequestMapping(method = {RequestMethod.GET})
    @ResponseBody
    public List<SchemaRepositoryDefinition> listSchemaRepositories() {
        return schemaRepositoryService.listSchemaRepositories(projectService.getProjectContextConfigFile());
    }

    @RequestMapping(method = {RequestMethod.POST})
    @ResponseBody
    public void createSchemaRepository(@RequestBody SchemaRepositoryDefinition xsdSchemaRepository) {
        springBeanService.addBeanDefinition(projectService.getProjectContextConfigFile(), xsdSchemaRepository);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public SchemaRepositoryDefinition getSchemaRepository(@PathVariable("id") String id) {
        return schemaRepositoryService.getSchemaRepository(projectService.getProjectContextConfigFile(), id);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateSchemaRepository(@PathVariable("id") String id, @RequestBody SchemaRepositoryDefinition xsdSchemaRepository) {
        List<SchemaRepositoryDefinition.Schemas.Ref> schemaRefs = new ArrayList<SchemaRepositoryDefinition.Schemas.Ref>();
        for (Object schema: xsdSchemaRepository.getSchemas().getRevesAndSchemas()) {
            if (schema instanceof String) {
                SchemaRepositoryDefinition.Schemas.Ref ref = new SchemaRepositoryDefinition.Schemas.Ref();
                ref.setSchema((String) schema);
                schemaRefs.add(ref);
            } else if (schema instanceof SchemaRepositoryDefinition.Schemas.Ref) {
                schemaRefs.add((SchemaRepositoryDefinition.Schemas.Ref) schema);
            }
        }

        xsdSchemaRepository.getSchemas().getRevesAndSchemas().clear();
        xsdSchemaRepository.getSchemas().getRevesAndSchemas().addAll(schemaRefs);

        springBeanService.updateBeanDefinition(projectService.getProjectContextConfigFile(), id, xsdSchemaRepository);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteSchemaRepository(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(projectService.getProjectContextConfigFile(), id);
    }
}
