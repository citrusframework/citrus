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
import org.apache.zookeeper.AsyncCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class Delete extends AbstractZooCommand<ZooResponse> {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(Delete.class);

    /**
     * Default constructor initializing the command name.
     */
    public Delete() {
        super("zookeeper:delete");
    }

    @Override
    public void execute(ZooClient zookeeperClient, TestContext context) {
        ZooResponse commandResult = new ZooResponse();
        setCommandResult(commandResult);

        String path = this.getParameter(PATH, context);
        int version = Integer.valueOf(this.getParameter(VERSION, context));

        try {
            zookeeperClient.getZooKeeperClient().delete(path, version, getDeleteCallback(commandResult), null);
            waitAndRecordResponse(commandResult, 5);
        } catch (InterruptedException e) {
            throw new CitrusRuntimeException(e);
        }
        logger.debug(getCommandResult().toString());
    }

    /**
     * Sets the path parameter.
     * @param path
     * @return
     */
    public Delete path(String path) {
        getParameters().put(PATH, path);
        return this;
    }

    /**
     * Sets the version parameter.
     * @param version
     * @return
     */
    public Delete version(int version) {
        getParameters().put(VERSION, version);
        return this;
    }

    private AsyncCallback.VoidCallback getDeleteCallback(final ZooResponse commandResult) {
        return new AsyncCallback.VoidCallback() {
            @Override
            public void processResult(int responseCode, String path, Object ctx) {
                commandResult.setResponseParam(RESPONSE_CODE, responseCode);
                commandResult.setResponseParam(PATH, path);
            }
        };
    }

    private void waitAndRecordResponse(final ZooResponse commandResult, final int seconds) throws InterruptedException {
        int retryAttempts = seconds;
        while (!commandResult.hasResponseData() && retryAttempts > 0) {
            Thread.sleep(1000);
            retryAttempts--;
        }
    }
}
