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

package org.citrusframework.actions.testcontainers;

import org.citrusframework.spi.Resource;

public interface TestcontainersComposeActionBuilder {

    /**
     * Start compose testcontainers instance.
     */
    TestcontainersComposeUpActionBuilder<?, ?, ?> up();

    /**
     * Start compose testcontainers instance.
     */
    default TestcontainersComposeUpActionBuilder<?, ?, ?> up(String filePath) {
        return up().file(filePath);
    }

    /**
     * Start compose testcontainers instance.
     */
    default TestcontainersComposeUpActionBuilder<?, ?, ?> up(Resource fileResource) {
        return up().file(fileResource);
    }

    /**
     * Stop compose testcontainers instance.
     */
    TestcontainersComposeDownActionBuilder<?, ?, ?> down();
}
