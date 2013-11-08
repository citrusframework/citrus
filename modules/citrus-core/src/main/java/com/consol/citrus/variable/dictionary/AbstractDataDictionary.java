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

package com.consol.citrus.variable.dictionary;

import com.consol.citrus.validation.interceptor.AbstractMessageConstructionInterceptor;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractDataDictionary extends AbstractMessageConstructionInterceptor implements DataDictionary {

    /** Scope defines where dictionary should be applied (inbound, outbound, explicit, global) */
    private DictionaryScope scope = DictionaryScope.GLOBAL;

    @Override
    public DictionaryScope getScope() {
        return scope;
    }

    @Override
    public void setScope(DictionaryScope scope) {
        this.scope = scope;
    }
}
