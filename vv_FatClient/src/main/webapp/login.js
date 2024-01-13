/**
 * Login
 */



(function() {
	
	window.onload = function(){
	
	console.log("Evaluating registration");

	var msg = new URLSearchParams(window.location.search).get("successfullyRegistrated");

	
	console.log(msg);
	
	if(msg === 'true'){
		document.getElementById("msg").textContent = "Successfully registrated! You can now login!";
	}
	}
	
	
	
    document.getElementById("loginButton").addEventListener('click' , (e) => {
		
		e.preventDefault();

        console.log("Login event!");
        //Take the closest form
        let form = e.target.closest("form");

        //Check if the form is valid -> every field is been filled
        if(form.checkValidity()){
			
			let user = document.getElementById("user").value;
            let password = document.getElementById("password").value;
            
            if(user.trim()==="" || password.trim()===""){
				document.getElementById("error").textContent = "Missing parameters (client check)";
				return;
			}
			

            //Make the call to the server
            makeCall("POST" , 'CheckLogin' , form ,
                function (x) {

                    if(x.readyState == XMLHttpRequest.DONE){
                        let message = x.responseText;
                        switch(x.status){
                            //If ok -> set the userName in the session
                            case 200:
                                sessionStorage.setItem('userName' , message);
                              
                                window.location.href = "home.html";
                                break;
                            //If ko -> show the error
                            default:
                                document.getElementById("error").textContent = message;
                                break;
                        }
                    }
                }
            );
        }else{
            form.reportValidity();
        }
    });
})();
