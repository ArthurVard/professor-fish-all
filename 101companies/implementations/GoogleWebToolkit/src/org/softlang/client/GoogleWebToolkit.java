package org.softlang.client;

import java.util.Stack;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GoogleWebToolkit implements EntryPoint {

	private Label headline = new Label();
		
	private CompanyGUI companyGUI = new CompanyGUI(this);
	private DepartmentGUI departmentGUI = new DepartmentGUI(this);
	private EmployeeGUI employeeGUI = new EmployeeGUI(this);
	
	private Stack<Integer> history;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		history = new Stack<Integer>();
		
		headline.setStylePrimaryName("text-headline");
		
		selectCompany(1);
	}
	
	public void back() {
		if (history.size() > 0) {
			selectDepartment(history.pop());
		} else {
			selectCompany(companyGUI.getCompany());
		}
	}
	
	private void initSpecificGUI(String head, Grid grid) {
		headline.setText(head);
		
		VerticalPanel panel = new VerticalPanel();
		
		panel.setStylePrimaryName("content");
		
		panel.add(headline);
		panel.add(grid);
		
		RootPanel.get("content").clear();
		RootPanel.get("content").add(panel);
	}
	
	public void selectCompany(int id) {
		companyGUI.setCompany(id);
		
		initSpecificGUI("CompanyView", companyGUI);
	}
	
	public void selectDepartment(int id) {
		departmentGUI.setDepartment(id);
		
		initSpecificGUI("Department View", departmentGUI);
	}
	
	public void selectDepartmentWithHistory(int id) {
		if (departmentGUI.getDepartment() != null) {
			history.push(departmentGUI.getDepartment());
		}
		
		selectDepartment(id);
	}
	
	public void selectEmployee(int id) {
		history.push(departmentGUI.getDepartment());
		
		employeeGUI.setEmployee(id);
		
		initSpecificGUI("Employee View", employeeGUI);
	}
}