package com.consol.citrus.admin.spring.model;

import com.consol.citrus.model.testcase.core.TestcaseDefinition;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "imports",
    "beans",
    "testcase"
})
@XmlRootElement(name = "beans")
public class SpringBeans {

    protected Description description;
    
    @XmlElementRef(name = "import", namespace = "http://www.springframework.org/schema/beans", type = Import.class)
    protected List<Import> imports;
    
    @XmlElementRef(name = "bean", namespace = "http://www.springframework.org/schema/beans", type = SpringBean.class)
    protected List<SpringBean> beans;

    @XmlElementRef(name = "testcase", namespace = "http://www.citrusframework.org/schema/testcase", type = TestcaseDefinition.class)
    protected TestcaseDefinition testcase;
    
    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the imports property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the imports property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImportedFiles().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Import }
     * 
     * 
     */
    public List<Import> getImports() {
        if (imports == null) {
            imports = new ArrayList<Import>();
        }
        return this.imports;
    }

    /**
     * Gets the value of the beans property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the beans property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBeans().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SpringBeans }
     * 
     * 
     */
    public List<SpringBean> getBeans() {
        if (beans == null) {
            beans = new ArrayList<SpringBean>();
        }
        return this.beans;
    }

    /**
     * Gets the test case bean.
     * @return
     */
    public TestcaseDefinition getTestcase() {
        return this.testcase;
    }

    /**
     * Sets the test case bean.
     * @param testcase
     */
    public void setTestcase(TestcaseDefinition testcase) {
        this.testcase = testcase;
    }
}
