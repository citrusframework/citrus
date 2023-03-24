/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.context.resolver;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.xml.Marshaller;
import org.citrusframework.xml.MarshallerAdapter;
import org.springframework.oxm.Unmarshaller;

/**
 * @author Christoph Deppisch
 */
public class MarshallerAliasResolver implements TypeAliasResolver<Marshaller, org.springframework.oxm.Marshaller> {

    @Override
    public boolean isAliasFor(Class<?> sourceType) {
        return Marshaller.class.isAssignableFrom(sourceType);
    }

    @Override
    public Marshaller adapt(Object alias) {
        if (!org.springframework.oxm.Marshaller.class.isAssignableFrom(alias.getClass())) {
            throw new CitrusRuntimeException(String.format("Given alias object is not assignable from %s", org.springframework.oxm.Marshaller.class));
        }

        if (Unmarshaller.class.isAssignableFrom(alias.getClass())) {
            return new MarshallerAdapter((org.springframework.oxm.Marshaller) alias);
        } else {
            return new MarshallerAdapter((org.springframework.oxm.Marshaller) alias, null);
        }
    }

    @Override
    public Class<org.springframework.oxm.Marshaller> getAliasType() {
        return org.springframework.oxm.Marshaller.class;
    }
}
