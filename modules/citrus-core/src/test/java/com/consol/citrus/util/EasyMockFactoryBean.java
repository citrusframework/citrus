package com.consol.citrus.util;

import org.easymock.IMocksControl;
import org.easymock.EasyMock;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author roland
 * @since Apr 8, 2008
 */
public class EasyMockFactoryBean implements FactoryBean {

    boolean singleton = true;
    Class type;
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

    public Class getObjectType() {
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
