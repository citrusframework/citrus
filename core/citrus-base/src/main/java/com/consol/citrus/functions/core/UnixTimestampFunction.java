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

package com.consol.citrus.functions.core;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

/**
 * Function returning the actual timestamp.
 * 
 * @author Alexandr Kuznecov
 */
public class UnixTimestampFunction extends AbstractDateFunction {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(UnixTimestampFunction.class);

    /**
     * @see com.consol.citrus.functions.Function#execute(List, TestContext)
     * @throws CitrusRuntimeException
     */
    public String execute(List<String> parameterList, TestContext context) {
        long unixTimestamp;
        try {
            unixTimestamp = Instant.now().getEpochSecond();
        } catch (RuntimeException e) {
            log.error("Error while getting timestamp value ", e);
            throw new CitrusRuntimeException(e);
        }

        return Long.toString(unixTimestamp);
    }
}
