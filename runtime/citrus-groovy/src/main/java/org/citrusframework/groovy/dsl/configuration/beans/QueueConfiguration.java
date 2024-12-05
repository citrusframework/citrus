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

package org.citrusframework.groovy.dsl.configuration.beans;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.spi.ReferenceResolver;

public class QueueConfiguration {

    private final ReferenceResolver referenceResolver;

    public QueueConfiguration(Citrus citrus) {
        this(citrus.getCitrusContext());
    }

    public QueueConfiguration(CitrusContext citrusContext) {
        this(citrusContext.getReferenceResolver());
    }

    public QueueConfiguration(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public void queue(String name) {
        referenceResolver.bind(name, new DefaultMessageQueue(name));
    }
}
