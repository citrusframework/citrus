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

package org.citrusframework.zookeeper.client;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class ZooClientConfig {

    private String id;
    private String url;
    private int timeout;

    public ZooClientConfig() {
    }

    public ZooClientConfig(String id, String url, int timeout) {
        this.id = id;
        this.url = url;
        this.timeout = timeout;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public static ZooKeeperClientConfigBuilder createDefaultConfigBuilder() {
        return new ZooKeeperClientConfigBuilder();
    }

    public static class ZooKeeperClientConfigBuilder {
        public static final String DEFAULT_URL = "localhost:2181";
        public static final int DEFAULT_TIMEOUT = 2000;

        private String id;
        private String url = DEFAULT_URL;
        private int timeout = DEFAULT_TIMEOUT;

        public ZooKeeperClientConfigBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public ZooKeeperClientConfigBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public ZooKeeperClientConfigBuilder withTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public ZooClientConfig build() {
            return new ZooClientConfig(id, url, timeout);
        }

    }
}
