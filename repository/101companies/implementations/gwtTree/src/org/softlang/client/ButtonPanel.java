package org.softlang.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;

public class ButtonPanel extends Grid {

	public ButtonPanel() {
		super(1, 2);
		setWidget(0, 0, new Button("create Department"));
		setWidget(0, 1, new Button("create Employee"));
	}
}
