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

package org.citrusframework.zookeeper.command;

import org.apache.zookeeper.data.Stat;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class CommandHelper {
    public static final void parseStatResponse(ZooResponse commandResult, Stat stat) {
        if (stat == null) {
            return;
        }
        commandResult.setResponseParam("aversion", stat.getAversion());
        commandResult.setResponseParam("ctime", stat.getCtime());
        commandResult.setResponseParam("cversion", stat.getCversion());
        commandResult.setResponseParam("czxid", stat.getCzxid());
        commandResult.setResponseParam("dataLength", stat.getDataLength());
        commandResult.setResponseParam("ephemeralOwner", stat.getEphemeralOwner());
        commandResult.setResponseParam("mtime", stat.getMtime());
        commandResult.setResponseParam("mzxid", stat.getMzxid());
        commandResult.setResponseParam("numChildren", stat.getNumChildren());
        commandResult.setResponseParam("pzxid", stat.getPzxid());
        commandResult.setResponseParam("version", stat.getVersion());
    }
}
