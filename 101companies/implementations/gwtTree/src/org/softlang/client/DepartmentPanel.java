package org.softlang.client;

import org.softlang.client.guiinfos.DepartmentInfo;
import org.softlang.client.interfaces.DepartmentService;
import org.softlang.client.interfaces.DepartmentServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DepartmentPanel extends VerticalPanel {
	
	private final DepartmentServiceAsync  departmentService = GWT.create(DepartmentService.class);
	
	private TextBox name = new TextBox();
	private TextBox total = new TextBox();	
	private ListBox manager = new ListBox(false);
	private ListBox parent = new ListBox(false);
	
	private Button save = new Button("save");
	private Button cut = new Button("cut");
	
	private Integer department;	
	
	private GwtTree app;
	
	public DepartmentPanel(GwtTree app) {
		this.app = app;
		
		total.setReadOnly(true);
		name.setWidth("300px");
		total.setWidth("300px");
		manager.setHeight("28px");
		parent.setHeight("28px");
		
		Grid grid = new Grid(4, 2);
		
		Label lname = new Label("Name:");
		lname.setWidth("60px");
		
		grid.setWidget(0, 0, lname);
		grid.setWidget(1, 0, new Label("Total:"));
		grid.setWidget(2, 0, new Label("Manager:"));
		grid.setWidget(3, 0, new Label("Parent:"));
		
		grid.setWidget(0, 1, name);
		grid.setWidget(1, 1, total);
		grid.setWidget(2, 1, manager);
		grid.setWidget(3, 1, parent);

		add(grid);
		
		HorizontalPanel buttons = new HorizontalPanel();
		
		buttons.setSpacing(5);
		
		buttons.add(save);
		buttons.add(cut);
		
		add(buttons);
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
				name.setText(result.getName());
				total.setText(Double.toString(result.getTotal()));
				int i = 0;
				for (Integer key : result.getDepartments().keySet()) {
					parent.addItem(result.getDepartments().get(key), Integer.toString(key));
					if (key == result.getParent()) {
						parent.setSelectedIndex(i);
					}
					i++;
				}
				i = 0;
				for (Integer key: result.getEmployees().keySet()) {
					manager.addItem(result.getEmployees().get(key), Integer.toString(key));
					if (key == result.getManager()) {
						manager.setSelectedIndex(i);
					}
					i++;
				}
			}
		});
	}
	
}
