//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.03.19 at 11:11:08 PM BRT 
//


package org.softlang.company;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="employee" type="{http://www.softlang.org/company.xsd}employee"/>
 *         &lt;element name="subdept" type="{http://www.softlang.org/company.xsd}dept"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "employee",
    "subdept"
})
@XmlRootElement(name = "subunit")
public class Subunit {

    protected Employee employee;
    protected Dept subdept;

    /**
     * Gets the value of the employee property.
     * 
     * @return
     *     possible object is
     *     {@link Employee }
     *     
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * Sets the value of the employee property.
     * 
     * @param value
     *     allowed object is
     *     {@link Employee }
     *     
     */
    public void setEmployee(Employee value) {
        this.employee = value;
    }

    /**
     * Gets the value of the subdept property.
     * 
     * @return
     *     possible object is
     *     {@link Dept }
     *     
     */
    public Dept getSubdept() {
        return subdept;
    }

    /**
     * Sets the value of the subdept property.
     * 
     * @param value
     *     allowed object is
     *     {@link Dept }
     *     
     */
    public void setSubdept(Dept value) {
        this.subdept = value;
    }

}
