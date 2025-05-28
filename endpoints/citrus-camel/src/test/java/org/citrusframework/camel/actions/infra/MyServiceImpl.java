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

package org.citrusframework.camel.actions.infra;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyServiceImpl implements MyService {

    AtomicBoolean initialized = new AtomicBoolean(false);
    AtomicBoolean shutdown = new AtomicBoolean(false);

    public void initialize() {
        initialized.set(true);
    }

    public void shutdown() {
        shutdown.set(true);
    }

    @Override
    public String getServerUrl() {
        return "tcp://%s:%s".formatted(host(), port());
    }

    @Override
    public String host() {
        return "my-host";
    }

    @Override
    public int port() {
        return 18088;
    }

    @Override
    public boolean isFaultTolerant() {
        return false;
    }
}
