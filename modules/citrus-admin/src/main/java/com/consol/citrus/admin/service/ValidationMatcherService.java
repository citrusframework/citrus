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
import com.consol.citrus.model.config.core.ValidationMatcherLibrary;
import com.consol.citrus.validation.matcher.ValidationMatcher;
import com.consol.citrus.validation.matcher.ValidationMatcherConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
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
    public ValidationMatcherLibrary getDefaultValidationMatcherLibrary() {
        ValidationMatcherLibrary library = new ObjectFactory().createValidationMatcherLibrary();

        ValidationMatcherConfig config = new ValidationMatcherConfig();
        com.consol.citrus.validation.matcher.ValidationMatcherLibrary defaultValidationMatcher = config.getValidationMatcherLibrary();
        library.setId(defaultValidationMatcher.getName());
        library.setPrefix(defaultValidationMatcher.getPrefix());

        for (Map.Entry<String, ValidationMatcher> matcherEntry : defaultValidationMatcher.getMembers().entrySet()) {
            ValidationMatcherLibrary.Matcher matcher = new ValidationMatcherLibrary.Matcher();
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
    public ValidationMatcherLibrary getValidationMatcherLibrary(String id) {
        ValidationMatcherLibrary library = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, ValidationMatcherLibrary.class);

        if (library == null) {
            SpringBean springBean = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, SpringBean.class);
            if (springBean != null) {
                library = matcherLibrarySpringBeanConverter.convert(springBean);
            }
        }

        if (library == null) {
            new CitrusAdminRuntimeException(String.format("Unable to find validation matcher library definition for id '%s'", id));
        }

        return library;
    }

    /**
     * List all validation matcher libraries in application context.
     * @return
     */
    public List<ValidationMatcherLibrary> listValidationMatcherLibraries() {
        List<ValidationMatcherLibrary> libraries = new ArrayList<ValidationMatcherLibrary>();

        libraries.addAll(springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), ValidationMatcherLibrary.class));

        List<SpringBean> springBeans = springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), SpringBean.class, Collections.singletonMap("class", com.consol.citrus.validation.matcher.ValidationMatcherLibrary.class.getName()));
        for (SpringBean springBean : springBeans) {
            libraries.add(matcherLibrarySpringBeanConverter.convert(springBean));
        }

        return libraries;
    }

}
