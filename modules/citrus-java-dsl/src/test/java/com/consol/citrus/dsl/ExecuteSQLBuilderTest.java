package com.consol.citrus.dsl;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ExecuteSQLAction;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;


public class ExecuteSQLBuilderTest {	
	DataSource dataSource = EasyMock.createMock(DataSource.class);
	Resource resource = EasyMock.createMock(Resource.class);
	
	@Test
	public void TestExecuteSQLBuilderWithStatement(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
            protected void configure() {
	            executeSQL(dataSource)
	            	.statements("Test Statement", "Test2 Statement", "Test3 Statement")
	            	.ignoreErrors(false);
            }
		};
		
		builder.configure();
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLAction.class);
        
        ExecuteSQLAction action = (ExecuteSQLAction)builder.getTestCase().getActions().get(0);
        Assert.assertEquals(action.getName(), ExecuteSQLAction.class.getSimpleName());
        Assert.assertEquals(action.getStatements().toString(), "[Test Statement, Test2 Statement, Test3 Statement]");
        Assert.assertEquals(action.isIgnoreErrors(), false);
        Assert.assertEquals(action.getDataSource(), dataSource);
	}
	
	@Test
	public void TestExecuteSQLBuilderWithSQLResource(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder(){
			@Override
			protected void configure(){
				executeSQL(dataSource)
					.sqlResource(resource)
					.ignoreErrors(true);
			}
		};
	
		builder.configure();
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
	    Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLAction.class);

        ExecuteSQLAction action = (ExecuteSQLAction)builder.getTestCase().getActions().get(0);
	    Assert.assertEquals(action.getName(), ExecuteSQLAction.class.getSimpleName());
	    Assert.assertEquals(action.isIgnoreErrors(), true);
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertEquals(action.getSqlResource(), resource);
	}
}
