package org.softlang.client;

import org.softlang.client.guiinfos.CompanyInfo;
import org.softlang.client.interfaces.CompanyService;
import org.softlang.client.interfaces.CompanyServiceAsync;

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

public class CompanyGUI extends Grid {

	private TextBox name = new TextBox();
	private TextBox total = new TextBox();
	
	private ListBox departments = new ListBox(false);
	
	private Button save = new Button("save");
	private Button selectDepartment = new Button("select");
	private Button cut = new Button("cut");
	
	private Integer company;
	
	private final CompanyServiceAsync companyService = GWT.create(CompanyService.class);
	
	private GoogleWebToolkit app;
	
	public CompanyGUI(GoogleWebToolkit app) {
		super(3, 3);
		
		this.app = app;
		
		departments.setVisibleItemCount(5);
		
		total.setReadOnly(true);
		
		cut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				companyService.cut(company, new AsyncCallback<Double>() {
					
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
		
		selectDepartment.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				CompanyGUI.this.app.selectDepartment(Integer.valueOf(departments.getValue(departments.getSelectedIndex())));
			}
			
		});
		
		save.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				CompanyGUI.this.companyService.saveCompany(company, name.getText(), new AsyncCallback<String>() {

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
		
		setWidget(0, 0, new Label("Name"));
		setWidget(1, 0, new Label("Departments"));
		setWidget(2, 0, new Label("Total"));
		
		setWidget(0, 1, name);
		setWidget(1, 1, departments);
		setWidget(2, 1, total);
		
		setWidget(0, 2, save);
		setWidget(1, 2, selectDepartment);
		setWidget(2, 2, cut);
		
		setStylePrimaryName("fields");
	}
	
	public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
		
		companyService.getCompany(company, new AsyncCallback<CompanyInfo>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(CompanyInfo result) {
				initFields(result);
			}
		});
	}
	
	public void initFields(CompanyInfo result) {
		departments.clear();
				
		name.setText(result.getName());
		for (int key : result.getDepartments().keySet()) {
			departments.addItem(result.getDepartments().get(key), Integer.toString(key));
		}
		total.setText(Double.toString(result.getTotal()));
	}
	
	
}
