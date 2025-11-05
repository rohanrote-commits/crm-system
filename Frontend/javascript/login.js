$('#loginButton').click(function() {
    let user = {
        email : $('#loginEmail').val(),
        password : $('#loginPassword').val()
    };

    $.ajax({
        url : 'http://localhost:8080/crm/user/sign-in',
        type : 'POST',
        contentType : 'application/json',
        data : JSON.stringify(user),
        success : function(token) {
            if(token){
                alert('Login Successful ✅');
                sessionStorage.setItem("jwt", token);   // ✅ correct way
                window.location.href = "/dashboard.html"; 
            }
        },
        error : function(xhr) {
            if(xhr.status == 401){
                alert("❌ Invalid Credentials");
            } else {
                alert("⚠ Server side error occurred");
            }
        }
    });
});
