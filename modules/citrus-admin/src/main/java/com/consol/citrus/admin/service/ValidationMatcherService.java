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

import com.consol.citrus.admin.converter.spring.ValidationMatcherSpringBeanConverter;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.spring.model.SpringBean;
import com.consol.citrus.model.config.core.ObjectFactory;
import com.consol.citrus.model.config.core.ValidationMatcherLibraryDefinition;
import com.consol.citrus.validation.matcher.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Component
public class ValidationMatcherService {

    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private ProjectService projectService;

    private ValidationMatcherSpringBeanConverter matcherLibrarySpringBeanConverter = new ValidationMatcherSpringBeanConverter();

    /**
     * Gets the default validation matcher library from Citrus Spring bean configuration.
     * @return
     */
    public ValidationMatcherLibraryDefinition getDefaultValidationMatcherLibrary() {
        ValidationMatcherLibraryDefinition library = new ObjectFactory().createValidationMatcherLibraryDefinition();

        ValidationMatcherConfig config = new ValidationMatcherConfig();
        com.consol.citrus.validation.matcher.ValidationMatcherLibrary defaultValidationMatcher = config.getValidationMatcherLibrary();
        library.setId(defaultValidationMatcher.getName());
        library.setPrefix(defaultValidationMatcher.getPrefix());

        for (Map.Entry<String, ValidationMatcher> matcherEntry : defaultValidationMatcher.getMembers().entrySet()) {
            ValidationMatcherLibraryDefinition.Matcher matcher = new ValidationMatcherLibraryDefinition.Matcher();
            matcher.setName(matcherEntry.getKey());
            matcher.setClazz(matcherEntry.getValue().getClass().getName());
            library.getMatchers().add(matcher);
        }

        return library;
    }

    /**
     * Gets the validation matcher library object from bean in application context. Bean is identified by its id.
     * @param id
     * @return
     */
    public ValidationMatcherLibraryDefinition getValidationMatcherLibrary(String id) {
        ValidationMatcherLibraryDefinition library = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, ValidationMatcherLibraryDefinition.class);

        if (library == null) {
            SpringBean springBean = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, SpringBean.class);
            if (springBean != null) {
                library = matcherLibrarySpringBeanConverter.convert(springBean);
            }
        }

        if (library == null) {
            throw new CitrusAdminRuntimeException(String.format("Unable to find validation matcher library definition for id '%s'", id));
        }

        return library;
    }

    /**
     * List all validation matcher libraries in application context.
     * @return
     */
    public List<ValidationMatcherLibraryDefinition> listValidationMatcherLibraries() {
        List<ValidationMatcherLibraryDefinition> libraries = new ArrayList<ValidationMatcherLibraryDefinition>();

        libraries.addAll(springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), ValidationMatcherLibraryDefinition.class));

        List<SpringBean> springBeans = springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), SpringBean.class, Collections.singletonMap("class", ValidationMatcherLibrary.class.getName()));
        for (SpringBean springBean : springBeans) {
            libraries.add(matcherLibrarySpringBeanConverter.convert(springBean));
        }

        return libraries;
    }

}
