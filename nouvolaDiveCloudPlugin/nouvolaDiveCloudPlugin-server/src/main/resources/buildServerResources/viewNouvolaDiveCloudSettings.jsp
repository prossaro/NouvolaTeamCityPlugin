<%-- any content can be specified here e.g.: --%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
    Plan ID: <strong><props:displayValue name="planId" emptyValue="not specified" /></strong>
</div>

<div class="parameter">
  API Key: <strong><props:displayValue name="APIKey" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Wait Time: <strong><props:displayValue name="waitTime" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Return URL: <strong><props:displayValue name="returnURL" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Listener Time Out: <strong><props:displayValue name="timeOut" emptyValue="not specified"/></strong>
</div>
