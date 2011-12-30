package org.softlang.client;

import org.softlang.client.guiinfos.DepartmentInfo;
import org.softlang.client.interfaces.DepartmentService;
import org.softlang.client.interfaces.DepartmentServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class DepartmentGUI extends Grid {
	
	private TextBox name = new TextBox();
	private TextBox manager = new TextBox();
	private TextBox total = new TextBox();
	
	private ListBox departments = new ListBox(false);
	private ListBox employees = new ListBox(false);
	
	private Button save = new Button("save");
	private Button edit = new Button("edit");
	private Button selectDepartment = new Button("select");
	private Button selectEmployee = new Button("select");
	private Button cut = new Button("cut");
	private Button back = new Button("back");
	
	private Integer department;
	private Integer managerId;
	
	private final DepartmentServiceAsync departmentService = GWT.create(DepartmentService.class);
	
	private GoogleWebToolkit app;
	
	public DepartmentGUI(GoogleWebToolkit app) {
		super(6, 3);
		
		this.app = app;
		
		departments.setVisibleItemCount(5);
		employees.setVisibleItemCount(5);
		
		manager.setReadOnly(true);
		total.setReadOnly(true);
		
		save.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				DepartmentGUI.this.departmentService.saveDepartment(department, name.getText(), new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(String result) {
						name.setText(result);
					}
				});
			}
			
		});
		
		cut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				departmentService.cut(department, new AsyncCallback<Double>() {
					
					@Override
					public void onSuccess(Double result) {
						total.setText(Double.toString(result));
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}
				});
			}
		});
		
		edit.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				DepartmentGUI.this.app.selectEmployee(managerId);
			}
			
		});
		
		selectDepartment.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				DepartmentGUI.this.app.selectDepartmentWithHistory(Integer.valueOf(departments.getValue(departments.getSelectedIndex())));
			}
			
		});
		
		selectEmployee.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				DepartmentGUI.this.app.selectEmployee(Integer.valueOf(employees.getValue(employees.getSelectedIndex())));
			}
			
		});
		
		back.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				DepartmentGUI.this.app.back();
			}
			
		});
		
		setWidget(0, 0, new Label("Name"));
		setWidget(1, 0, new Label("Manager"));
		setWidget(2, 0, new Label("Departments"));
		setWidget(3, 0, new Label("Employees"));
		setWidget(4, 0, new Label("Total"));
		
		setWidget(0, 1, name);
		setWidget(1, 1, manager);
		setWidget(2, 1, departments);
		setWidget(3, 1, employees);
		setWidget(4, 1, total);
		
		setWidget(0, 2, save);
		setWidget(1, 2, edit);
		setWidget(2, 2, selectDepartment);
		setWidget(3, 2, selectEmployee);
		setWidget(4, 2, cut);
		
		setWidget(5, 1, back);
		
		setStylePrimaryName("fields");
	}

	public Integer getDepartment() {
		return department;
	}

	public void setDepartment(Integer department) {
		this.department = department;
		
		departmentService.getDepartment(department, new AsyncCallback<DepartmentInfo>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(DepartmentInfo result) {
				initFields(result);
			}
		});
	}
	
	private void initFields(DepartmentInfo result) {
		departments.clear();
		employees.clear();
		
		name.setText(result.getName());
		manager.setText(result.getManager());
		managerId = result.getManagerId();
		for (int key : result.getDepartments().keySet()) {
			departments.addItem(result.getDepartments().get(key), Integer.toString(key));
		}
		for (int key : result.getEmployees().keySet()) {
			employees.addItem(result.getEmployees().get(key), Integer.toString(key));
		}
		total.setText(Double.toString(result.getTotal()));
	}
	
}
