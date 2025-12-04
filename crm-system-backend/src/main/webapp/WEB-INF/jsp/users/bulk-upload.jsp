<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Bulk User Upload</title>
<%--    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/bulk-upload.css">--%>
    <script src="${pageContext.request.contextPath}/static/javascript/jquery.js"></script>
    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/bulk-upload.css?v=1">
    <!-- jQuery & Plugins -->
    <script src="${pageContext.request.contextPath}/static/javascript/jquery.js"></script>
    <script src="${pageContext.request.contextPath}/static/javascript/jquery.validate.min.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/javascript/datatables.min.css"/>
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
                <button id="manage-users" class="drop-item" onclick="window.location.href =`/crm/users/user-dashboard`">
                    Manage Users
                </button>
                <button id="back" class="drop-item">Back</button>
                <button id="delete-profile" class="drop-item">Delete Profile</button>
                <button id="logout" class="drop-item">Logout</button>

            </div>

        </div>
        <img src="${pageContext.request.contextPath}/static/assests/profile.png" class="profile-pic" id="view-profile">
    </div>
    </div>
</header>
<!-- Alert container -->
<div id="alert-container" class="mt-3"></div>

<main class="dashboard-container">
    <div class="dashboard-section p-4"
         style="background-color: #fff; border-radius: 12px; box-shadow: 0 2px 6px rgba(0,0,0,0.08);">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h3 class="m-0">Upload Users</h3>
            <div>
                <button class="btn btn-outline-primary me-2" id="downloadTemplate">
                    <i class="bi bi-download"></i> Download Template
                </button>
                <button class="btn btn-warning" id="uploadTemplateBtn">
                    <i class="bi bi-upload"></i> Upload Template
                </button>
            </div>
        </div>
        <p class="text-muted mb-0">Download the Excel template, fill in lead details, and upload it to bulk import
            leads.</p>
    </div>
    <div class="dashboard-section mt-4 p-4"
         style="background-color: #fff; border-radius: 12px; box-shadow: 0 2px 6px rgba(0,0,0,0.08);">
        <h4 class="mb-3">Upload History</h4>
        <hr/>
        <div class="upload-history-table-container">
            <table id="upload-table" class="table table-bordered table-hover align-middle" style="width:100%;">
                <thead class="table-light">
                <tr>
                    <th style="display:none">ID</th>
                    <th>Sr.No.</th>
                    <th>File Name</th>
                    <th>Uploaded At</th>
                    <th>Uploaded By</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
                </thead>

                <tbody>
                <!-- Rows will be dynamically populated -->
                </tbody>
            </table>
        </div>
    </div>


    <div class="modal fade" id="profileModal" tabindex="-1" aria-labelledby="profileModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">

                <form id="profileForm">

                    <div class="modal-header text-white">
                        <h5 class="modal-title" id="profileModalLabel">User Profile</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"
                                aria-label="Close"></button>
                    </div>

                    <div class="modal-body">
                        <div class="row g-3">

                            <div class="col-md-6">
                                <label class="form-label fw-bold">Name</label>
                                <input type="text" class="form-control" id="profileName" name="profileName" readonly>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label fw-bold">Mobile</label>
                                <input type="text" class="form-control" id="profileMobile" name="profileMobile"
                                       readonly>
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
                                <textarea class="form-control" id="profileAddress" name="profileAddress"
                                          readonly></textarea>
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
                                <input type="text" class="form-control" id="profileCountry" name="profileCountry"
                                       readonly>
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

</main>
<script src="${pageContext.request.contextPath}/static/javascript/bulk-upload.js?v=3"></script>
<script src="${pageContext.request.contextPath}/static/javascript/users/upload_file_history.js?v=1"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/static/javascript/components/header.js?v=1"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</body>

</html>