package com.consol.citrus.script;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class GroovyScriptBeanTest extends AbstractBaseTest {
    
    @Test
    public void testScript() {
        GroovyScriptBean bean = new GroovyScriptBean();
        bean.setScript("println 'Hello TestFramework!'");
        bean.execute(context);
    }
    
    @Test
    public void testFileResource() {
        GroovyScriptBean bean = new GroovyScriptBean();
        bean.setFileResource(new ClassPathResource("com/consol/citrus/script/example.groovy"));
        bean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testScriptFailure() {
        GroovyScriptBean bean = new GroovyScriptBean();
        bean.setScript("Some wrong script");
        bean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testFileNotFound() {
        GroovyScriptBean bean = new GroovyScriptBean();
        bean.setFileResource(new FileSystemResource("some/wrong/path/test.groovy"));
        bean.execute(context);
    }
}
