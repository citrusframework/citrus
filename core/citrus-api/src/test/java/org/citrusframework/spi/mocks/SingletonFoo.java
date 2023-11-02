package org.citrusframework.spi.mocks;

/**
 * @author Thorsten Schlathoelter
 */
public class SingletonFoo {

    public static final SingletonFoo INSTANCE = new SingletonFoo();

    private SingletonFoo() {
    }
}
