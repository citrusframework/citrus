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
import com.consol.citrus.admin.model.MessageReceiverItem;
import com.consol.citrus.admin.model.MessageSenderItem;
import com.consol.citrus.admin.service.*;
import com.consol.citrus.model.config.core.*;
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
@RequestMapping("/config")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private SchemaRepositoryService schemaRepositoryService;

    @Autowired
    private MessageSenderService messageSenderService;

    @Autowired
    private MessageReceiverService messageReceiverService;

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

    @RequestMapping(value = "/xsd-schema-repository", method = {RequestMethod.GET})
    @ResponseBody
    public List<SchemaRepository> listSchemaRepositories() {
        return schemaRepositoryService.listSchemaRepositories(projectService.getProjectContextConfigFile());
    }

    @RequestMapping(value = "/xsd-schema-repository/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public SchemaRepository getSchemaRepository(@PathVariable("id") String id) {
        return schemaRepositoryService.getSchemaRepository(projectService.getProjectContextConfigFile(), id);
    }

    @RequestMapping(value="/xsd-schema-repository", method = {RequestMethod.POST})
    @ResponseBody
    public void createSchemaRepository(@RequestBody SchemaRepository xsdSchemaRepository) {
        springBeanService.addBeanDefinition(projectService.getProjectContextConfigFile(), xsdSchemaRepository);
    }

    @RequestMapping(value = "/xsd-schema-repository/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateSchemaRepository(@PathVariable("id") String id, @RequestBody SchemaRepository xsdSchemaRepository) {
        springBeanService.updateBeanDefinition(projectService.getProjectContextConfigFile(), id, xsdSchemaRepository);
    }

    @RequestMapping(value = "/xsd-schema-repository/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteSchemaRepository(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(projectService.getProjectContextConfigFile(), id);
    }

    @RequestMapping(value = "/xsd-schema", method = {RequestMethod.GET})
    @ResponseBody
    public List<Schema> listXsdSchemas() {
        return schemaRepositoryService.listSchemas(projectService.getProjectContextConfigFile());
    }

    @RequestMapping(value = "/xsd-schema/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public Schema getXsdSchema(@PathVariable("id") String id) {
        return schemaRepositoryService.getSchema(projectService.getProjectContextConfigFile(), id);
    }

    @RequestMapping(value="/xsd-schema", method = {RequestMethod.POST})
    @ResponseBody
    public void createXsdSchema(@RequestBody Schema xsdSchema) {
        springBeanService.addBeanDefinition(projectService.getProjectContextConfigFile(), xsdSchema);
    }

    @RequestMapping(value = "/xsd-schema/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateXsdSchema(@PathVariable("id") String id, @RequestBody Schema xsdSchema) {
        springBeanService.updateBeanDefinition(projectService.getProjectContextConfigFile(), id, xsdSchema);
    }

    @RequestMapping(value = "/xsd-schema/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteXsdSchema(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(projectService.getProjectContextConfigFile(), id);
    }

    @RequestMapping(value = "/msg-sender", method = {RequestMethod.GET})
    @ResponseBody
    public List<MessageSenderItem> listMsgSender() {
        return messageSenderService.listMessageSender(projectService.getProjectContextConfigFile());
    }

    @RequestMapping(value = "/msg-sender/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public MessageSenderItem getMsgSender(@PathVariable("id") String id) {
        return messageSenderService.getMessageSender(projectService.getProjectContextConfigFile(), id);
    }

    @RequestMapping(value="/msg-sender", method = {RequestMethod.POST})
    @ResponseBody
    public void createMsgSender(@RequestBody MessageSenderItem msgSender) {
        springBeanService.addBeanDefinition(projectService.getProjectContextConfigFile(), msgSender);
    }

    @RequestMapping(value = "/msg-sender/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateMsgSender(@PathVariable("id") String id, @RequestBody MessageSenderItem msgSender) {
        springBeanService.updateBeanDefinition(projectService.getProjectContextConfigFile(), id, msgSender);
    }

    @RequestMapping(value = "/msg-sender/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteMsgSender(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(projectService.getProjectContextConfigFile(), id);
    }

    @RequestMapping(value = "/msg-receiver", method = {RequestMethod.GET})
    @ResponseBody
    public List<MessageReceiverItem> listMsgReceiver() {
        return messageReceiverService.listMessageReceiver(projectService.getProjectContextConfigFile());
    }

    @RequestMapping(value = "/msg-receiver/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public MessageReceiverItem getMsgReceiver(@PathVariable("id") String id) {
        return messageReceiverService.getMessageReceiver(projectService.getProjectContextConfigFile(), id);
    }

    @RequestMapping(value="/msg-receiver", method = {RequestMethod.POST})
    @ResponseBody
    public void createMsgReceiver(@RequestBody MessageReceiverItem msgReceiver) {
        springBeanService.addBeanDefinition(projectService.getProjectContextConfigFile(), msgReceiver);
    }

    @RequestMapping(value = "/msg-receiver/{id}", method = {RequestMethod.PUT})
    @ResponseBody
    public void updateMsgReceiver(@PathVariable("id") String id, @RequestBody MessageReceiverItem msgReceiver) {
        springBeanService.updateBeanDefinition(projectService.getProjectContextConfigFile(), id, msgReceiver);
    }

    @RequestMapping(value = "/msg-receiver/{id}", method = {RequestMethod.DELETE})
    @ResponseBody
    public void deleteMsgReceiver(@PathVariable("id") String id) {
        springBeanService.removeBeanDefinition(projectService.getProjectContextConfigFile(), id);
    }
}
