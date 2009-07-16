package com.consol.citrus.script

import com.consol.citrus.*
import com.consol.citrus.actions.*
import com.consol.citrus.script.GroovyTestCaseParser.TestCaseBuilder
import com.consol.citrus.script.GroovyTestActionBuilder

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import java.text.SimpleDateFormat
import java.util.*

public class TestCaseBuilderImpl implements TestCaseBuilder {
    TestCase testCase
    TestCaseMetaInfo testMetaInfo
    
    ApplicationContext ctx
    
    LinkedHashMap variables = [:]
    LinkedHashMap binding = [:]
    
    TestCase build(ApplicationContext applicationContext) {
        testMetaInfo = new TestCaseMetaInfo()
        testCase = new TestCase(metaInfo: testMetaInfo)
        testCase.testVariables = variables
        ctx = applicationContext

        def test_case = { Map args -> 
            name(args.name)
            author(args.author)
            status(args.status)
            creation_date(args.creation_date)
            last_update_by(args.last_update_by)
            last_update_on(args.last_update_on)
            description(args.description)
        }
        
        def name = { name -> testCase.setName(name) }
        def author = { author -> testMetaInfo.author = author }
        def creation_date = { date -> testMetaInfo.creationDate = new SimpleDateFormat("dd.MM.yyyy").parse(date) }
        def status = { status -> testMetaInfo.status = status }
        def last_update_by = { updatedBy -> testMetaInfo.lastUpdatedBy = updatedBy }
        def last_update_on = { datetime -> testMetaInfo.lastUpdatedOn = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(datetime) }
        def description = { description -> testCase.description = description.trim() }

        def echo = { message -> testCase.testChain.add(new EchoBean(message: message)) }
        def sleep = { delay -> testCase.testChain.add(new DelayBean(delay: delay)) }
        
        GroovyTestActionBuilder.metaClass.methodMissing = { String methodName, args ->
            LinkedHashMap properties = [:]
            
            args.each { entry -> properties += entry }
        
            TestAction action = build(methodName, properties)
            testCase.testChain.add(action)
        }
        
        GroovyTestActionBuilder test_suite = new GroovyTestActionBuilder(applicationContext: ctx)
        
        "+++++ BODY +++++"
        
        /* convert variable values to String */
        variables.each { it.value = it.value.toString() }
        
        return testCase
    }

    def methodMissing (String name, args) {
        testCase.testVariables.put("\${" + name + "}", args[0].toString())
    }

    def propertyMissing (String propertyName) { 
        if(binding.containsKey(propertyName)) {
            binding[propertyName]
        } else {
            "\${" + propertyName + "}"
        }
    }
    
    def propertyMissing (String propertyName, args) {
        binding.put(propertyName, args.toString())
    }
}