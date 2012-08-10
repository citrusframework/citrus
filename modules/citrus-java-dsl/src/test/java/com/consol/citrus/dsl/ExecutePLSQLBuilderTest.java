package com.consol.citrus.dsl;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ExecutePLSQLAction;

public class ExecutePLSQLBuilderTest {
	DataSource dataSource = EasyMock.createMock(DataSource.class);
	Resource resource = EasyMock.createMock(Resource.class);
	
	@Test
	public void testExecutePLSQLBuilderWithStatement(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	executePLSQL(dataSource)
            		.ignoreErrors(true)
            		.script("testScript")
            		.statements("Test Statement", "Test2 Statement", "Test3 Statement");
            }
          };
          
          builder.configure();
          
          Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
          Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecutePLSQLAction.class);
          
          ExecutePLSQLAction action = (ExecutePLSQLAction)builder.getTestCase().getActions().get(0);
          Assert.assertEquals(action.getName(), ExecutePLSQLAction.class.getSimpleName());
          Assert.assertEquals(action.getStatements().toString(), "[Test Statement, Test2 Statement, Test3 Statement]");
          Assert.assertEquals(action.getScript(), "testScript");
          Assert.assertEquals(action.isIgnoreErrors(), true);
          Assert.assertEquals(action.getDataSource(), dataSource);
	}
	
	@Test
	public void testExecutePLSQLBuilderWithSQLResource(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	executePLSQL(dataSource)
            		.ignoreErrors(false)
            		.script("testScript2")
            		.sqlResource(resource);
            }
          };
          
          builder.configure();
          
          Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
          Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecutePLSQLAction.class);

          ExecutePLSQLAction action = (ExecutePLSQLAction)builder.getTestCase().getActions().get(0);
          Assert.assertEquals(action.getName(), ExecutePLSQLAction.class.getSimpleName());
          Assert.assertEquals(action.getScript(), "testScript2");
          Assert.assertEquals(action.isIgnoreErrors(), false);
          Assert.assertEquals(action.getDataSource(), dataSource);
          Assert.assertEquals(action.getSqlResource(), resource);
	}
	
}
