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

package org.citrusframework.report;

/**
 * Basic implementation of {@link TestSuiteListener} interface so that subclasses must not implement
 * all methods but only overwrite some listener methods.
 *  
 * @author Christoph Deppisch
 */
public abstract class AbstractTestSuiteListener implements TestSuiteListener {

    @Override
    public void onFinish() {}

    @Override
    public void onFinishFailure(Throwable cause) {}

    @Override
    public void onFinishSuccess() {}

    @Override
    public void onStart() {}

    @Override
    public void onStartFailure(Throwable cause) {}

    @Override
    public void onStartSuccess() {}
}
