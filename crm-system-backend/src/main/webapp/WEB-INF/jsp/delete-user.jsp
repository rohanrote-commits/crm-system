<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Delete User</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/delete-user.css">
    <script src="${pageContext.request.contextPath}/static/javascript/jquery.js"></script>
    <script src="${pageContext.request.contextPath}/static/javascript/jquery.validate.min.js"></script>
</head>
<body>

<header class="top-bar">
    <div class="logo">CRM <span>Lead Management</span></div>
</header>

<div class="form-container">
    <h2>Delete User</h2>

    <form id="deleteUserForm">

        <div class="form-group">
            <label>Email <span class="required">*</span></label>
            <input type="email" id="email" name="email" placeholder="Enter user email" required>
        </div>

        <button type="submit" class="btn-submit">Delete User</button>

    </form>
</div>

<script src="${pageContext.request.contextPath}/static/javascript/delete-user.js"></script>

</body>
</html>
