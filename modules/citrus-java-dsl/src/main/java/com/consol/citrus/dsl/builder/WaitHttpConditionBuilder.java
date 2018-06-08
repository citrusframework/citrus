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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.condition.HttpCondition;
import com.consol.citrus.container.Wait;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class WaitHttpConditionBuilder extends WaitConditionBuilder<HttpCondition> {

    /**
     * Default constructor using fields.
     * @param action
     * @param condition
     */
    public WaitHttpConditionBuilder(Wait action, HttpCondition condition) {
        super(action, condition);
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
    public WaitHttpConditionBuilder status(HttpStatus status) {
        getCondition().setHttpResponseCode(String.valueOf(status.value()));
        return this;
    }

    /**
     * Sets the Http method.
     * @param method
     * @return
     */
    public WaitHttpConditionBuilder method(HttpMethod method) {
        getCondition().setMethod(method.name());
        return this;
    }

    @Override
    public WaitHttpConditionBuilder ms(Long milliseconds) {
        return (WaitHttpConditionBuilder) super.ms(milliseconds);
    }

    @Override
    public WaitHttpConditionBuilder ms(String milliseconds) {
        return (WaitHttpConditionBuilder) super.ms(milliseconds);
    }

    @Override
    public WaitHttpConditionBuilder seconds(Long seconds) {
        return (WaitHttpConditionBuilder) super.seconds(seconds);
    }

    @Override
    public WaitHttpConditionBuilder seconds(String seconds) {
        return (WaitHttpConditionBuilder) super.seconds(seconds);
    }

    @Override
    public WaitHttpConditionBuilder interval(Long interval) {
        return (WaitHttpConditionBuilder) super.interval(interval);
    }

    @Override
    public WaitHttpConditionBuilder interval(String interval) {
        return (WaitHttpConditionBuilder) super.interval(interval);
    }
}
