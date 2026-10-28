package org.citrusframework.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReflectionHelperTest {

    @Test
    public void copyFields() {
        SubClass source = new SubClass("John Doe", 30, "Super John", 60);

        SubClass target = new SubClass(null, 0, null, 0);

        ReflectionHelper.copyFields(SubClass.class, source, target);

        Assertions.assertEquals("John Doe", target.name);
        Assertions.assertEquals(30, target.age);

        Assertions.assertEquals("Super John", target.superName);
        Assertions.assertEquals(60, target.superAge);

    }

    private static class SuperClass {

        String superName;
        int superAge;

        public SuperClass(String superName, int superAge) {
            this.superName = superName;
            this.superAge = superAge;
        }
    }

    private static class SubClass extends SuperClass {

        String name;
        int age;

        public SubClass(String name, int age, String superName, int superAge) {
            super(superName, superAge);
            this.name = name;
            this.age = age;
        }
    }

}
