/*
 * Copyright the original author or authors.
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

package org.citrusframework.functions;

import org.citrusframework.CitrusSettings;
import org.citrusframework.functions.core.AbsoluteFunction;
import org.citrusframework.functions.core.AdvancedRandomNumberFunction;
import org.citrusframework.functions.core.AvgFunction;
import org.citrusframework.functions.core.CeilingFunction;
import org.citrusframework.functions.core.ChangeDateFunction;
import org.citrusframework.functions.core.ConcatFunction;
import org.citrusframework.functions.core.CurrentDateFunction;
import org.citrusframework.functions.core.DecodeBase64Function;
import org.citrusframework.functions.core.DigestAuthHeaderFunction;
import org.citrusframework.functions.core.EncodeBase64Function;
import org.citrusframework.functions.core.EscapeJsonFunction;
import org.citrusframework.functions.core.FloorFunction;
import org.citrusframework.functions.core.LoadMessageFunction;
import org.citrusframework.functions.core.LocalHostAddressFunction;
import org.citrusframework.functions.core.LowerCaseFunction;
import org.citrusframework.functions.core.MaxFunction;
import org.citrusframework.functions.core.MinFunction;
import org.citrusframework.functions.core.RandomEnumValueFunction;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.functions.core.RandomPatternFunction;
import org.citrusframework.functions.core.RandomStringFunction;
import org.citrusframework.functions.core.RandomUUIDFunction;
import org.citrusframework.functions.core.ReadFileResourceFunction;
import org.citrusframework.functions.core.RoundFunction;
import org.citrusframework.functions.core.StringLengthFunction;
import org.citrusframework.functions.core.SubstringAfterFunction;
import org.citrusframework.functions.core.SubstringBeforeFunction;
import org.citrusframework.functions.core.SubstringFunction;
import org.citrusframework.functions.core.SumFunction;
import org.citrusframework.functions.core.SystemPropertyFunction;
import org.citrusframework.functions.core.TranslateFunction;
import org.citrusframework.functions.core.UnixTimestampFunction;
import org.citrusframework.functions.core.UpperCaseFunction;
import org.citrusframework.functions.core.UrlDecodeFunction;
import org.citrusframework.functions.core.UrlEncodeFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFunctionLibrary extends FunctionLibrary {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultFunctionLibrary.class);

    /**
     * Default constructor adding default function implementations.
     */
    public DefaultFunctionLibrary() {
        setName("citrusFunctionLibrary");

        addMember("randomNumber", new RandomNumberFunction());
        addMember("randomNumberGenerator", new AdvancedRandomNumberFunction());
        addMember("randomString", new RandomStringFunction());
        addMember("randomPattern", new RandomPatternFunction());
        addMember("concat", new ConcatFunction());
        addMember("currentDate", new CurrentDateFunction());
        addMember("substring", new SubstringFunction());
        addMember("stringLength", new StringLengthFunction());
        addMember("translate", new TranslateFunction());
        addMember("substringBefore", new SubstringBeforeFunction());
        addMember("substringAfter", new SubstringAfterFunction());
        addMember("round", new RoundFunction());
        addMember("floor", new FloorFunction());
        addMember("ceiling", new CeilingFunction());
        addMember("upperCase", new UpperCaseFunction());
        addMember("lowerCase", new LowerCaseFunction());
        addMember("average", new AvgFunction());
        addMember("minimum", new MinFunction());
        addMember("maximum", new MaxFunction());
        addMember("sum", new SumFunction());
        addMember("absolute", new AbsoluteFunction());
        addMember("randomEnumValue", new RandomEnumValueFunction());
        addMember("randomUUID", new RandomUUIDFunction());
        addMember("encodeBase64", new EncodeBase64Function());
        addMember("decodeBase64", new DecodeBase64Function());
        addMember("urlEncode", new UrlEncodeFunction());
        addMember("urlDecode", new UrlDecodeFunction());
        addMember("digestAuthHeader", new DigestAuthHeaderFunction());
        addMember("localHostAddress", new LocalHostAddressFunction());
        addMember("changeDate", new ChangeDateFunction());
        addMember("readFile", new ReadFileResourceFunction());
        addMember("message", new LoadMessageFunction());
        addMember("systemProperty", new SystemPropertyFunction());
        addMember("unixTimestamp", new UnixTimestampFunction());
        addMember("escapeJson", new EscapeJsonFunction());

        lookupFunctions();
    }

    /**
     * Add custom function implementations loaded from resource path lookup.
     */
    private void lookupFunctions() {
        boolean allowOverride = CitrusSettings.isAllowFunctionOverride();

        Function.lookup().forEach((k, m) -> {
            if (allowOverride) {
                getMembers().put(k, m);
            } else {
                addMember(k, m);
            }
            logger.debug("Register function '{}' as {}", k, m.getClass());
        });
    }
}
