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

import com.consol.citrus.admin.jackson.JsonHelper;
import com.consol.citrus.admin.service.*;
import com.consol.citrus.model.config.core.*;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Controller
@RequestMapping("/data-dictionary")
public class DataDictionaryController {

    public static final String MODEL_TYPE = "modelType";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private DataDictionaryService dataDictionaryService;

    @Autowired
    private JsonHelper jsonHelper;

    @RequestMapping(method = {RequestMethod.GET})
    @ResponseBody
    public List<DataDictionaryType> listDataDictionaries() {
        return dataDictionaryService.listDataDictionaries();
    }

    @RequestMapping(method = {RequestMethod.POST})
    @ResponseBody
    public void createDataDictionary(@RequestBody JSONObject dictionary) {
        springBeanService.addBeanDefinition(projectService.getProjectContextConfigFile(), jsonHelper.readModel(enrichModelType(dictionary)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public DataDictionaryType getDataDictionary(@PathVariable("id") String id) {
        return dataDictionaryService.getDataDictionary(id);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.POST})
    @ResponseBody
    public void updateDataDictionary(@PathVariable("id") String id, @RequestBody JSONObject dictionary) {
        springBeanService.updateBeanDefinition(projectService.getProjectContextConfigFile(), id, jsonHelper.readModel(enrichModelType(dictionary)));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteDataDictionary(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(projectService.getProjectContextConfigFile(), id);
    }

    /**
     * Maps type information to proper model type and add this information to Json object.
     * @param dictionary
     * @return
     */
    private JSONObject enrichModelType(JSONObject dictionary) {
        String type = dictionary.get("type").toString();
        if (type.equals("xpath-data-dictionary")) {
            dictionary.put(MODEL_TYPE, XpathDataDictionaryDefinition.class.getName());
        } else if (type.equals("xml-data-dictionary")) {
            dictionary.put(MODEL_TYPE, XmlDataDictionaryDefinition.class.getName());
        } else if (type.equals("json-data-dictionary")) {
            dictionary.put(MODEL_TYPE, JsonDataDictionaryDefinition.class.getName());
        } else {
            dictionary.put(MODEL_TYPE, DataDictionaryType.class.getName());
        }
        return dictionary;
    }
}
