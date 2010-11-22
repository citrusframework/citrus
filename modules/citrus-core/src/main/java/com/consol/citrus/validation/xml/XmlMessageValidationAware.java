/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.validation.xml;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.ControlMessageValidationAware;

/**
 * Interface marking that a class is aware of XML message validation.
 * 
 * @author Christoph Deppisch
 */
public interface XmlMessageValidationAware extends ControlMessageValidationAware {
    
    /**
     * Get the validation context used for XML message validation.
     * 
     * @param context the current test context.
     * @return the validation context.
     */
    public XmlMessageValidationContext getXmlMessageValidationContext(TestContext context);
    
}
