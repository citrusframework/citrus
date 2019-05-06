/*
 * Copyright 2019-2019 the original author or authors.
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

package com.consol.citrus.context;

/**
 * Immutable class that holds information about the result of evaluation of an XPath assertion.
 * Holds the XPath expression that was evaluated, the expected and actual results.
 */
public class XpathAssertionResult {

    /** Actual result of the XPath evaluation. */
    private String actualResult;

    /** Expected result of the XPath evaluation. */
    private String expectedResult;

    /** XPath expression that was evaluated. */
    private String xpathExpression;

    /**
     * Construct an XpathAssertionResult from the XPath expression, expected and actual results.
     * @param xpathExpression XPath expression that was evaluated
     * @param expectedResult Expected result of evaluation
     * @param actualResult Actual result of evaluation
     */
    public XpathAssertionResult(String xpathExpression, String expectedResult, String actualResult) {
        this.xpathExpression = xpathExpression;
        this.expectedResult = expectedResult;
        this.actualResult = actualResult;
    }

    /*
     * Return the actual result of the XPath evaluation.
     */
    public String getActualResult() {
        return this.actualResult;
    }

    /*
     * Return the expected result of the XPath evaluation.
     */
    public String getExpectedResult() {
        return this.expectedResult;
    }

    /*
     * Return the XPath expression.
     */
    public String getXpathExpression() {
        return this.xpathExpression;
    }

    public String toString() {
        return "XPath assertion failure - " + this.xpathExpression + " : expected = " + this.expectedResult + ", actual = " + this.actualResult;
    }

}
