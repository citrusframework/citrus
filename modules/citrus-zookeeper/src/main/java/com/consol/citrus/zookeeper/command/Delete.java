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
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class Delete extends AbstractZookeeperCommand<ZookeeperResponse> {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(Delete.class);

    /**
     * Default constructor initializing the command name.
     */
    public Delete() {
        super("zookeeper:delete");
    }

    @Override
    public void execute(ZookeeperClient zookeeperClient, TestContext context) {
        ZookeeperResponse commandResult = new ZookeeperResponse();
        setCommandResult(commandResult);

        String path = this.getParameter("path", context);
        int version = Integer.valueOf(this.getParameter("version", context));

        try {
            zookeeperClient.getZooKeeperClient().delete(path, version, getDeleteCallback(commandResult), null);
            waitAndRecordResponse(commandResult, 5);
        } catch (InterruptedException e) {
            throw new CitrusRuntimeException(e);
        }
        log.debug(getCommandResult().toString());
    }

    private AsyncCallback.VoidCallback getDeleteCallback(final ZookeeperResponse commandResult) {
        return new AsyncCallback.VoidCallback() {
            @Override
            public void processResult(int responseCode, String path, Object ctx) {
                commandResult.setResponseParam("responseCode", responseCode);
                commandResult.setResponseParam("path", path);
            }
        };
    }

    private void waitAndRecordResponse(final ZookeeperResponse commandResult, final int seconds) throws InterruptedException {
        int retryAttempts = seconds;
        while (!commandResult.hasResponseData() && retryAttempts > 0) {
            Thread.sleep(1000);
            retryAttempts--;
        }
    }
}
