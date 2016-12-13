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

package com.consol.citrus.kubernetes.model;

import io.fabric8.kubernetes.client.Watcher;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class WatchEvent<T> {

    private T target;
    private Watcher.Action action;

    /**
     * Default constructor.
     */
    public WatchEvent() {
        super();
    }

    /**
     * Constructor using fields.
     * @param target
     * @param action
     */
    public WatchEvent(T target, Watcher.Action action) {
        this.target = target;
        this.action = action;
    }

    public T getTarget() {
        return target;
    }

    public void setTarget(T target) {
        this.target = target;
    }

    public Watcher.Action getAction() {
        return action;
    }

    public void setAction(Watcher.Action action) {
        this.action = action;
    }
}
