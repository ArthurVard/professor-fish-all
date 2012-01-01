package org.softlang.client;

import java.util.List;

import org.softlang.client.guiinfos.TreeInfo;
import org.softlang.client.guiinfos.tree.CompanyItem;
import org.softlang.client.guiinfos.tree.DepartmentItem;
import org.softlang.client.guiinfos.tree.EmployeeItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class TreePanel extends Tree {
	
	private GwtTree main;
	
	public TreePanel(GwtTree main) {
		this.main = main;
	}
	
	public void generateTree(TreeInfo info) {
		clear();
		for (CompanyItem item : info.getCompanies()) {
			Button company = new Button(item.getName());
			company.setStylePrimaryName("companyButton");
			company.addClickHandler(new CompanyHandler(item.getId()));
			TreeItem root = new TreeItem(company);
			appendDepsAndEmps(root, item.getDepartments());
			addItem(root);
		}
	}

	private void appendDepsAndEmps(TreeItem root, List<DepartmentItem> departments) {
		for (DepartmentItem dItem : departments) {
			Button department = new Button(dItem.getName());
			department.addClickHandler(new DepartmentHandler(dItem.getId()));
			department.setStylePrimaryName("departmentButton");
			TreeItem treeDItem = new TreeItem(department);
			root.addItem(treeDItem);
			for (EmployeeItem eItem : dItem.getEmployees()) {
				Button employee = new Button(eItem.getName());
				employee.addClickHandler(new EmployeeHandler(eItem.getId()));
				employee.setStylePrimaryName("employeeButton");
				TreeItem treeEItem = new TreeItem(employee);
				treeDItem.addItem(treeEItem);
			}
			appendDepsAndEmps(treeDItem, dItem.getDepartments());
		}
	}
	
	private class CompanyHandler implements ClickHandler {

		private Integer companyId;
		
		public CompanyHandler(Integer companyId) {
			this.companyId = companyId;
		}
		
		@Override
		public void onClick(ClickEvent event) {
			main.showCompany(companyId);
		}
	}
	
	private class DepartmentHandler implements ClickHandler {

		private Integer departmentId;
		
		public DepartmentHandler(Integer departmentId) {
			this.departmentId = departmentId;
		}
		
		@Override
		public void onClick(ClickEvent event) {
			main.showDepartment(departmentId);
		}
		
	}
	
	private class EmployeeHandler implements ClickHandler {

		private Integer employeeId;
		
		public EmployeeHandler(Integer employeeId) {
			this.employeeId = employeeId;
		}
		
		@Override
		public void onClick(ClickEvent event) {
			main.showEmployee(employeeId);
		}
		
	}
}
