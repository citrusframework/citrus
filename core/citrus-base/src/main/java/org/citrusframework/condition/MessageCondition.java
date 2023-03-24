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

package org.citrusframework.condition;

import org.citrusframework.context.TestContext;

/**
 * Condition checks whether a message is present in test context message store. Messages are automatically
 * stored in that store when sending and receiving messages with respective test actions. So this condition
 * can be used to wait for a message to arrive or being sent out.
 *
 * Message to check is identified by its name in the message store.
 *
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class MessageCondition extends AbstractCondition {

    /** Message that should be present in message store */
    private String messageName;

    @Override
    public boolean isSatisfied(TestContext context) {
        return context.getMessageStore().getMessage(context.replaceDynamicContentInString(messageName)) != null;
    }

    @Override
    public String getSuccessMessage(TestContext context) {
        return String.format("Message condition success - found message '%s' in message store", context.replaceDynamicContentInString(messageName));
    }

    @Override
    public String getErrorMessage(TestContext context) {
        return String.format("Message condition failed - unable to find message '%s' in message store", context.replaceDynamicContentInString(messageName));
    }

    /**
     * Sets the messageName property.
     *
     * @param messageName the message name to set
     */
    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    /**
     * Gets the value of the messageName property.
     *
     * @return the messageName
     */
    public String getMessageName() {
        return messageName;
    }

    @Override
    public String toString() {
        return "MessageCondition{" +
                "messageName='" + messageName + '\'' +
                ", name='" + getName() + '\'' +
                '}';
    }
}
