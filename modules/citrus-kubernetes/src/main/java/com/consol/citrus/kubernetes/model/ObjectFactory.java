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

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.consol.citrus.kubernetes.model
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link InfoMessage }
     *
     */
    public InfoMessage createInfoMessage() {
        return new InfoMessage();
    }

    /**
     * Create an instance of {@link ListPodsMessage }
     *
     */
    public ListPodsMessage createListPodsMessage() {
        return new ListPodsMessage();
    }

    /**
     * Create an instance of {@link WatchPodsMessage }
     *
     */
    public WatchPodsMessage createWatchPodsMessage() {
        return new WatchPodsMessage();
    }

    /**
     * Create an instance of {@link ListReplicationControllersMessage }
     *
     */
    public ListReplicationControllersMessage createListReplicationControllersMessage() {
        return new ListReplicationControllersMessage();
    }

    /**
     * Create an instance of {@link WatchReplicationControllersMessage }
     *
     */
    public WatchReplicationControllersMessage createWatchReplicationControllersMessage() {
        return new WatchReplicationControllersMessage();
    }

    /**
     * Create an instance of {@link ListServicesMessage }
     *
     */
    public ListServicesMessage createListServicesMessage() {
        return new ListServicesMessage();
    }

    /**
     * Create an instance of {@link WatchServicesMessage }
     *
     */
    public WatchServicesMessage createWatchServicesMessage() {
        return new WatchServicesMessage();
    }

    /**
     * Create an instance of {@link ListNodesMessage }
     *
     */
    public ListNodesMessage createListNodesMessage() {
        return new ListNodesMessage();
    }

    /**
     * Create an instance of {@link WatchNodesMessage }
     *
     */
    public WatchNodesMessage createWatchNodesMessage() {
        return new WatchNodesMessage();
    }

    /**
     * Create an instance of {@link ListNamespacesMessage }
     *
     */
    public ListNamespacesMessage createListNamespacesMessage() {
        return new ListNamespacesMessage();
    }

    /**
     * Create an instance of {@link WatchNamespacesMessage }
     *
     */
    public WatchNamespacesMessage createWatchNamespacesMessage() {
        return new WatchNamespacesMessage();
    }

    /**
     * Create an instance of {@link ListEventsMessage }
     *
     */
    public ListEventsMessage createListEventsMessage() {
        return new ListEventsMessage();
    }

    /**
     * Create an instance of {@link ListEndpointsMessage }
     *
     */
    public ListEndpointsMessage createListEndpointsMessage() {
        return new ListEndpointsMessage();
    }

}
