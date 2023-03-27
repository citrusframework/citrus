/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.citrus.zookeeper.command;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.zookeeper.client.ZooClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class Info extends AbstractZooCommand<ZooResponse> {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Info.class);

    /**
     * Default constructor initializing the command name.
     */
    public Info() {
        super("zookeeper:info");
    }

    @Override
    public void execute(ZooClient zookeeperClient, TestContext context) {
        ZooResponse commandResult = new ZooResponse();
        setCommandResult(commandResult);

        String state = zookeeperClient.getZooKeeperClient().getState().name();
        long sessionId = zookeeperClient.getZooKeeperClient().getSessionId();
        byte[] sessionPwd = zookeeperClient.getZooKeeperClient().getSessionPasswd();
        int sessionTimeout = zookeeperClient.getZooKeeperClient().getSessionTimeout();
        commandResult.setResponseParam("state", state);
        commandResult.setResponseParam("sessionId", sessionId);
        commandResult.setResponseParam("sessionPwd", sessionPwd);
        commandResult.setResponseParam("sessionTimeout", sessionTimeout);
        log.debug(getCommandResult().toString());
    }


}
