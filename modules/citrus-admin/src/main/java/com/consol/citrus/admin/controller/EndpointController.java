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

import com.consol.citrus.admin.model.EndpointData;
import com.consol.citrus.admin.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
@Controller
@RequestMapping("/endpoint")
public class EndpointController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private EndpointService endpointService;

    @RequestMapping(method = {RequestMethod.GET})
    @ResponseBody
    public List<EndpointData> listEndpoints() {
        return endpointService.listEndpoints();
    }

    @RequestMapping(method = {RequestMethod.POST})
    @ResponseBody
    public void createEndpoint(@RequestBody EndpointData endpointData) {
        springBeanService.addBeanDefinition(projectService.getProjectContextConfigFile(), endpointData);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public EndpointData getEndpointData(@PathVariable("id") String id) {
        return endpointService.getEndpoint(id);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateEndpoint(@PathVariable("id") String id, @RequestBody EndpointData endpointData) {
        springBeanService.updateBeanDefinition(projectService.getProjectContextConfigFile(), id, endpointData);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteEndpoint(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(projectService.getProjectContextConfigFile(), id);
    }
}
