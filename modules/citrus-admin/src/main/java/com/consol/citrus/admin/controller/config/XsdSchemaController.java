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

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.service.ProjectService;
import com.consol.citrus.admin.util.CitrusConfigHelper;
import com.consol.citrus.model.config.core.XsdSchema;
import com.consol.citrus.model.spring.beans.Beans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.List;

/**
 * Controller manages all XSD Schema related requests
 *
 * @author Martin.Maher@consol.de
 * @since 2013.02.02
 */
@Controller
@RequestMapping("/config/xsd-schema")
public class XsdSchemaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(XsdSchemaController.class);


    @Autowired
    private ProjectService projectService;

    @Autowired
    private CitrusConfigHelper citrusConfigHelper;

    /**
     * Returns a list of all XSD schemas configured in project
     *
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET})
    @ResponseBody
    public List<XsdSchema> list() {
        return citrusConfigHelper.getConfigElementsByType(getCitrusConfig(), XsdSchema.class);
    }

    @RequestMapping(method = {RequestMethod.POST})
    @ResponseBody
    public void createXsdSchema(XsdSchema xsdSchema) {
        final Beans citrusConfig = getCitrusConfig();
        citrusConfig.getImportsAndAliasAndBeen().add(xsdSchema);
        citrusConfigHelper.persistCitrusConfig(projectService.getProjectConfigFile(), citrusConfig);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public XsdSchema getXsdSchema(@PathVariable("id") String id) {
        List<XsdSchema> xsdSchemas = citrusConfigHelper.getConfigElementsByType(getCitrusConfig(), XsdSchema.class);
        return findById(xsdSchemas, id);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateXsdSchema(@PathVariable("id") String id, XsdSchema xsdSchema) {
        final Beans citrusConfig = getCitrusConfig();
        List<XsdSchema> xsdSchemas = citrusConfigHelper.getConfigElementsByType(citrusConfig, XsdSchema.class);
        if (updateById(xsdSchemas, id, xsdSchema)) {
            citrusConfigHelper.persistCitrusConfig(projectService.getProjectConfigFile(), citrusConfig);
        } else {
            throw new CitrusAdminRuntimeException(String.format("No XSD Schema found matching id '%s'", id));
        }
    }

    @RequestMapping(value = "/{id}", params = {"deleteFile"}, method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteXsdSchema(@PathVariable("id") String id, @RequestParam("deleteFile") Boolean deleteFile) {
        final Beans citrusConfig = getCitrusConfig();
        List<XsdSchema> xsdSchemas = citrusConfigHelper.getConfigElementsByType(citrusConfig, XsdSchema.class);
        if (deleteById(xsdSchemas, id)) {
            citrusConfigHelper.persistCitrusConfig(projectService.getProjectConfigFile(), citrusConfig);
        } else {
            throw new CitrusAdminRuntimeException(String.format("No XSD Schema found matching id '%s'", id));
        }

    }

    private Beans getCitrusConfig() {
        File projectConfigFile = projectService.getProjectConfigFile();
        return citrusConfigHelper.loadCitrusConfig(projectConfigFile);
    }

    private XsdSchema findById(List<XsdSchema> xsdSchemas, String id) {
        if (xsdSchemas != null) {
            for (XsdSchema xsdSchema : xsdSchemas) {
                if (id.equals(xsdSchema.getId())) {
                    LOGGER.info("Found XSD Schema matching id " + id);
                    return xsdSchema;
                }
            }
        }
        LOGGER.info("NO XSD Schema found matching id " + id);
        return null;
    }

    private boolean deleteById(List<XsdSchema> xsdSchemas, String id) {
        for (int i = 0; i < xsdSchemas.size(); i++) {
            XsdSchema xsdSchema = xsdSchemas.get(i);
            if (id.equals(xsdSchema.getId())) {
                xsdSchemas.remove(i);
                LOGGER.info("XSD Schema deleted matching id " + id);
                return true;
            }
        }
        LOGGER.info("NO XSD Schema found matching id " + id);
        return false;
    }

    private boolean updateById(List<XsdSchema> xsdSchemas, String id, XsdSchema updatedSchema) {
        XsdSchema schemaToUpdate = findById(xsdSchemas, id);
        if (schemaToUpdate != null) {
            schemaToUpdate.setLocation(updatedSchema.getLocation());
            schemaToUpdate.setId(updatedSchema.getId());
            LOGGER.info("XSD Schema updated matching id " + id);
            return true;
        } else {
            LOGGER.info("NO XSD Schema found matching id " + id);
            return false;
        }
    }
}
