package com.consol.citrus.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class GroovyActionTest extends AbstractBaseTest {
    
    @Test
    public void testScript() {
        GroovyAction bean = new GroovyAction();
        bean.setScript("println 'Hello TestFramework!'");
        bean.execute(context);
    }
    
    @Test
    public void testFileResource() {
        GroovyAction bean = new GroovyAction();
        bean.setFileResource(new ClassPathResource("com/consol/citrus/script/example.groovy"));
        bean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testScriptFailure() {
        GroovyAction bean = new GroovyAction();
        bean.setScript("Some wrong script");
        bean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testFileNotFound() {
        GroovyAction bean = new GroovyAction();
        bean.setFileResource(new FileSystemResource("some/wrong/path/test.groovy"));
        bean.execute(context);
    }
}
