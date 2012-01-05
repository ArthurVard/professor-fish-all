package org.softlang.client.guiinfos.tree;

import java.io.Serializable;

public class EmployeeItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1237062883990308823L;
	
	private String name;
	private Integer id;
	private boolean manager;
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

	public void setManager(boolean manager) {
		this.manager = manager;
	}
	
	public boolean isManager() {
		return this.manager;
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
