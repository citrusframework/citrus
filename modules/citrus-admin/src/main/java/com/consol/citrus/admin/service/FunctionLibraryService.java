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

package com.consol.citrus.admin.service;

import com.consol.citrus.admin.converter.spring.FunctionLibrarySpringBeanConverter;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.spring.model.SpringBean;
import com.consol.citrus.functions.*;
import com.consol.citrus.model.config.core.FunctionLibraryDefinition;
import com.consol.citrus.model.config.core.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Component
public class FunctionLibraryService {

    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private ProjectService projectService;

    private FunctionLibrarySpringBeanConverter functionLibrarySpringBeanConverter = new FunctionLibrarySpringBeanConverter();

    /**
     * Gets the default function library from Citrus Spring bean configuration.
     * @return
     */
    public FunctionLibraryDefinition getDefaultFunctionLibrary() {
        FunctionLibraryDefinition library = new ObjectFactory().createFunctionLibraryDefinition();

        FunctionConfig config = new FunctionConfig();
        com.consol.citrus.functions.FunctionLibrary defaultFunctionLibrary = config.getFunctionaLibrary();
        library.setId(defaultFunctionLibrary.getName());
        library.setPrefix(defaultFunctionLibrary.getPrefix());

        for (Map.Entry<String, Function> functionEntry : defaultFunctionLibrary.getMembers().entrySet()) {
            FunctionLibraryDefinition.Function function = new FunctionLibraryDefinition.Function();
            function.setName(functionEntry.getKey());
            function.setClazz(functionEntry.getValue().getClass().getName());
            library.getFunctions().add(function);
        }

        return library;
    }

    /**
     * Gets the function library object from bean in application context. Bean is identified by its id.
     * @param id
     * @return
     */
    public FunctionLibraryDefinition getFunctionLibrary(String id) {
        FunctionLibraryDefinition library = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, FunctionLibraryDefinition.class);

        if (library == null) {
            SpringBean springBean = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, SpringBean.class);
            if (springBean != null) {
                library = functionLibrarySpringBeanConverter.convert(springBean);
            }
        }

        if (library == null) {
            throw new CitrusAdminRuntimeException(String.format("Unable to find function library definition for id '%s'", id));
        }

        return library;
    }

    /**
     * List all function libraries in application context.
     * @return
     */
    public List<FunctionLibraryDefinition> listFunctionLibraries() {
        List<FunctionLibraryDefinition> libraries = new ArrayList<FunctionLibraryDefinition>();

        libraries.addAll(springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), FunctionLibraryDefinition.class));

        List<SpringBean> springBeans = springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), SpringBean.class, Collections.singletonMap("class", com.consol.citrus.functions.FunctionLibrary.class.getName()));
        for (SpringBean springBean : springBeans) {
            libraries.add(functionLibrarySpringBeanConverter.convert(springBean));
        }

        return libraries;
    }

}
