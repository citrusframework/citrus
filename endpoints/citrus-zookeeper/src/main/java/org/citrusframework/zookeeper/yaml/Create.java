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

package org.citrusframework.zookeeper.yaml;

import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.zookeeper.command.ZooCommand;

public class Create {

    private final org.citrusframework.zookeeper.command.Create command = new org.citrusframework.zookeeper.command.Create();

    @SchemaProperty(description = "The path of the node to create.", required = true)
    public void setPath(String path) {
        command.path(path);
    }

    @SchemaProperty(description = "The data of the node to create.")
    public void setData(String data) {
        command.data(data);
    }

    @SchemaProperty(description = "The creation mode of the node to create.")
    public void setMode(String mode) {
        command.mode(mode);
    }

    @SchemaProperty(description = "The access control list of the node to create.")
    public void setAcl(String acl) {
        command.acl(acl);
    }

    public ZooCommand<?> getCommand() {
        return command;
    }
}
