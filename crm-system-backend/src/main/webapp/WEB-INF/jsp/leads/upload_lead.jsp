<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Upload Leads | CRM Lead Management</title>

  <!-- Bootstrap -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />

  <!-- Custom CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/lead/upload_lead.css" />

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
  
  <!-- Main Section -->
  <main class="dashboard-container">
     <!-- Alert container -->
    <div id="alert-container" class="mt-3"></div>
    <!-- Upload Leads Section (Top White Container) -->
    <div class="dashboard-section p-4" style="background-color: #fff; border-radius: 12px; box-shadow: 0 2px 6px rgba(0,0,0,0.08);">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h3 class="m-0">Upload Leads</h3>
        <div>
          <button class="btn btn-outline-primary me-2" id="downloadTemplate">
            <i class="bi bi-download"></i> Download Template
          </button>
          <button class="btn btn-success" id="uploadTemplateBtn">
            <i class="bi bi-upload"></i> Upload File
          </button>
        </div>
      </div>
      <p class="text-muted mb-0">Download the Excel template, fill in lead details, and upload it to bulk import leads.</p>
    </div>

    <!-- Upload History Table -->
    <div class="dashboard-section mt-4 p-4" style="background-color: #fff; border-radius: 12px; box-shadow: 0 2px 6px rgba(0,0,0,0.08);">
      <h4 class="mb-3">Upload History</h4>
      <hr />
      <div class="upload-history-table-container">
        <table id="upload-table" class="table table-bordered table-hover align-middle" style="width:100%;">
          <thead>
            <tr>
              <th>Sr.No</th>
              <th>File Name</th>
              <th>Uploaded At</th>
              <th>Uploaded By</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
           
          </tbody>
        </table>
      </div>
    </div>
  </main>

  <!-- Upload Leads Modal -->
  <div class="modal fade" id="uploadLeadsModal" tabindex="-1" aria-labelledby="uploadLeadsLabel" aria-hidden="true">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title text-white" id="uploadLeadsLabel">Upload Leads from Excel</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <div class="modal-body">
          <form id="uploadLeadsForm" enctype="multipart/form-data">
            <div class="mb-3">
              <label for="leadFile" class="form-label">Select Excel File (.xlsx / .xls)</label>
              <input type="file" class="form-control" id="leadFile" name="file" accept=".xlsx,.xls" required />
            </div>
            <div class="text-center">
              <button type="submit" class="btn btn-outline-success">Upload & Process</button>
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
  <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  <!-- Custom JS -->
    <script src="${pageContext.request.contextPath}/static/javascript/leads/constants/end_point_constants.js?v=1"></script>
    <script src="${pageContext.request.contextPath}/static/javascript/leads/upload_lead.js"></script>
    <script src="${pageContext.request.contextPath}/static/javascript/leads/download_templates.js"></script>
    <script src="${pageContext.request.contextPath}/static/javascript/leads/upload_file_history.js"></script>
    <script src="${pageContext.request.contextPath}/static/javascript/components/header.js"></script>
</body>

</html>
