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
package org.citrusframework.jbang.commands;

import org.citrusframework.jbang.CitrusJBangMain;
import picocli.AutoComplete;
import picocli.CommandLine;

@CommandLine.Command(name = "complete", description = "Generate completion script for bash/zsh")
public class Complete extends CitrusCommand {

    public Complete(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() throws Exception {
        String script = AutoComplete.bash(
                spec.parent().name(),
                spec.parent().commandLine());

        // not PrintWriter.println: scripts with Windows line separators fail in strange ways!
        printer().print(script);
        printer().print("\n");
        return 0;
    }
}
