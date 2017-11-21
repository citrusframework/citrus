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

package com.consol.citrus.validation.interceptor;

import com.consol.citrus.variable.dictionary.DataDictionary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * List of global message construction interceptors that modify message payload and message headers. User just has to add
 * interceptor implementation as bean to the Spring application context.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class GlobalMessageConstructionInterceptors {

    @Autowired(required = false)
    private List<MessageConstructionInterceptor> messageConstructionInterceptors = new ArrayList<>();

    /**
     * Sets the messageConstructionInterceptors property.
     *
     * @param messageConstructionInterceptors
     */
    public void setMessageConstructionInterceptors(List<MessageConstructionInterceptor> messageConstructionInterceptors) {
        this.messageConstructionInterceptors = messageConstructionInterceptors;
    }

    /**
     * Gets the messageConstructionInterceptors.
     *
     * @return
     */
    public List<MessageConstructionInterceptor> getMessageConstructionInterceptors() {
        return messageConstructionInterceptors.stream()
                .filter(interceptor -> !(interceptor instanceof DataDictionary) || ((DataDictionary) interceptor).isGlobalScope())
                .collect(Collectors.toList());
    }
}
