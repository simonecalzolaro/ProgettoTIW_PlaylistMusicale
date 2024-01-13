/**
 * Create a new playList
 */
(function() {
    document.getElementById("createPlaylistButton").addEventListener("click" , (e) => {
		
		
		
		e.preventDefault();
		
	
        console.log("Creating a new playList -->");

        //Take the closest form
        let form = e.target.closest("form");

        if(form.checkValidity()){
        	 //Check if the title specified is valid
        	 let title = document.getElementById("name").value;
        	
        	 if(title.trim()===""){
                document.getElementById("createPlaylistError").textContent = "Missing parameters - client check";
                return;
			 }
			 
			 let songs_len = document.querySelectorAll("#canzoniUtente:checked").length;
			 
			 if(songs_len == 0){
				document.getElementById("createPlaylistError").textContent = "Playlist must include at least one song. If there're no songs create one! -- client check";
                return;
			 }
			 

					 
       
        
        	console.log("createPlaylist.js request CreatePlaylist.java ---> ")
            //Make the call to the server
            makeCall("POST" , "CreatePlaylist" , form ,
                function (x) {

                    if(x.readyState == XMLHttpRequest.DONE){
                    pageOrchestrator.resetErrors();
                    
                        switch (x.status){
							
			
                            case 200:
                                //Update the playList list
                                console.log("request success --->")
                                playlistList.show();
                                break;

                            case 403:
                                sessionStorage.removeItem("userName");
                                window.location.href = "login.html";
                                break;

                            default:
                                document.getElementById("createPlaylistError").textContent = x.responseText;
                                break;
                        }
                    }
                }
            );
            form.reset();
        }else{
            form.reportValidity();
        }
    });
})();