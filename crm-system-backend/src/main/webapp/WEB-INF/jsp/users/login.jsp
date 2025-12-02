<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Login | CRM Lead Management</title>
  <!-- <link rel="stylesheet" href="/static/css/login.css"> -->

  <!-- Bootstrap -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/login.css">

  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

  <!-- jQuery + jQuery Validate CDN -->
  <script src="${pageContext.request.contextPath}/static/javascript/jquery.js"></script>
  <script src="${pageContext.request.contextPath}/static/javascript/jquery.validate.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>


</head>

<body>

  <header class="top-bar">
    <div class="logo">CRM <span>Lead Management</span></div>
    <div class="contact">
      <a href="mailto:sales@perennialsys.com">sales@perennialsys.com</a>
      <span>|</span>
      <a href="tel:+918007700800">+91 705 7058 631</a>
    </div>
  </header>

<div id="alert-container"></div>

  <div class="login-container">

    <div class="login-box">

      <h2>Login</h2>

      <form id="loginForm">

        <label>Email Address<span class="required">*</span></label>
        <input type="email" id="loginEmail" name="loginEmail" placeholder="Enter your email">

        <label>Password<span class="required">*</span></label>
        <div class="password-wrapper">
          <input type="password" id="loginPassword" name="loginPassword" placeholder="Enter your password">
          <button type="button" class="pw-toggle" aria-label="Show password" aria-pressed="false">
            <!-- Eye icon SVG -->
            <svg class="eye-icon" viewBox="0 0 24 24" width="20" height="20" xmlns="http://www.w3.org/2000/svg"
              aria-hidden="true">
              <path
                d="M12 5C7 5 2.73 8.11 1 12c1.73 3.89 6 7 11 7s9.27-3.11 11-7c-1.73-3.89-6-7-11-7zm0 12a5 5 0 1 1 0-10 5 5 0 0 1 0 10z" />
              <circle class="pupil" cx="12" cy="12" r="2.5" />
            </svg>
          </button>
        </div>


        <button type="submit" class="btn-orange">Continue</button>
      </form>

      <a class="forgot-link" href="${pageContext.request.contextPath}/crm/reset-password">I forgot my password</a>

      <hr>
      <p>Dont have an account? Signup as Master Admin</p>
        <button class="btn-blue" onclick="window.location.href='/crm/sign-up'">Register</button>



    </div>
  </div>

  <script src="${pageContext.request.contextPath}/static/javascript/login.js?v=2"></script>
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</body>

</html>