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

package org.citrusframework.cucumber.backend;

import java.net.URI;
import java.util.Collection;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.Status;

public final class Scenario {

    private final TestCaseState delegate;

    Scenario(TestCaseState delegate) {
        this.delegate = delegate;
    }

    public Collection<String> getSourceTagNames() {
        return delegate.getSourceTagNames();
    }

    public Status getStatus() {
        return Status.valueOf(delegate.getStatus().name());
    }

    public boolean isFailed() {
        return delegate.isFailed();
    }

    public String getName() {
        return delegate.getName();
    }

    public String getId() {
        return delegate.getId();
    }

    public URI getUri() {
        return delegate.getUri();
    }

    public Integer getLine() {
        return delegate.getLine();
    }
}
