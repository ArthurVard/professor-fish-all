package org.softlang.client.interfaces;

import org.softlang.client.guiinfos.CompanyInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("company")
public interface CompanyService extends RemoteService {
	
	public double cut(int id);
	
	public CompanyInfo getCompany(int id);
	
	public String saveCompany(int id, String name);
}
