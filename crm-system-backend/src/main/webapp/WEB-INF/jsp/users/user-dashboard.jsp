<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>User Dashboard | CRM Lead Management</title>

  <!-- Bootstrap -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

  <!-- Custom CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/user-dashboard.css">

  <!-- jQuery & Plugins -->
  <script src="${pageContext.request.contextPath}/static/javascript/jquery.js"></script>
  <script src="${pageContext.request.contextPath}/static/javascript/jquery.validate.min.js"></script>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/javascript/datatables.min.css" />
  <script src="${pageContext.request.contextPath}/static/javascript/datatables.min.js"></script>
</head>

<body>

  <!-- Header -->
  <header class="top-bar">
    <div class="logo">CRM <span>Lead Management</span></div>
    <div class="nav-right">
      <div class="profile-menu">
        <i class="bi bi-gear profile-pic settings-icon" id="profilePic"></i>
        <div class="dropdown" id="profileDropdown">
          <button id="back" class="drop-item">Back</button>
          <button id="delete-profile" class="drop-item">Delete Profile</button>
          <button id="logout" class="drop-item">Logout</button>

        </div>
        
      </div>
       <img src="${pageContext.request.contextPath}/static/assests/profile.png" class="profile-pic" id="view-profile"></div>
    </div>
  </header>
  <!-- Alert container -->
  <div id="alert-container" class="mt-3"></div>



  <!-- Main -->
  <main class="dashboard-container">
    <!-- Users Section -->
    <div class="top-container">
      <div class="top-section">
        <h3>Users</h3>
        <div class="section-buttons">
          <button class="btn-section" id="addUserBtn">Add User</button>
          <div class="dropdown" id="userDropdown">
            <button id="addUser" class="drop-item">Add User</button>
            <button id="importUser" class="drop-item">Bulk Import</button>
          </div>
        </div>
      </div>
    </div>
    <div class="dashboard-section" id="users">

      <div class="user-table-container">
        <table id="user-table" class="table table-bordered table-hover " style="width:100%; margin: 20px auto;">
          <thead>
            <tr>
              <th>Sr.No</th>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Email</th>
              <th>Mobile</th>
              <th>Role</th>
              <th>Registered By</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr>
            </tr>
          </tbody>
        </table>
      </div>

  </main>
  

  <!-- Add User Modal -->
  <div class="modal fade" id="userModal" tabindex="-1" aria-labelledby="userModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header  text-white">
          <h5 class="modal-title" id="userModalLabel">Add User</h5>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>


        <div class="modal-body">
          <form id="userForm">
            <input type="hidden" id="userId" name="userId">

            <div class="row g-3">
              <div class="col-md-6">
                <label for="userFirstName" class="form-label">First Name <span class="required">*</span></label>
                <input type="text" id="userFirstName"placeholder="Enter First Name" name="firstName" class="form-control" required>
              </div>
              <div class="col-md-6">
                <label for="userLastName" class="form-label">Last Name<span class="required">*</span></label>
                <input type="text" id="userLastName" placeholder= "Enter Last Name" name="lastName" class="form-control">
              </div>
              <div class="col-md-6">
                <label for="userEmail" class="form-label">Email<span class="required">*</span></label>
                <input type="email" id="userEmail" placeholder="Enter email" name="email" class="form-control" required>
              </div>
              <div class="col-md-6">
                <label for="userMobileNumber" class="form-label">Mobile<span class="required">*</span></label>
                <input type="text" id="userMobileNumber" placeholder="Enter Mobile Number" name="mobileNumber" class="form-control" required>
              </div>
              <div class="col-md-6">
                <label for="userPassword" class="form-label">Password<span class="required">*</span></label>

                <div class="password-wrapper">
                  <input type="password" id="userPassword" placeholder="Enter Password" name="password" class="form-control" required>

                  <button type="button" class="pw-toggle" aria-label="Show password" aria-pressed="false">
                    <svg class="eye-icon" viewBox="0 0 24 24" width="20" height="20" xmlns="http://www.w3.org/2000/svg">
                      <path
                        d="M12 5C7 5 2.73 8.11 1 12c1.73 3.89 6 7 11 7s9.27-3.11 11-7c-1.73-3.89-6-7-11-7zm0 12a5 5 0 1 1 0-10 5 5 0 0 1 0 10z" />
                      <circle class="pupil" cx="12" cy="12" r="2.5" />
                    </svg>
                  </button>
                </div>
              </div>

              <div class="col-md-6">
                <label for="userConfirmPassword" class="form-label">Confirm Password<span class="required">*</span></label>

                <div class="password-wrapper">
                  <input type="password" id="userConfirmPassword" placeholder="Confirm the Password" name="confirmPassword" class="form-control" required>

                  <button type="button" class="pw-toggle" aria-label="Show password" aria-pressed="false">
                    <svg class="eye-icon" viewBox="0 0 24 24" width="20" height="20" xmlns="http://www.w3.org/2000/svg">
                      <path
                        d="M12 5C7 5 2.73 8.11 1 12c1.73 3.89 6 7 11 7s9.27-3.11 11-7c-1.73-3.89-6-7-11-7zm0 12a5 5 0 1 1 0-10 5 5 0 0 1 0 10z" />
                      <circle class="pupil" cx="12" cy="12" r="2.5" />
                    </svg>
                  </button>
                </div>
              </div>

              <div class="col-12">
                <label for="userAddress" class="form-label">Address</label>
                <input type="text" id="userAddress" placeholder="Enter Address" name="address" class="form-control">
              </div>

              <div id="addressFields" class="row g-3 d-none">
                <div class="col-md-6">
                  <label for="userCity" class="form-label">City<span class="required">*</span></label>
                  <input type="text" id="userCity"placeholder="Enter City" name="city" class="form-control">
                </div>
                <div class="col-md-6">
                  <label for="userState" class="form-label">State<span class="required">*</span></label>
                  <select id="userState" name="state" class="form-select">
                    <option value="">Select State</option>
                    <option value="Maharashtra">Maharashtra</option>
                    <option value="Gujarat">Gujarat</option>
                    <option value="Delhi">Delhi</option>
                  </select>
                </div>
                <div class="col-md-6">
                  <label for="userCountry" class="form-label">Country<span class="required">*</span></label>
                  <select id="userCountry" name="country" class="form-select">
                    <option value="">Select Country</option>
                    <option value="India">India</option>
                  </select>
                </div>
                <div class="col-md-6">
                  <label for="userPinCode" class="form-label">Pin Code<span class="required">*</span></label>
                  <input type="text" placeholder="Enter Pin Code" id="userPinCode" name="pinCode" class="form-control">
                </div>
              </div>

              <div class="col-md-6">
                <label for="userRole" class="form-label">Role<span class="required">*</span></label>
                <select id="userRole" name="role" class="form-select" required>
                  <option value="">Select Role</option>
                </select>
              </div>
            </div>

            <!-- Modal footer must be inside form for submit button to work -->
            <div class="modal-footer">
              <button type="button" class="btn btn-orange" data-bs-dismiss="modal">Cancel</button>
              <button type="button" id="clearUserBtn" class="btn btn-clear text-white">Clear</button>
              <button type="submit" id="saveUserBtn" class="btn btn-green text-white">Save User</button>
            </div>
          </form>
        </div>

      </div>
    </div>
  </div>





  <!-- Import Users Modal -->
  <div class="modal fade" id="importUsersModal" tabindex="-1" aria-labelledby="importUsersLabel" aria-hidden="true">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="importUsersLabel">Import Users from Excel</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <div class="modal-body">
          <form id="importUsersForm" enctype="multipart/form-data">
            <div class="mb-3">
              <label for="userFile" class="form-label">Upload Excel File (.xlsx / .xls)</label>
              <input type="file" class="form-control" id="userFile" name="file" accept=".xlsx,.xls" required />
            </div>
            <div class="text-center">
              <button type="submit" class="btn btn-success">Upload & Import</button>
            </div>
          </form>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
        </div>
      </div>
    </div>

  </div>


