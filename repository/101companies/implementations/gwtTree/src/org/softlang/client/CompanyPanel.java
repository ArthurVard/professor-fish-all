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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CompanyPanel extends VerticalPanel {

	private TextBox name = new TextBox();
	private TextBox total = new TextBox();
	
	private Button save = new Button("save");
	private Button cut = new Button("cut");
	
	private Integer company;
	
	private TreePanel tree;
	
	private final CompanyServiceAsync companyService = GWT.create(CompanyService.class);
	
	public CompanyPanel(TreePanel tree) {
		this.tree = tree;
		
		total.setReadOnly(true);
		name.setWidth("300px");
		total.setWidth("300px");
		
		cut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				companyService.cut(company, new AsyncCallback<Double>() {

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
				companyService.saveCompany(company, name.getText(), new AsyncCallback<String>() {

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
		
		Grid grid = new Grid(2, 2);
		
		Label lname = new Label("Name:");
		lname.setWidth("60px");
		
		grid.setWidget(0, 0, lname);
		grid.setWidget(1, 0, new Label("Total:"));
		
		grid.setWidget(0, 1, name);
		grid.setWidget(1, 1, total);

		add(grid);
		
		HorizontalPanel buttons = new HorizontalPanel();
		
		buttons.setSpacing(5);
		
		buttons.add(save);
		buttons.add(cut);
		
		add(buttons);
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
				name.setText(result.getName());
				total.setText(Double.toString(result.getTotal()));
			}
		});
	}	
	
}
