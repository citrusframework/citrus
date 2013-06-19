package com.consol.citrus.admin.handlebars;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class HandlebarsHelperSourceTest {

    @Test
    public void testFolderToPackage() throws Exception {
        HandlebarsHelperSource helperSource = new HandlebarsHelperSource();

        Assert.assertEquals(helperSource.folderToPackage("com/consol/citrus/admin/sample"), "com.consol.citrus.admin.sample");
        Assert.assertEquals(helperSource.folderToPackage("com.consol.citrus.admin.sample"), "com.consol.citrus.admin.sample");
        Assert.assertEquals(helperSource.folderToPackage("default"), "default");
        Assert.assertEquals(helperSource.folderToPackage(""), "");
    }
}
