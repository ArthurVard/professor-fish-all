<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="/struts-tags" prefix="s" %>
<html>
<head>
<title>101companies - all companies</title>
    <style type="text/css">
       <%@ include file="../../tab-style.css" %>
    </style>
    <LINK type="text/css" rel="stylesheet" href="space.css">
    <LINK type="text/css" rel="stylesheet" href="http://cwiki.apache.org/confluence/download/resources/confluence.ext.code:code/shStyles.css">
    <STYLE type="text/css">
      .dp-highlighter {
        width:95% !important;
      }
    </STYLE>
    <STYLE type="text/css">
      .footer {
        background-image:      url('http://cwiki.apache.org/confluence/images/border/border_bottom.gif');
        background-repeat:     repeat-x;
        background-position:   left top;
        padding-top:           4px;
        color:                 #666;
      }
    </STYLE>
</head>
<body>

<h2>All companies</h2>

<s:form action="company">
<table id="hor-minimalist-a" summary="Employee Pay Sheet">
    <thead>
    	<tr>
            <th scope="col">Name</th>
            <th scope="col">Total Salary</th>
	    <th scope="col">Cut salaries</th>
	    <th scope="col">Show details</th>
        </tr>
    </thead>
    <tbody> 
    	    
    	    	    <s:iterator value="allCompanies">
  	    	    <tr>
			<td><s:property value="name"/></td>
    			<td><s:property value="total"/></td>
			<td>
 			  <s:url id="cutURL" action="company.cutSalaries">
  			   <s:param name="id" value="%{id}"/>
 			  </s:url>
 			  <s:a href="%{cutURL}">Cut</s:a>
                        </td>
			<td>
 			  <s:url id="detailURL" action="company.details">
  			   <s:param name="id" value="%{id}"/>
 			  </s:url>
 			  <s:a href="%{detailURL}">Detail</s:a>
                        </td>
  	    	    </tr>
 		    </s:iterator>
    </tbody>	
</table> 
</s:form>
</body>
</html>