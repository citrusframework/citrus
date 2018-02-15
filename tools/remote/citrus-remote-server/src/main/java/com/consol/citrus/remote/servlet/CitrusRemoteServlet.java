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

package com.consol.citrus.remote.servlet;

import com.consol.citrus.Citrus;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.main.CitrusAppConfiguration;
import com.consol.citrus.remote.CitrusRemoteConfiguration;
import com.consol.citrus.remote.controller.RunController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import spark.Filter;
import spark.servlet.SparkApplication;

import java.net.URLDecoder;
import java.util.Optional;

import static spark.Spark.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusRemoteServlet implements SparkApplication {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusRemoteServlet.class);

    /** Cached instance of Citrus */
    private Citrus citrus;

    private final CitrusRemoteConfiguration configuration;

    public CitrusRemoteServlet() {
        this(new CitrusRemoteConfiguration());
    }

    public CitrusRemoteServlet(CitrusRemoteConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void init() {
        before((Filter) (request, response) -> log.info(request.requestMethod() + " " + request.url() + Optional.ofNullable(request.queryString()).map(query -> "?" + query).orElse("")));

        get("/run", (req, res) -> {
            CitrusAppConfiguration citrusAppConfiguration = new CitrusAppConfiguration();

            if (req.queryParams().contains("package")) {
                citrusAppConfiguration.setPackageName(URLDecoder.decode(req.queryParams("package"), "UTF-8"));
            }

            if (req.queryParams().contains("class")) {
                String value = URLDecoder.decode(req.queryParams("class"), "UTF-8");
                String className;
                String methodName = null;
                if (value.contains("#")) {
                    className = value.substring(0, value.indexOf("#"));
                    methodName = value.substring(value.indexOf("#") + 1);
                } else {
                    className = value;
                }

                if (StringUtils.hasText(methodName)) {
                    citrusAppConfiguration.setTestMethod(methodName);
                }

                try {
                    citrusAppConfiguration.setTestClass(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    throw new CitrusRuntimeException("Unable to test class: " + className, e);
                }
            }

            citrusAppConfiguration.setConfigClass(configuration.getConfigClass());

            new RunController().run(getCitrus(), citrusAppConfiguration);
            return "";
        });

        exception(CitrusRuntimeException.class, (exception, request, response) -> {
            response.status(500);
            response.body(exception.getMessage());
        });
    }

    @Override
    public void destroy() {
        if (citrus != null && citrus.getApplicationContext() instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) citrus.getApplicationContext()).close();
        }
    }

    /**
     * Gets the Citrus instance.
     * @return
     */
    public Citrus getCitrus() {
        if (citrus == null)  {
            if (configuration.getConfigClass() != null) {
                citrus = Citrus.newInstance(configuration.getConfigClass());
            } else {
                citrus = Citrus.newInstance();
            }
        }

        return citrus;
    }
}
