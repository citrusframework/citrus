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

package org.citrusframework.container;

import org.citrusframework.condition.HttpCondition;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class WaitHttpConditionBuilder extends WaitConditionBuilder<HttpCondition, WaitHttpConditionBuilder> {

    /**
     * Default constructor using fields.
     * @param builder
     */
    public WaitHttpConditionBuilder(Wait.Builder<HttpCondition> builder) {
        super(builder);
    }

    public WaitHttpConditionBuilder url(String requestUrl) {
        getCondition().setUrl(requestUrl);
        return this;
    }

    /**
     * Sets the Http connection timeout.
     * @param timeout
     * @return
     */
    public WaitHttpConditionBuilder timeout(String timeout) {
        getCondition().setTimeout(timeout);
        return this;
    }

    /**
     * Sets the Http connection timeout.
     * @param timeout
     * @return
     */
    public WaitHttpConditionBuilder timeout(Long timeout) {
        getCondition().setTimeout(timeout.toString());
        return this;
    }

    /**
     * Sets the Http status code to check.
     * @param status
     * @return
     */
    public WaitHttpConditionBuilder status(int status) {
        getCondition().setHttpResponseCode(String.valueOf(status));
        return this;
    }

    /**
     * Sets the Http method.
     * @param method
     * @return
     */
    public WaitHttpConditionBuilder method(String method) {
        getCondition().setMethod(method.toUpperCase());
        return this;
    }
}
