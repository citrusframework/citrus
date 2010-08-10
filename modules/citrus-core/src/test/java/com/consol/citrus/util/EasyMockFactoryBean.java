/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.util;

import org.easymock.IMocksControl;
import org.easymock.EasyMock;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Christoph Deppisch
 */
public class EasyMockFactoryBean implements FactoryBean {

    boolean singleton = true;
    Class<?> type;
    IMocksControl mockControl;
    org.easymock.classextension.IMocksControl classMockControl;

    public Object getObject() throws Exception {
        if(type.isInterface()){
            if(mockControl == null) {
                mockControl = EasyMock.createControl();
            }
            return mockControl.createMock(getObjectType());
        }else{
            if(classMockControl == null) {
               classMockControl = org.easymock.classextension.EasyMock.createControl();
            }
            return classMockControl.createMock(getObjectType());
        }
    }

    public Class<?> getObjectType() {
        return type;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean pSingleton) {
        singleton = pSingleton;
    }

    @Required
    public void setType(String pType) {
        try {
            type = Class.forName(pType);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("No class with name " + pType + " exist");
        }
    }

    public void setMockControl(IMocksControl pMockControl) {
        mockControl = pMockControl;
    }
}
