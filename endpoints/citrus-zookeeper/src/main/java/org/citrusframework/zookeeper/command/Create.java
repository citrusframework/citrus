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

package org.citrusframework.zookeeper.command;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.zookeeper.client.ZooClient;
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
public class Create extends AbstractZooCommand<ZooResponse> {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(Create.class);

    public static final String ACL_ALL = "CREATOR_ALL_ACL";
    public static final String ACL_OPEN = "OPEN_ACL_UNSAFE";
    public static final String ACL_READ = "READ_ACL_UNSAFE";

    /**
     * Default constructor initializing the command name.
     */
    public Create() {
        super("zookeeper:create");
    }

    @Override
    public void execute(ZooClient zookeeperClient, TestContext context) {
        ZooResponse commandResult = new ZooResponse();
        setCommandResult(commandResult);

        String data = this.getParameter(DATA, context);
        String path = this.getParameter(PATH, context);
        String mode = this.getParameter(MODE, context);
        String acl = this.getParameter(ACL, context);

        String newPath = null;
        try {
            newPath = zookeeperClient.getZooKeeperClient().create(path, data.getBytes(), lookupAcl(acl), lookupCreateMode(mode));
        } catch (KeeperException | InterruptedException e) {
            throw new CitrusRuntimeException(e);
        }
        commandResult.setResponseParam(PATH, newPath);
        logger.debug(getCommandResult().toString());
    }

    /**
     * Sets the data parameter.
     * @param data
     * @return
     */
    public Create data(String data) {
        getParameters().put(DATA, data);
        return this;
    }

    /**
     * Sets the path parameter.
     * @param path
     * @return
     */
    public Create path(String path) {
        getParameters().put(PATH, path);
        return this;
    }

    /**
     * Sets the mode parameter.
     * @param mode
     * @return
     */
    public Create mode(String mode) {
        getParameters().put(MODE, mode);
        return this;
    }

    /**
     * Sets the acl parameter.
     * @param acl
     * @return
     */
    public Create acl(String acl) {
        getParameters().put(ACL, acl);
        return this;
    }

    private CreateMode lookupCreateMode(String mode) {
        return CreateMode.valueOf(mode);
    }

    private List<ACL> lookupAcl(String acl) {
        switch (acl) {
            case ACL_ALL:
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            case ACL_OPEN:
                return ZooDefs.Ids.OPEN_ACL_UNSAFE;
            case ACL_READ:
                return ZooDefs.Ids.READ_ACL_UNSAFE;
            default:
                throw new CitrusRuntimeException(String.format("ACL '%s' not supported", acl));
        }
    }
}
