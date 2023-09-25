/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.jmx.mbean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class HelloBeanImpl implements HelloBean {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(HelloBeanImpl.class);

    private String helloMessage;

    @Override
    public String getHelloMessage() {
        return helloMessage;
    }

    @Override
    public void setHelloMessage(String message) {
        this.helloMessage = message;
    }

    @Override
    public String hello(String username) {
        logger.info(String.format(helloMessage, username));
        return String.format(helloMessage, username);
    }
}
