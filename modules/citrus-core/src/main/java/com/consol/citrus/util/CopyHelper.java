package com.consol.citrus.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.consol.citrus.exceptions.TestSuiteException;

public class CopyHelper {
    public static Object deepCopy(Object o) throws TestSuiteException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new ObjectOutputStream(baos).writeObject(o);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return new ObjectInputStream(bais).readObject();
        } catch (IOException e) {
            throw new TestSuiteException(e);
        } catch (ClassNotFoundException e) {
            throw new TestSuiteException(e);
        }
    }
}
