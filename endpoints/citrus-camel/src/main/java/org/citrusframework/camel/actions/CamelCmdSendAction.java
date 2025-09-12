/*
 * Copyright the original author or authors.
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

package org.citrusframework.camel.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.citrusframework.actions.camel.CamelJBangCmdSendActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel JBang send command.
 */
public class CamelCmdSendAction extends AbstractCamelJBangAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelCmdSendAction.class);

    /** Camel integration name */
    private final String integrationName;

    /** Send timeout */
    private final String timeout;

    /** Message headers */
    private final List<String> headers;

    /** Body content */
    private final String body;

    /** Body content as file resource */
    private final Resource bodyResource;

    /** Endpoint to invoke */
    private final String endpoint;

    /** Endpoint URI to invoke */
    private final String endpointUri;

    /** Camel JBang command arguments */
    private final List<String> args;

    /** Wait for reply */
    private final boolean reply;

    /**
     * Default constructor.
     */
    public CamelCmdSendAction(CamelCmdSendAction.Builder builder) {
        super("cmd-send", builder);
        this.integrationName = builder.integrationName;
        this.timeout = builder.timeout;
        this.headers = builder.headers;
        this.body = builder.body;
        this.bodyResource = builder.bodyResource;
        this.endpoint = builder.endpoint;
        this.endpointUri = builder.endpointUri;
        this.args = builder.args;
        this.reply = builder.reply;
    }

    @Override
    public void doExecute(TestContext context) {
        List<String> commandArgs = new ArrayList<>();

        if (StringUtils.hasText(integrationName)) {
            logger.info("Camel JBang cmd sending message to integration '%s'".formatted(integrationName));
            commandArgs.add(context.replaceDynamicContentInString(integrationName));
        } else {
            logger.info("Camel JBang cmd sending message to current Camel integration");
        }

        commandArgs.add("--timeout");
        commandArgs.add(context.replaceDynamicContentInString(timeout));

        for (String header : headers) {
            commandArgs.add("--header");
            commandArgs.add(context.replaceDynamicContentInString(header));
        }

        if (StringUtils.hasText(body)) {
            commandArgs.add("--body");
            commandArgs.add(StringUtils.quote(context.replaceDynamicContentInString(body), true));
        } else if (bodyResource != null) {
            commandArgs.add("--body");
            commandArgs.add("file:" + bodyResource.getLocation());
        }

        if (StringUtils.hasText(endpoint)) {
            commandArgs.add("--endpoint");
            commandArgs.add(endpoint);
        }

        if (StringUtils.hasText(endpointUri)) {
            commandArgs.add("--uri");
            commandArgs.add(endpointUri);
        }

        if (reply) {
            commandArgs.add("--reply");
        }

        if (!args.isEmpty()) {
            commandArgs.addAll(context.resolveDynamicValuesInList(args));
        }

        ProcessAndOutput pao = camelJBang().send(commandArgs.toArray(new String[0]));

        if (reply) {
            logger.info("Received reply from Camel JBang send command:%n%s".formatted(pao.getOutput()));
        }

        logger.info("Successfully sent message to Camel integration via Camel JBang");
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelCmdSendAction, Builder>
            implements CamelJBangCmdSendActionBuilder<CamelCmdSendAction, Builder> {

        private String integrationName;
        private String timeout = "20000";
        private final List<String> headers = new ArrayList<>();
        private String body;
        private Resource bodyResource;
        private String endpoint;
        private String endpointUri;
        private final List<String> args = new ArrayList<>();
        private boolean reply;

        @Override
        public Builder integration(String name) {
            this.integrationName = name;
            return this;
        }

        @Override
        public Builder timeout(long timeout) {
            this.timeout = Long.toString(timeout);
            return this;
        }

        @Override
        public Builder timeout(String timeout) {
            this.timeout = timeout;
            return this;
        }

        @Override
        public Builder header(String name, String value) {
            this.headers.add("%s=%s".formatted(name, value));
            return this;
        }

        @Override
        public Builder headers(Map<String, String> values) {
            values.forEach((k, v) -> this.headers.add(String.format("%s=%s", k, v)));
            return this;
        }

        @Override
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        @Override
        public Builder body(Resource body) {
            this.bodyResource = body;
            return this;
        }

        @Override
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        @Override
        public Builder endpointUri(String uri) {
            this.endpointUri = uri;
            return this;
        }

        @Override
        public Builder withArg(String arg) {
            this.args.add(arg);
            return this;
        }

        @Override
        public Builder withArg(String name, String value) {
            this.args.add(name);
            this.args.add(value);
            return this;
        }

        @Override
        public Builder withArgs(String... args) {
            this.args.addAll(Arrays.asList(args));
            return this;
        }

        @Override
        public Builder reply(boolean reply) {
            this.reply = reply;
            return this;
        }

        @Override
        public CamelCmdSendAction doBuild() {
            return new CamelCmdSendAction(this);
        }
    }
}
