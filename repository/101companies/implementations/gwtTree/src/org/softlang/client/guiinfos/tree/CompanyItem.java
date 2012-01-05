package org.softlang.client.guiinfos.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompanyItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1382222600294176621L;

	private String name;
	private Integer id;
	private List<DepartmentItem> departments;
	private boolean fault;
	private String faultMessage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<DepartmentItem> getDepartments() {
		return departments;
	}

	public void setDepartments(List<DepartmentItem> departments) {
		this.departments = departments;
	}

	public boolean isFault() {
		return fault;
	}

	public void setFault(boolean fault) {
		this.fault = fault;
	}

	public String getFaultMessage() {
		return faultMessage;
	}

	public void setFaultMessage(String faultMessage) {
		this.faultMessage = faultMessage;
	}
	
	
}
