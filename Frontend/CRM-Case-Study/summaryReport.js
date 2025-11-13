
$(document).ready(function() {

    $.validator.addMethod("lessThan", function(value, element, param){
        var startDate = new Date(value);
        var endDate = new Date($(param).val());
        if(!endDate) {
            return true;
        }
        var endDate = new Date(endDate);
        if(isNaN(endDate.getTime())) {
            return true;
        }
        return this.optional(element) || startDate < endDate;
    }, "From Validator: Start date must be before end date");

    // value - Used to create the startDate object
    // element - allows the validation method to interact directly with the HTML input
    // param - Used to retrieve the value of the comparison field

    $.validator.addMethod("NotEqual", function(value, element, param){
        var startDate = new Date(value);
        var endDate = new Date($(param).val());

        var start = startDate.getFullYear() + "-" + (startDate.getMonth() + 1) + "-" + startDate.getDate();
        var end = endDate.getFullYear() + "-" + (endDate.getMonth() + 1) + "-" + endDate.getDate();

        return this.optional(element) || start !== end;
    }, "Start date and end date cannot be equal");


    $("#summary-report").validate({
        rules: {
            startDate: {
                required: true,
                lessThan: "[name='endDate']",
                NotEqual: "[name='endDate']"
            },
            endDate : {
                required: true,
                NotEqual: "[name='startDate']"
            } 
        },
        messages: {
            startDate: {
                required: "**Start date is missing",
                lessThan: "**Start date must be before end date",
                NotEqual: "**Start date and end date cannot be equal"
            },
            endDate: {
                required: "**End date is missing",
                NotEqual: "**Start date and end date cannot be equal"
            }
        },

        submitHandler: function(form) {

            const start = $('[name="startDate"]').val();
            const end = $('[name="endDate"]').val();

            const url = `http://localhost:8080/getSummaryReport?start=${start}&end=${end}`;

            fetch(url, {
                method: 'GET'
            })
            .then(response => {
                if(!response.ok) {
                    throw new Error(`Http Error ! status: ${response.status}`);
                }
                return response.blob();
            })
            .then(blob => {                                  // Separate link is created and destroyed every time
                const href = URL.createObjectURL(blob);      // Creates a temporary link.
                const link = document.createElement('a');    // Creates a hidden link element.
                link.href = href;                            // Sets the link's destination.
                link.download = 'Summary_Report.xlsx';               // Names the file.
                link.style.display = 'none';                 // Hides the link

                document.body.appendChild(link);             // Attaches the link to the page
                link.click();                                // Triggers the download.
                document.body.removeChild(link);             // Cleans up

                URL.revokeObjectURL(href);                   // Cleans up the memory
            })
            .catch(error => {
                alert("Failed to download the file, Error: " + error.message);
            });
        }
    })
})