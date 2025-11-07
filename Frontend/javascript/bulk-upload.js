$(document).ready(function () {

    $("#bulkUploadForm").submit(function (e) {
        e.preventDefault();

        let file = $('#userFile')[0].files[0];
        if (!file) {
            alert("⚠ Please choose an Excel file first!");
            return;
        }

        const token = sessionStorage.getItem("Authorization");
        if (!token) {
            alert("⚠ Session expired. Please login again.");
            window.location.href = "/Frontend/html/login.html";
            return;
        }

        let formData = new FormData();
        formData.append("file", file);

        $.ajax({
            url: 'http://localhost:8080/crm/user/upload-user-file',
            type: 'POST',
            headers: { "Authorization": "Bearer " + token },
            data: formData,
            processData: false,
            contentType: false,
            success: function (response,data) {
    
               alert("Data Inserted Successfully !")
            },
            error: function (xhr,data) {
            
                alert("Error in file");
               
            }
        });
    });

$("#downloadTemplate").click(function (e){
    e.preventDefault();

            $.ajax({
            url: 'http://localhost:8080/crm/files/user-template',
            type: 'GET',
            xhxhrFields: { responseType: 'blob' },
            success: function (status,data,xhr,) {
               

            let filename = "User_Template.xlsx";
    
            const blob = new Blob([data]);
            const url = window.URL.createObjectURL(blob);

            const a = document.createElement("a");
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            a.remove();

            window.URL.revokeObjectURL(url);
            },
            error: function (xhr) {
                alert("Error in downloading file");
               
            }
        });

})


});
