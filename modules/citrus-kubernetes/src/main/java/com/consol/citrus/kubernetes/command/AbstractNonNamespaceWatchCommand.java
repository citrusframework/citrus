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

package com.consol.citrus.kubernetes.command;

import com.consol.citrus.kubernetes.model.WatchError;
import com.consol.citrus.kubernetes.model.WatchEvent;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ClientNonNamespaceOperation;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractNonNamespaceWatchCommand<R, T extends AbstractClientCommand> extends AbstractClientCommand<ClientNonNamespaceOperation, WatchEvent<R>, T> {

    private ConcurrentLinkedQueue<WatchEvent<R>> results = new ConcurrentLinkedQueue<>();

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractNonNamespaceWatchCommand(String name) {
        super(name + ":watch");
    }

    @Override
    public void execute(ClientNonNamespaceOperation operation) {
        operation.watch(new Watcher<R>() {
            @Override
            public void eventReceived(Action action, R resource) {
                results.add(new WatchEvent<>(resource, action));
            }

            @Override
            public void onClose(KubernetesClientException cause) {
                results.add(new WatchError<>(cause));
            }
        });
    }

    @Override
    public WatchEvent<R> getCommandResult() {
        return results.poll();
    }
}
