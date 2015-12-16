/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.zookeeper.command;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.zookeeper.client.ZookeeperClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class Create extends AbstractZookeeperCommand<ZookeeperResponse> {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(Create.class);

    /**
     * Default constructor initializing the command name.
     */
    public Create() {
        super("zookeeper:create");
    }

    @Override
    public void execute(ZookeeperClient zookeeperClient, TestContext context) {
        ZookeeperResponse commandResult = new ZookeeperResponse();
        setCommandResult(commandResult);

        String data = this.getParameter("data", context);
        String path = this.getParameter("path", context);
        String mode = this.getParameter("mode", context);
        String acl = this.getParameter("acl", context);

        String newPath = null;
        try {
            newPath = zookeeperClient.getZooKeeperClient().create(path, data.getBytes(), lookupAcl(acl), lookupCreateMode(mode));
        } catch (KeeperException | InterruptedException e) {
            throw new CitrusRuntimeException(e);
        }
        commandResult.setResponseParam("path", newPath);
        log.debug(getCommandResult().toString());
    }

    private CreateMode lookupCreateMode(String mode) {
        return CreateMode.valueOf(mode);
    }

    private List<ACL> lookupAcl(String acl) {
        switch (acl) {
            case "CREATOR_ALL_ACL":
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            case "OPEN_ACL_UNSAFE":
                return ZooDefs.Ids.OPEN_ACL_UNSAFE;
            case "READ_ACL_UNSAFE":
                return ZooDefs.Ids.READ_ACL_UNSAFE;
            default:
                throw new CitrusRuntimeException(String.format("ACL '%s' not supported", acl));
        }
    }
}
