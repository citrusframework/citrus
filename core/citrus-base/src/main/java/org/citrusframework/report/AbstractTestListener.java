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

package org.citrusframework.report;

import org.citrusframework.TestCase;

/**
 * Basic implementation of {@link TestListener} interface so that subclasses must not implement
 * all methods but only overwrite some listener methods.
 *
 */
public abstract class AbstractTestListener implements TestListener {

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {}

    @Override
    public void onTestExecutionEnd(TestCase test) {}

    @Override
    public void onTestSkipped(TestCase test) {}

    @Override
    public void onTestStart(TestCase test) {}

    @Override
    public void onTestSuccess(TestCase test) {}

}
