$(document).ready(function() {

    $.validator.addMethod("regex", function(value, element, regexp){
        return this.optional(element) || regexp.test(value)
    }, "Email must follow standard format");

    $("#per-user-report").validate({

        rules: {
            email: {
                required: true,
                regex: /^[a-zA-Z0-9_.-]+@[a-z.]+\.[a-z]{2,}$/
            }    
        },
        messages: {
            email: {
                required: "**Email cannot be left Empty",
                regex: "**Email must follow standard format"
            }
        },
        submitHandler: function(form) {

            const emailEntered = $('[name="email"]').val();
            const url = `http://localhost:8080/getReport?email=${emailEntered}`;

            fetch(url, {
                mathod: 'GET'
            })
            .then(response => {
                if(!response.ok) {
                    throw new Error("Http Error ! status: " + error)
                }
                return response.blob();
            })
            .then(blob => {
                const href = URL.createObjectURL(blob);
                const link = document.createElement('a');
                
                link.href = href;
                link.download='Report.xlsx';
                link.style.display='none';

                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);

                URL.revokeObjectURL(blob);
            })
            .catch(error => {
                alert("Failed to download the file, Error: " + error.message);
            })
        }
    })
})