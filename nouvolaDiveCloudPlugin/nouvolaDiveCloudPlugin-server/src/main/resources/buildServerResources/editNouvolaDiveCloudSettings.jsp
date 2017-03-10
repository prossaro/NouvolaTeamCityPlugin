<%@page contentType="text/html" pageEncoding="windows-1252"%>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<l:settingsGroup title="Setup">
    <tr>
        <th><label for="planId">Test Plan ID: </label></th>
        <td><props:textProperty name="planId"/>
            <span class="error" id="error_planId" />
            <span class="smallNote" >Your Nouvola DiveCloud Test Plan ID</span>
        </td>        
    </tr>
    <tr>
        <th><label for="APIKey">API Key: </label></th>
        <td><props:textProperty name="APIKey"/>
            <span class="error" id="error_APIKey"/>
            <span class="smallNote" >Your Nouvola DiveCloud API Key</span>            
        </td>        
    </tr>
    <tr>
        <th><label for="returnURL">Return URL (optional): </label></th>
        <td><props:textProperty name="returnURL"/>
            <span class="error" id="error_returnURL" />
            <span class="smallNote" >If you would like to receive a callback after triggering a test, enter a URL that DiveCloud can send a callback to</span>
        </td>        
    </tr>
    <tr>
        <th><label for="timeOut">Listener Time Out in minutes (optional): </label></th>
        <td><props:textProperty name="timeOut"/>
            <span class="error" id="error_timeOut" />
            <span class="smallNote" >How long to keep the listener open (defaults to 60 minutes)</span>
        </td>        
    </tr>
</l:settingsGroup>
