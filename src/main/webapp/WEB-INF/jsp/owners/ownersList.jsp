<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="owners">
    <h2 id="owners">Owners</h2>

    <div class="row">
        <div class="col-sm-6">
            <p class="lead" id="ownersTotal">Total owners found: ${totalCount}</p>
        </div>
        <div class="col-sm-6 text-right">
            <spring:url value="/owners" var="sizeFormUrl"/>
            <form class="form-inline" method="get" action="${fn:escapeXml(sizeFormUrl)}">
                <input type="hidden" name="lastName" value="${fn:escapeXml(searchLastName)}"/>
                <input type="hidden" name="direction" value="next"/>
                <label class="control-label" for="pageSizeSelect">Results per page</label>
                <select class="form-control" id="pageSizeSelect" name="size" onchange="this.form.submit()">
                    <c:forEach var="option" items="${pageSizeOptions}">
                        <option value="${option}" <c:if test="${option == pageSize}">selected</c:if>>${option}</option>
                    </c:forEach>
                </select>
            </form>
        </div>
    </div>

    <table id="ownersTable" class="table table-striped" aria-describedby="owners">
        <thead>
        <tr>
            <th scope="col" style="width: 150px;">Name</th>
            <th scope="col" style="width: 200px;">Address</th>
            <th scope="col">City</th>
            <th scope="col" style="width: 120px">Telephone</th>
            <th scope="col">Pets</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${selections}" var="owner">
            <tr>
                <td>
                    <spring:url value="/owners/{ownerId}" var="ownerUrl">
                        <spring:param name="ownerId" value="${owner.id}"/>
                    </spring:url>
                    <a href="${fn:escapeXml(ownerUrl)}"><c:out value="${owner.firstName} ${owner.lastName}"/></a>
                </td>
                <td>
                    <c:out value="${owner.address}"/>
                </td>
                <td>
                    <c:out value="${owner.city}"/>
                </td>
                <td>
                    <c:out value="${owner.telephone}"/>
                </td>
                <td>
                    <c:forEach var="pet" items="${owner.pets}">
                        <c:out value="${pet.name} "/>
                    </c:forEach>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${hasPrevious or hasNext}">
        <nav aria-label="Owner search pages">
            <ul class="pagination justify-content-center">
                <li class="page-item${hasPrevious ? '' : ' disabled'}">
                    <spring:url value="/owners" var="prevUrl">
                        <spring:param name="lastName" value="${searchLastName}"/>
                        <spring:param name="size" value="${pageSize}"/>
                        <spring:param name="direction" value="previous"/>
                        <c:if test="${hasPrevious}">
                            <spring:param name="cursor" value="${previousCursor}"/>
                        </c:if>
                    </spring:url>
                    <a class="page-link" href="${fn:escapeXml(prevUrl)}" aria-label="Previous" <c:if test='${!hasPrevious}'>tabindex="-1" aria-disabled="true"</c:if>>
                        <span aria-hidden="true">&laquo;</span>
                    </a>
                </li>
                <li class="page-item${hasNext ? '' : ' disabled'}">
                    <spring:url value="/owners" var="nextUrl">
                        <spring:param name="lastName" value="${searchLastName}"/>
                        <spring:param name="size" value="${pageSize}"/>
                        <spring:param name="direction" value="next"/>
                        <c:if test="${hasNext}">
                            <spring:param name="cursor" value="${nextCursor}"/>
                        </c:if>
                    </spring:url>
                    <a class="page-link" href="${fn:escapeXml(nextUrl)}" aria-label="Next" <c:if test='${!hasNext}'>tabindex="-1" aria-disabled="true"</c:if>>
                        <span aria-hidden="true">&raquo;</span>
                    </a>
                </li>
            </ul>
        </nav>
    </c:if>
</petclinic:layout>
