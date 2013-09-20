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

package com.consol.citrus.admin.jackson;

import com.consol.citrus.TestAction;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

/**
 * Builds proper type id resolver for Citrus test actions.
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class CitrusTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {

    /**
     * Default constructor with field initialization.
     */
    public CitrusTypeResolverBuilder() {
        super(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        init(JsonTypeInfo.Id.CUSTOM, new CitrusTypeNameIdResolver());
        inclusion(JsonTypeInfo.As.PROPERTY);
        typeProperty("type");
    }

    @Override
    public boolean useForType(JavaType t) {
        // just use this type resolver for test action instances
        return t.getRawClass().isAssignableFrom(TestAction.class);
    }
}
