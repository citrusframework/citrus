/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.rmi.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiMarshaller extends Jaxb2Marshaller {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(RmiMarshaller.class);

    public RmiMarshaller() {
        setClassesToBeBound(RmiServiceInvocation.class,
                            RmiServiceResult.class);

        setSchema(new ClassPathResource("com/consol/citrus/schema/citrus-rmi-message.xsd"));

        try {
            afterPropertiesSet();
        } catch (Exception e) {
            log.warn("Failed to setup rmi message marshaller", e);
        }
    }
}
