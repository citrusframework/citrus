/*
 * Copyright the original author or authors.
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

package org.citrusframework.camel.util;

import java.net.URISyntaxException;
import java.util.Objects;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.camel.CamelContext;
import org.apache.camel.NoSuchBeanException;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.URISupport;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.context.CamelReferenceResolver;
import org.citrusframework.camel.endpoint.CamelEndpointConfiguration;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel utilities.
 */
public class CamelUtils {

    private static final Logger logger = LoggerFactory.getLogger(CamelUtils.class);

    private static volatile JAXBContext context;

    /**
     * Creates new Camel JaxB context.
     * @return
     * @throws JAXBException
     */
    public static JAXBContext getJaxbContext() throws JAXBException {
        if (context == null) {
            synchronized(CamelUtils.class) {
                context = JAXBContext.newInstance("org.apache.camel:org.apache.camel.model:org.apache.camel.model.cloud:" +
                        "org.apache.camel.model.config:org.apache.camel.model.dataformat:org.apache.camel.model.language:" +
                        "org.apache.camel.model.loadbalancer:org.apache.camel.model.rest:org.apache.camel.model.transformer:" +
                        "org.apache.camel.model.validator:org.apache.camel.core.xml:org.apache.camel.spring.xml:org.apache.camel.model", CamelUtils.class.getClassLoader());
            }
        }

        return context;
    }

    /**
     * Resolves endpoint uri with bean reference support, that resolves beans from Citrus context.
     */
    public static String resolveEndpointUri(TestContext context, CamelEndpointConfiguration endpointConfiguration) {
        String endpointUri;
        if (endpointConfiguration.getEndpointUri() != null) {
            endpointUri = context.replaceDynamicContentInString(endpointConfiguration.getEndpointUri()).trim();

            // check for bean references in endpoint uri
            if (endpointUri.contains("=#")) {
                CamelContext camelContext = resolveCamelContext(context.getReferenceResolver(), endpointConfiguration);
                if (camelContext != null) {
                    try {
                        URISupport.parseQuery(endpointUri).values().stream()
                                .filter(Objects::nonNull)
                                .filter(it -> it instanceof String)
                                .filter(it -> it.toString().startsWith("#"))
                                .peek(it -> logger.debug("Resolving bean reference '{}' in Camel endpoint uri", it))
                                .map(it -> it.toString().substring(1))
                                .forEach(beanRef -> {
                                    boolean missing;
                                    try {
                                        missing = camelContext.getRegistry().lookupByName(beanRef) == null;
                                    } catch (NoSuchBeanException e) {
                                        missing = true;
                                    }

                                    if (missing && context.getReferenceResolver().isResolvable(beanRef)) {
                                        logger.info("Propagating bean reference '{}' to Camel registry", beanRef);
                                        camelContext.getRegistry().bind(beanRef,
                                                context.getReferenceResolver().resolve(beanRef));
                                    }
                                });
                    } catch (URISyntaxException e) {
                        logger.warn("Failed to parse Camel endpoint uri", e);
                    }
                }
            }
        } else if (endpointConfiguration.getEndpoint() != null) {
            endpointUri = endpointConfiguration.getEndpoint().getEndpointUri();
        } else {
            throw new CitrusRuntimeException("Missing endpoint or endpointUri on Camel producer");
        }

        return endpointUri;
    }

    /**
     * Resolves Camel context from given Citrus test context.
     */
    public static CamelContext resolveCamelContext(ReferenceResolver referenceResolver,
                                                   CamelEndpointConfiguration endpointConfiguration) {
        if (endpointConfiguration != null && endpointConfiguration.getCamelContext() != null) {
            return endpointConfiguration.getCamelContext();
        }

        if (referenceResolver instanceof CamelReferenceResolver camelReferenceResolver &&
                camelReferenceResolver.getCamelContext() != null) {
            return camelReferenceResolver.getCamelContext();
        }

        if (referenceResolver.resolveAll(CamelContext.class).size() == 1) {
            return referenceResolver.resolve(CamelContext.class);
        }

        if (referenceResolver.isResolvable(CamelSettings.getContextName(), CamelContext.class)) {
            return referenceResolver.resolve(CamelSettings.getContextName(), CamelContext.class);
        }

        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start Camel context '%s'".formatted(CamelSettings.getContextName()), e);
        }
        referenceResolver.bind(CamelSettings.getContextName(), camelContext);

        return camelContext;
    }
}
