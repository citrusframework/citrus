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

package org.citrusframework.agent.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;
import org.citrusframework.util.StringUtils;

public class ImageConfiguration {

    @Parameter(property = "citrus.agent.image.registry", defaultValue = "quay.io")
    private String registry;
    @Parameter(property = "citrus.agent.image.name", defaultValue = "citrusframework/citrus-agent")
    private String name;
    @Parameter(property = "citrus.agent.image.tag", defaultValue = "latest")
    private String tag;

    public ImageConfiguration() {
        registry = "quay.io";
        name = "citrusframework/citrus-agent";
        tag = "latest";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getImage() {
        if (StringUtils.hasText(registry)) {
            return "%s/%s:%s".formatted(registry, name, tag);
        }

        return "%s:%s".formatted(name, tag);
    }
}
