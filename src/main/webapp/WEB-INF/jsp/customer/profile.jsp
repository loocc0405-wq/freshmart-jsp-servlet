<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Profile - FreshMart</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f6f8fa; }
        .container { max-width: 900px; margin: 30px auto; background: #fff; padding: 24px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
        h1 { margin-top: 0; }
        .msg-success { color: #0a7a33; margin-bottom: 12px; }
        .msg-error { color: #c62828; margin-bottom: 12px; }
        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
        .field { display: flex; flex-direction: column; margin-bottom: 14px; }
        label { font-weight: bold; margin-bottom: 6px; }
        input, select, textarea { padding: 10px; border: 1px solid #ccc; border-radius: 6px; font-size: 14px; }
        textarea { min-height: 100px; resize: vertical; }
        .actions { margin-top: 16px; }
        button { background: #2e7d32; color: #fff; border: none; padding: 10px 18px; border-radius: 6px; cursor: pointer; }
        button:hover { background: #256628; }
        .full { grid-column: 1 / -1; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container">
    <h1>My Profile</h1>

    <c:if test="${not empty successMessage}">
        <div class="msg-success">${successMessage}</div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="msg-error">${errorMessage}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/customer/profile">
        <div class="grid">
            <div class="field">
                <label>Username</label>
                <input type="text" value="${profileUser.username}" readonly />
            </div>

            <div class="field">
                <label>Email</label>
                <input type="email" value="${profileUser.email}" readonly />
            </div>

            <div class="field">
                <label>Full name</label>
                <input type="text" name="fullName" value="${profileUser.fullName}" />
            </div>

            <div class="field">
                <label>Gender</label>
                <select name="gender">
                    <option value="">-- Select gender --</option>
                    <option value="MALE" ${profileUser.gender != null && profileUser.gender.name() == 'MALE' ? 'selected' : ''}>Male</option>
                    <option value="FEMALE" ${profileUser.gender != null && profileUser.gender.name() == 'FEMALE' ? 'selected' : ''}>Female</option>
                    <option value="OTHER" ${profileUser.gender != null && profileUser.gender.name() == 'OTHER' ? 'selected' : ''}>Other</option>
                </select>
            </div>

            <div class="field">
                <label>Date of birth</label>
                <input type="date" name="dob" value="${profileUser.dob}" />
            </div>

            <div class="field">
                <label>Phone</label>
                <input type="text" name="phone" value="${profileUser.phone}" />
            </div>

            <div class="field full">
                <label>Address</label>
                <textarea name="address">${profileUser.address}</textarea>
            </div>
        </div>

        <div class="actions">
            <button type="submit">Save profile</button>
        </div>
    </form>
</div>
</body>
</html>