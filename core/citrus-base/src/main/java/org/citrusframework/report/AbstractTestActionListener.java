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

import org.citrusframework.TestAction;
import org.citrusframework.TestCase;

/**
 * Basic implementation of {@link TestActionListener} interface so that subclasses only have to overwrite 
 * methods of interest.
 *  
 * @author Christoph Deppisch
 * @since 1.3
 */
public abstract class AbstractTestActionListener implements TestActionListener {

    @Override
    public void onTestActionFinish(TestCase testCase, TestAction testAction) {}

    @Override
    public void onTestActionSkipped(TestCase testCase, TestAction testAction) {}

    @Override
    public void onTestActionStart(TestCase testCase, TestAction testAction) {}

}
