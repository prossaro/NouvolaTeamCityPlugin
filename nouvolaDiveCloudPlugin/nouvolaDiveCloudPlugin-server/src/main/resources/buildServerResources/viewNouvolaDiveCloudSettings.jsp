<%-- any content can be specified here e.g.: --%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
    Load test authentication token: <strong><props:displayValue name="planId" emptyValue="not specified" /></strong>
</div>

<div class="parameter">
  Load test preset name: <strong><props:displayValue name="APIKey" emptyValue="not specified"/></strong>
</div>
