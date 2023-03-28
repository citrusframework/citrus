/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.remote.servlet;

import org.citrusframework.remote.CitrusRemoteApplication;
import spark.servlet.SparkFilter;

import javax.servlet.*;
import java.util.Set;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusRemoteServletContainerInitializer implements ServletContainerInitializer {
    
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext servletContext) throws ServletException {
        FilterRegistration.Dynamic encodingFilter = servletContext.addFilter("spark-filter", SparkFilter.class);
        encodingFilter.setInitParameter(SparkFilter.APPLICATION_CLASS_PARAM, CitrusRemoteApplication.class.getName());
        encodingFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
