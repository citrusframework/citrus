/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.util;

import org.easymock.IMocksControl;
import org.easymock.EasyMock;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * @author deppisch
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
