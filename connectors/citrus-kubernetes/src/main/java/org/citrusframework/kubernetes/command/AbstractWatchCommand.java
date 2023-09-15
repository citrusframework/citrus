/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.kubernetes.command;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ClientNonNamespaceOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractWatchCommand<R extends KubernetesResource, T extends KubernetesCommand<R>> extends AbstractClientCommand<ClientNonNamespaceOperation, R, T> {

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Watch handle */
    private Watch watch;

    /** Timeout to wait for watch result */
    private long timeout = 5000L;

    private BlockingQueue<WatchEventResult<R>> results = new ArrayBlockingQueue<>(1);
    private WatchEventResult<R> cachedResult;

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractWatchCommand(String name) {
        super("watch-" + name);
    }

    @Override
    public void execute(ClientNonNamespaceOperation operation, TestContext context) {
        watch = (Watch) operation.watch(new Watcher<R>() {
            @Override
            public void eventReceived(Action action, R resource) {
                if (results.isEmpty() && cachedResult == null) {
                    results.add(new WatchEventResult<>(resource, action));
                } else {
                    logger.debug("Ignoring watch result: " + action.name());
                }
            }

            @Override
            public void onClose(KubernetesClientException cause) {
                if (results.isEmpty()&& cachedResult == null) {
                    results.add(new WatchEventResult<>(cause));
                }
            }
        });
    }

    @Override
    public WatchEventResult<R> getCommandResult() {
        if (cachedResult != null) {
            return cachedResult;
        }

        try {
            WatchEventResult<R> watchEventResult = results.poll(timeout, TimeUnit.MILLISECONDS);
            if (watchEventResult == null) {
                throw new MessageTimeoutException(timeout, "watchEventResultQueue");
            }

            try {
                watch.close();
            } catch (KubernetesClientException e) {
                logger.warn("Failed to gracefully close watch", e);
            }

            watchEventResult.setWatch(watch);
            cachedResult = watchEventResult;
            return watchEventResult;
        } catch (InterruptedException e) {
            throw new CitrusRuntimeException("Failed to wait for watch result", e);
        }
    }

    /**
     * Gets the watch handle.
     * @return
     */
    public Watch getWatch() {
        return watch;
    }

    /**
     * Sets the timeout.
     *
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the timeout.
     *
     * @return
     */
    public long getTimeout() {
        return timeout;
    }
}
