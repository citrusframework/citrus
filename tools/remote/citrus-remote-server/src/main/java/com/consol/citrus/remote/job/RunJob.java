/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.remote.job;

import com.consol.citrus.main.TestRunConfiguration;
import com.consol.citrus.remote.model.RemoteResult;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class RunJob implements Callable<List<RemoteResult>> {

    private final TestRunConfiguration runConfiguration;

    /**
     * Default constructor using run configuration.
     * @param runConfiguration
     */
    public RunJob(TestRunConfiguration runConfiguration) {
        this.runConfiguration = runConfiguration;
    }

    @Override
    public List<RemoteResult> call() {
        return run(runConfiguration);
    }

    /**
     * Subclasses must implement this method for executing the tests based on given configuration.
     * @param runConfiguration
     * @return
     */
    protected abstract List<RemoteResult> run(TestRunConfiguration runConfiguration);
}
