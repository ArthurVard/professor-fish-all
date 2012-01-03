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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EmployeePanel extends VerticalPanel {
	
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	
	private TextBox name = new TextBox();
	private TextBox address = new TextBox();
	private TextBox total = new TextBox();
	private ListBox parent = new ListBox(false);
	
	private Button save = new Button("save");
	private Button cut = new Button("cut");

	private Integer employee;

	private TreePanel tree;
	
	public EmployeePanel(TreePanel tree) {
		this.tree = tree;
		
		name.setWidth("300px");
		address.setWidth("300px");
		total.setWidth("300px");
		parent.setHeight("28px");
		
		cut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				employeeService.cut(employee, new AsyncCallback<Double>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(Double result) {
						total.setText(Double.toString(result));
					}
				});
			}
		});
		
		save.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				int parentIndex;
				
				Integer parentDep = null;
				
				parentIndex = parent.getSelectedIndex();
				
				if (parentIndex > 0) {
					parentDep = Integer.parseInt(parent.getValue(parentIndex));
				}
				
				employeeService.saveEmployee(employee, name.getText(), address.getText(), Double.parseDouble(total.getText()), parentDep, new AsyncCallback<EmployeeInfo>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(EmployeeInfo result) {
						initFields(result);
						EmployeePanel.this.tree.refreshTree();
					}
				});
			}
			
		});
		
		Grid grid = new Grid(4, 2);
		
		Label lname = new Label("Name:");
		lname.setWidth("60px");
		
		grid.setWidget(0, 0, lname);
		grid.setWidget(1, 0, new Label("Address:"));
		grid.setWidget(2, 0, new Label("Salary:"));
		grid.setWidget(3, 0, new Label("Parent:"));
		
		grid.setWidget(0, 1, name);
		grid.setWidget(1, 1, address);
		grid.setWidget(2, 1, total);
		grid.setWidget(3, 1, parent);

		add(grid);
		
		HorizontalPanel buttons = new HorizontalPanel();
		
		buttons.setSpacing(5);
		
		buttons.add(save);
		buttons.add(cut);
		
		add(buttons);
	}

	public Integer getEmployee() {
		return employee;
	}

	public void setEmployee(Integer employee) {
		this.employee = employee;
		
		parent.clear();
		
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
		total.setText(Double.toString(result.getTotal()));
		int i = 0;
		int index = i;
		
		parent.addItem(null);
		
		for (Integer key : result.getAllDepartments().keySet()) {
			parent.addItem(result.getAllDepartments().get(key), Integer.toString(key));
			i++;
			if (key.equals(result.getAllDepartments())) {
				index = i;
			}
		}
		parent.setSelectedIndex(index);
	}
	
}
