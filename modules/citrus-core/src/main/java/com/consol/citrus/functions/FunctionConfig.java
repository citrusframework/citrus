/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.functions;

import com.consol.citrus.functions.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Configuration
public class FunctionConfig {

    private final RandomNumberFunction randomNumberFunction = new RandomNumberFunction();
    private final RandomStringFunction randomStringFunction = new RandomStringFunction();
    private final ConcatFunction concatFunction = new ConcatFunction();
    private final CurrentDateFunction currentDateFunction = new CurrentDateFunction();
    private final SubstringFunction substringFunction = new SubstringFunction();
    private final StringLengthFunction stringLengthFunction = new StringLengthFunction();
    private final TranslateFunction translateFunction = new TranslateFunction();
    private final SubstringBeforeFunction substringBeforeFunction = new SubstringBeforeFunction();
    private final SubstringAfterFunction substringAfterFunction = new SubstringAfterFunction();
    private final RoundFunction roundFunction = new RoundFunction();
    private final FloorFunction floorFunction = new FloorFunction();
    private final CeilingFunction ceilingFunction = new CeilingFunction();
    private final UpperCaseFunction upperCaseFunction = new UpperCaseFunction();
    private final LowerCaseFunction lowerCaseFunction = new LowerCaseFunction();
    private final AvgFunction avgFunction = new AvgFunction();
    private final MinFunction minFunction = new MinFunction();
    private final MaxFunction maxFunction = new MaxFunction();
    private final SumFunction sumFunction = new SumFunction();
    private final AbsoluteFunction absolutFunction = new AbsoluteFunction();
    private final RandomEnumValueFunction randomEnumValueFunction = new RandomEnumValueFunction();
    private final RandomUUIDFunction randomUuidFunction = new RandomUUIDFunction();
    private final CreateCDataSectionFunction createCDataSectionFunction = new CreateCDataSectionFunction();
    private final EscapeXmlFunction escapeXmlFunction = new EscapeXmlFunction();
    private final EncodeBase64Function encodeBase64Function = new EncodeBase64Function();
    private final DecodeBase64Function decodeBase64Function = new DecodeBase64Function();
    private final DigestAuthHeaderFunction digestAuthHeaderFunction = new DigestAuthHeaderFunction();
    private final LocalHostAddressFunction localHostAddressFunction = new LocalHostAddressFunction();
    private final ChangeDateFunction changeDateFunction = new ChangeDateFunction();
    private final ReadFileResourceFunction readFileResourceFunction = new ReadFileResourceFunction();

    @Bean(name = "functionRegistry")
    public FunctionRegistry getFunctionRegistry() {
        return new FunctionRegistry();
    }

    @Bean(name="citrusFunctionLibrary")
    public FunctionLibrary getFunctionaLibrary() {
        FunctionLibrary citrusFunctionLibrary = new FunctionLibrary();

        citrusFunctionLibrary.setPrefix("citrus:");
        citrusFunctionLibrary.setName("citrusFunctionLibrary");

        citrusFunctionLibrary.getMembers().put("randomNumber", randomNumberFunction);
        citrusFunctionLibrary.getMembers().put("randomString", randomStringFunction);
        citrusFunctionLibrary.getMembers().put("concat", concatFunction);
        citrusFunctionLibrary.getMembers().put("currentDate", currentDateFunction);
        citrusFunctionLibrary.getMembers().put("substring", substringFunction);
        citrusFunctionLibrary.getMembers().put("stringLength", stringLengthFunction);
        citrusFunctionLibrary.getMembers().put("translate", translateFunction);
        citrusFunctionLibrary.getMembers().put("substringBefore", substringBeforeFunction);
        citrusFunctionLibrary.getMembers().put("substringAfter", substringAfterFunction);
        citrusFunctionLibrary.getMembers().put("round", roundFunction);
        citrusFunctionLibrary.getMembers().put("floor", floorFunction);
        citrusFunctionLibrary.getMembers().put("ceiling", ceilingFunction);
        citrusFunctionLibrary.getMembers().put("upperCase", upperCaseFunction);
        citrusFunctionLibrary.getMembers().put("lowerCase", lowerCaseFunction);
        citrusFunctionLibrary.getMembers().put("average", avgFunction);
        citrusFunctionLibrary.getMembers().put("minimum", minFunction);
        citrusFunctionLibrary.getMembers().put("maximum", maxFunction);
        citrusFunctionLibrary.getMembers().put("sum", sumFunction);
        citrusFunctionLibrary.getMembers().put("absolute", absolutFunction);
        citrusFunctionLibrary.getMembers().put("randomEnumValue", randomEnumValueFunction);
        citrusFunctionLibrary.getMembers().put("randomUUID", randomUuidFunction);
        citrusFunctionLibrary.getMembers().put("cdataSection", createCDataSectionFunction);
        citrusFunctionLibrary.getMembers().put("escapeXml", escapeXmlFunction);
        citrusFunctionLibrary.getMembers().put("encodeBase64", encodeBase64Function);
        citrusFunctionLibrary.getMembers().put("decodeBase64", decodeBase64Function);
        citrusFunctionLibrary.getMembers().put("digestAuthHeader", digestAuthHeaderFunction);
        citrusFunctionLibrary.getMembers().put("localHostAddress", localHostAddressFunction);
        citrusFunctionLibrary.getMembers().put("changeDate", changeDateFunction);
        citrusFunctionLibrary.getMembers().put("readFile", readFileResourceFunction);

        return citrusFunctionLibrary;
    }
}
