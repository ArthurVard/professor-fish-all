package org.softlang.client;

import org.softlang.client.guiinfos.EmployeeInfo;
import org.softlang.client.interfaces.EmployeeService;
import org.softlang.client.interfaces.EmployeeServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class EmployeeGUI extends Grid {
	
	private TextBox name = new TextBox();
	private TextBox address = new TextBox();
	private TextBox total = new TextBox();
	
	private Button save = new Button("save");
	private Button cut = new Button("cut");
	private Button back = new Button("back");

	private Integer employee;
	
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);

	private GoogleWebToolkit app;
	
	public EmployeeGUI(GoogleWebToolkit app) {
		super(4, 3);
		
		this.app = app;
		
		cut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				employeeService.cut(employee, new AsyncCallback<Double>() {
					
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
		
		back.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				EmployeeGUI.this.app.back();
			}
			
		});
		
		setWidget(0, 0, new Label("Name"));
		setWidget(1, 0, new Label("Address"));
		setWidget(2, 0, new Label("Salary"));
		
		setWidget(0, 1, name);
		setWidget(1, 1, address);
		setWidget(2, 1, total);
		
		setWidget(0, 2, save);
		setWidget(2, 2, cut);
		
		setWidget(3, 1, back);
		
		setStylePrimaryName("fields");
	}

	public Integer getEmployee() {
		return employee;
	}

	public void setEmployee(Integer employee) {
		this.employee = employee;
		
		employeeService.getEmployee(employee, new AsyncCallback<EmployeeInfo>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(EmployeeInfo result) {
				initFields(result);
			}
		});
	}
	
	private void initFields(EmployeeInfo result) {
		name.setText(result.getName());
		address.setText(result.getAddress());
		total.setText(Double.toString(result.getSalary()));
	}
	
}
