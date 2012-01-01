package org.softlang.client;

import org.softlang.client.guiinfos.TreeInfo;
import org.softlang.client.interfaces.TreeService;
import org.softlang.client.interfaces.TreeServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GwtTree implements EntryPoint {

	private final TreeServiceAsync treeService = GWT.create(TreeService.class);
	
	private TreePanel treePanel;
	private ScrollPanel contentPanel;
	
	private CompanyPanel companyPanel;
	private DepartmentPanel departmentPanel;
	private EmployeePanel employeePanel;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		SplitLayoutPanel mainPanel = new SplitLayoutPanel();
		
		
		
		mainPanel.setStylePrimaryName("main");
		
		treePanel = new TreePanel(this);
		contentPanel = new ScrollPanel();
		
		companyPanel = new CompanyPanel(treePanel);
		departmentPanel = new DepartmentPanel(this);
		employeePanel = new EmployeePanel(this);
		
		treeService.getTree(new AsyncCallback<TreeInfo>() {
			
			@Override
			public void onSuccess(TreeInfo result) {
				treePanel.generateTree(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
		});
		
		ScrollPanel treeScroll = new ScrollPanel(treePanel);
		treeScroll.setStylePrimaryName("tree");
		
		mainPanel.addSouth(new ButtonPanel(), 32);
		mainPanel.addWest(treeScroll, 200);
		mainPanel.add(contentPanel);
		
		RootPanel.get("content").add(mainPanel);
	}

	public void showCompany(Integer companyId) {
		contentPanel.clear();
		companyPanel.setCompany(companyId);
		contentPanel.add(companyPanel);
	}

	public void showDepartment(Integer departmentId) {
		contentPanel.clear();
		departmentPanel.setDepartment(departmentId);
		contentPanel.add(departmentPanel);
	}

	public void showEmployee(Integer employeeId) {
		contentPanel.clear();
		employeePanel.setEmployee(employeeId);
		contentPanel.add(employeePanel);
	}
}
