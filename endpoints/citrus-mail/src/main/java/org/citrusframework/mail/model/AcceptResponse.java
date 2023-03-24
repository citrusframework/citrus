/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.mail.model;

import jakarta.xml.bind.annotation.*;

/**
 * Response outcome for accept requests. Just says whether accept request is accepted or declined according
 * to boolean response outcome.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "accept"
})
@XmlRootElement(name = "accept-response")
public class AcceptResponse {

    /** Accept request outcome yes/no */
    private boolean accept;

    /**
     * Gets the accept outcome.
     * @return
     */
    public boolean isAccept() {
        return accept;
    }

    /**
     * Sets the accept flag to mark success of accept request.
     * @param accept
     */
    public void setAccept(boolean accept) {
        this.accept = accept;
    }
}
