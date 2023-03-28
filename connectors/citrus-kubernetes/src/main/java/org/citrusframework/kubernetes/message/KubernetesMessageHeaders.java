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

package org.citrusframework.kubernetes.message;

import org.citrusframework.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesMessageHeaders {

    /**
     * Prevent instantiation.
     */
    private KubernetesMessageHeaders() {
    }

    public static final String KUBERNETES_PREFIX = MessageHeaders.PREFIX + "kubernetes_";

    public static final String COMMAND = KUBERNETES_PREFIX + "command";
    public static final String ACTION = KUBERNETES_PREFIX + "action";

    /** Parameter names */
    public static final String LABEL = KUBERNETES_PREFIX + "label";
    public static final String NAME = KUBERNETES_PREFIX + "name";
    public static final String NAMESPACE = KUBERNETES_PREFIX + "namespace";
}
