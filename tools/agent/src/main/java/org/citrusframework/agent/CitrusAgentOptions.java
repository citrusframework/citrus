/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.agent;

import java.util.Arrays;
import java.util.LinkedList;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.main.CitrusAppOptions;
import org.citrusframework.util.StringUtils;

public class CitrusAgentOptions extends CitrusAppOptions<CitrusAgentConfiguration> {

    protected CitrusAgentOptions() {
        super();

        options.add(new CliOption<>("P", "port", "Server port") {
            @Override
            protected void doProcess(CitrusAgentConfiguration configuration, String arg, String value, LinkedList<String> remainingArgs) {
                if (StringUtils.hasText(value)) {
                    configuration.setPort(Integer.parseInt(value));
                } else {
                    throw new CitrusRuntimeException("Missing parameter value for -P/--port option");
                }
            }
        });
    }

    public CitrusAgentConfiguration apply(CitrusAgentConfiguration configuration, String[] arguments) {
        LinkedList<String> args = new LinkedList<>(Arrays.asList(arguments));

        while (!args.isEmpty()) {
            String arg = args.removeFirst();

            for (CliOption<CitrusAgentConfiguration> option : options) {
                if (option.processOption(configuration, arg, args)) {
                    break;
                }
            }
        }

        return configuration;
    }
}
