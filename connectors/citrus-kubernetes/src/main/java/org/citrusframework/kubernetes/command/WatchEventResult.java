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

import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.client.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class WatchEventResult<R extends KubernetesResource> extends CommandResult<R> {

    private Watch watch;

    private Watcher.Action action;

    /**
     * Default constructor.
     */
    public WatchEventResult() {
        super();
    }

    /**
     * Constructor using fields.
     * @param result
     * @param action
     */
    public WatchEventResult(R result, Watcher.Action action) {
        super(result);
        this.action = action;
    }

    /**
     * Constructor using error.
     * @param error
     */
    public WatchEventResult(KubernetesClientException error) {
        super(error);
    }

    public Watcher.Action getAction() {
        return action;
    }

    public void setAction(Watcher.Action action) {
        this.action = action;
    }

    public Watch getWatch() {
        return watch;
    }

    public void setWatch(Watch watch) {
        this.watch = watch;
    }
}