<div class="modal fade" id="profileModal" tabindex="-1" aria-labelledby="profileModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-centered">
    <div class="modal-content">

      <form id="profileForm">

        <div class="modal-header text-white">
          <h5 class="modal-title" id="profileModalLabel">User Profile</h5>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <div class="modal-body">
          <div class="row g-3">

            <div class="col-md-6">
              <label class="form-label fw-bold">Name</label>
              <input type="text" class="form-control" id="profileName" name="profileName" readonly>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold">Mobile</label>
              <input type="text" class="form-control" id="profileMobile" name="profileMobile" readonly>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold">Email</label>
              <input type="email" class="form-control" id="profileEmail" name="profileEmail" readonly>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold">Role</label>
              <input type="text" class="form-control" id="profileRole" name="profileRole" readonly>
            </div>

            <div class="col-12">
              <label class="form-label fw-bold">Address</label>
              <textarea class="form-control" id="profileAddress" name="profileAddress" readonly></textarea>
            </div>

            <div class="col-md-4">
              <label class="form-label fw-bold">City</label>
              <input type="text" class="form-control" id="profileCity" name="profileCity" readonly>
            </div>

            <div class="col-md-4">
              <label class="form-label fw-bold">State</label>
              <input type="text" class="form-control" id="profileState" name="profileState" readonly>
            </div>

            <div class="col-md-4">
              <label class="form-label fw-bold">Country</label>
              <input type="text" class="form-control" id="profileCountry" name="profileCountry" readonly>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold">Pin Code</label>
              <input type="text" class="form-control" id="profilePin" name="profilePin" readonly>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-bold">Registered On</label>
              <input type="text" class="form-control" id="profileDate" readonly>
            </div>

          </div>
        </div>

        <div class="modal-footer">
          <button type="button" id="editProfileBtn" class="btn btn-primary">Edit</button>
          <button type="button" id="saveProfileBtn" class="btn btn-success d-none">Save</button>
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
        </div>

      </form>

    </div>
  </div>
</div>
<!-- Update Confirmation Modal -->
<div class="modal fade" id="updateConfirmModal" tabindex="-1">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0">
      <div class="modal-header bg-primary text-white">
        <h5 class="modal-title">Confirm Update</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>

      <div class="modal-body">
        Are you sure you want to update this record?
      </div>

      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <button type="button" id="confirmUpdateBtn" class="btn btn-primary">Yes, Update</button>
      </div>
    </div>
  </div>
</div>

<!-- Delete Confirmation Modal -->
<div class="modal fade" id="deleteConfirmModal" tabindex="-1">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content border-0">

      <div class="modal-header bg-danger text-white">
        <h5 class="modal-title">Confirm Delete</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
      </div>

      <div class="modal-body">
        Are you sure you want to delete this lead? This action cannot be undone.
      </div>

      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <button type="button" id="confirmDeleteBtn" class="btn btn-danger">Yes, Delete</button>
      </div>

    </div>
  </div>
</div>

  <!-- Bootstrap JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>
   <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>


  <!-- Custom JS -->
  <script src="${pageContext.request.contextPath}/static/javascript/users/add_users.js"></script>
  <script src="${pageContext.request.contextPath}/static/javascript/users/upload-users.js"></script>
  <script src="${pageContext.request.contextPath}/static/javascript/users/user-dashboard.js"></script>

</body>

</html>