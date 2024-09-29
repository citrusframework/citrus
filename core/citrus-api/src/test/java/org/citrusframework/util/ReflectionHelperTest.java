package org.citrusframework.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ReflectionHelperTest {

    @Test
    public void copyFields() {
        SubClass source = new SubClass("John Doe", 30, "Super John", 60);

        SubClass target = new SubClass(null, 0, null, 0);

        ReflectionHelper.copyFields(SubClass.class, source, target);

        Assert.assertEquals(target.name, "John Doe");
        Assert.assertEquals(target.age, 30);

        Assert.assertEquals(target.superName, "Super John");
        Assert.assertEquals(target.superAge, 60);

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
