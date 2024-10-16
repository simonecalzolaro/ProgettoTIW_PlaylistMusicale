{	
    //Page components
    var userPlaylists;
    var playlistList;
    var songUser;
    var songsInPlayList;
    var songsNotInPlayList;
    var songDetails;
    var sortingList;
    var playListSongsToOrder;
    var handleButtons;
    let personalMessage;
    let playListMessage;
    var pageOrchestrator = new PageOrchestrator();

    /**
     * It contains all the song titles and ids of the current playList needed for the sorting
     */
    function PlayListSongsToOrder(){
        /**
         * It's the id of the playList
         */
        this.playlistId = null;
        /**
         * It's an array that contains song element. This attribute will be used to fill the table for the reorder
         */
        this.songs = new Array();

        /**
         * Function used to reset the attribute songs
         */
        this.reset = function() {
            this.songs = [];
        }

        /**
         * Function that add a song to the attribute
         * @param song is the song to add
         */
        this.addSong = function(song) {
            this.songs.push(song);
        }
    }

    /**
     * Function that represent a song to be sorted
     */
    function Song(id , title){
        this.id = id;
        this.title = title;
    }

  

    /**
     * Function that initialize the personal message (the userName)
     * @param username is the userName to add in the HTML file
     * @param messageContainer is the place where add the userName
     */
    function PersonalMessage(userName , messageContainer) {
        this.username = userName;
        this.messageContainer = messageContainer;

        this.show = function() {
            this.messageContainer.textContent = this.username;
        }
    }

    function HandleButtons(before , next) {
    	this.before = before;
    	this.next = next;
    	
    	this.showBefore = function() {
    		this.before.style.display = "";
    	}
    	
    	this.hideBefore = function() {
    		this.before.style.display = "none";
    	}
    	
    	this.showNext = function() {
    		this.next.style.display = "";
    	}
    	
    	this.hideNext = function() {
    		this.next.style.display = "none";
    	}
    }

    /**
     * Function that show the name of the current playlist the user is watching
     * @param playlistName is the name of the playlist
     * @param messageContainer id the tag where put the name of the playlist
     */
    function PlaylistMessage(messageContainer){
        this.playlistName = null;
        this.messageContainer = messageContainer;

        this.show = function() {
            this.messageContainer.textContent = "PlayList: " + this.playlistName;
            this.messageContainer.style.display = "";
        }

        this.setPlayListName = function(playlistName) {
            this.playlistName = playlistName;
            this.show();
        }

        this.reset = function() {
            this.messageContainer.style.display = "none";
        }
    }

    /**
     * Function that take the playlist of the user from the data base
     * @param alertContainer is the container of the error
     * @param listContainer is the table that contains the list
     * @param listBodyContainer is the body of the table
     * @constructor
     */
    function PlaylistList(alertContainer , listContainer , listBodyContainer) {
        this.alertContainer = alertContainer;
        this.listContainer = listContainer;
        this.listBodyContainer = listBodyContainer;
        
        let self = this;
        let targetRow;
        let targetTitle;
        let targetTitles;

        this.reset = function() {
            this.listContainer.style.disply = "none";
            this.alertContainer.textContent = "";
        }
        
        this.setVisible = function() {
        	this.listContainer.style.display = "";
        }

        this.show = function(next) {
            let self = this;
			
			console.log(" PLAYLIST SHOW --> ");

            //Ask the playList table to the server
            makeCall("GET" , "GetPlaylistList" , null ,
                function(request) {
	
                    if(request.readyState == XMLHttpRequest.DONE){
	
                    	pageOrchestrator.resetErrors();
                    
                        switch(request.status){
                            case 200:
                                userPlaylists = JSON.parse(request.responseText);
                                
                                if(userPlaylists.length == 0){
                                    document.getElementById("playListTableMessage").textContent = "No playList yet";
                                    self.listContainer.style.display = "none";
                                    document.getElementById("orderPlaylistButton").style.display = "none";
                                    document.getElementById("goToSortingPageButton").style.display = "none";
                                	handleButtons.hideNext();
						        	handleButtons.hideBefore();
						        	playlistList.reset();
						        	songsInPlayList.reset();
						        	songsNotInPlayList.reset();
						        	songDetails.reset();
						        	
						        	playListMessage.reset();
                                    return;
                                }
                                document.getElementById("playListTableMessage").textContent = "";
                                document.getElementById("orderPlaylistButton").style.display = "";

                                self.alertContainer.textContent = "";
                                self.update(userPlaylists);
                                //Simulate a click with autoClick function
                                
                                if(next){
                                    next();
                                }
                                break;

                            case 403:
                                //Redirect to login.html and remove the username from the session
                                window.location.href = request.getResponseHeader("Location");
                                window.sessionStorage.removeItem("userName");
                                break;

                            default:
                            	alert("Default");
                                self.alertContainer.textContent = request.responseText;
                        }
                    }
                }
            );
        }

        this.update = function(playlists) {
			console.log(" PLAYLIST UPDATE --> ");

            //Elements of the table for each row
            let row , playListNameCell , creationDateCell , anchor , linkCell , linkText;
            //Save this to make it visible in the function
            let self = this;

            //Empty the table body
            this.listBodyContainer.innerHTML = "";

            playlists.forEach( function(playlist) {
                //Create the row
                row = document.createElement("tr");

                //Create the playlist title cell
                playListNameCell = document.createElement("td");
                
                anchor = document.createElement("a");
                row.id = "playlistList";
                playListNameCell.appendChild(anchor);
                linkText = document.createTextNode(playlist.title);
                anchor.appendChild(linkText);
                anchor.setAttribute("playlistId" , playlist.id);
                
                anchor.addEventListener("click" , (e) => {
                    //Reset the playListSongsToOrder
                    playListSongsToOrder.reset();
                    //Show songs in the playList selected
                    songsInPlayList.show(e.target.getAttribute("playlistId"));
                    //Show songs not in the playList selected
                    songsNotInPlayList.show(e.target.getAttribute("playlistId"));
                    //Show the title
                    targetRow = e.target.closest("tr");//Row of the event
                    targetTitles = targetRow.getElementsByTagName("a");
                    targetTitle = targetTitles[0].innerHTML;//Take the first td -> the title
                    playListMessage.setPlayListName(targetTitle);
                });
                //Disable the href of the anchor
                anchor.href = "#";

                row.appendChild(playListNameCell);

                //Create the creation date cell
                creationDateCell = document.createElement("td");
                creationDateCell.textContent = playlist.creationDate;
                row.appendChild(creationDateCell);

                self.listBodyContainer.appendChild(row);
            });
            //Show the table
            this.listContainer.style.display = "";
     
        }


        this.autoClick = function(playlistId) {
			
            let e = new Event("click");
            let selector = "a[playlistId=" + "\"" +  playlistId + "\"]";
            //Take the first element or the specified playlist
            let anchorToClick = (playlistId) ?
                document.querySelector(selector) :
                self.listBodyContainer.querySelectorAll("a")[0];           
   
            //console.log("AutoClick select playlist with id: " + anchorToClick.getAttribute("playlistId"));

            if(anchorToClick){
                anchorToClick.dispatchEvent(e);
            }
        
        }
        
    }
    
    
    /*
    *Function that takes all the user song and show them in the createplaylist section
    */
   function UserSong(listBodyContainer){
	   
	 
	   this.listBodyContainer = listBodyContainer;

	   let self = this;
	   let incomingSongs = null;

	
	
	   this.reset = function() {
		   this.listBodyContainer.textContent = "";
	   }


	   this.show = function() {
		   
		   console.log("UserSong show asking for user's songs--->")
		   let self = this;

		   //Ask the playList table to the server
		   makeCall("GET", "GetUserSongs", null,
			   function(request) {

				   if (request.readyState == XMLHttpRequest.DONE) {

					   pageOrchestrator.resetErrors();

					   switch (request.status) {
						   case 200:
							      incomingSongs  = JSON.parse(request.responseText);

							   if (incomingSongs.length == 0) { 
								   self.listBodyContainer.textContent = "No songs yet";

								   handleButtons.hideNext();
								   handleButtons.hideBefore();
								   playlistList.reset();
								   songsInPlayList.reset();
								   songsNotInPlayList.reset();
								   songDetails.reset();

								   playListMessage.reset();
								   return;
							   }
							   self.reset();
							   self.update();
							   
							   break;

						   case 403:
							   //Redirect to login.html and remove the username from the session
							   window.location.href = "login.html"
							   window.sessionStorage.removeItem("userName");
							   break;

						   default:
							   alert("Something went wrong within the application!");

					   }
				   }
			   }
		   );
	   }
	   
	   
	   
	   
        this.update = function() {
			
			console.log("UserSong update ---> ")
            //Elements of the table for each row
            let input,text,title;
            //Save this to make it visible in the function
          

            //Empty the table body
            this.listBodyContainer.innerHTML = "";

                incomingSongs.forEach( function(song) {
					
					
                //Create the row
                text = document.createElement("p");
                input = document.createElement("input");

                self.listBodyContainer.appendChild(text);
                text.appendChild(input);
                
                title = document.createTextNode(song.songTitle);
                text.appendChild(title);

                
         
               
                //Disable the href of the anchor
                input.type = "checkbox";
                input.name = "canzoni";
                input.id = "canzoniUtente";
                input.value = song.songId;
                

               

                //Create the creation date cell
                

            });
            
     
        }
	   
   }
    
    


    /**
     * Function that takes all the songs in the playlist specified by playlistId
     * @param alertContainer is the container where set the error
     * @param listContainer is the container of the table
     * @param listBodyContainer is the body of the table
     * @param playlistId is the playlist the user wants to see the songs
     */
    function SongsInPlaylist(alertContainer , listContainer , listBodyContainer){
        this.alertContainer = alertContainer;
        this.listContainer = listContainer;
        this.listBodyContainer = listBodyContainer;
        this.playlistId = null;
        this.songs = null;
        this.section = 0;

        this.reset = function() {
            this.listContainer.style.display = "none";
            this.alertContainer.textContent = "";
        }

        this.show = function(playlistId) {
			
			console.log("SongInPlaylist show --->")

            this.playlistId = playlistId;
            let self = this;

            makeCall("GET" , "GetSongsInPlaylist?playlistId=" + playlistId , null ,
                function(request) {
                    if(request.readyState == XMLHttpRequest.DONE){
						
	
                    	pageOrchestrator.resetErrors();
                    	
                  		if(playlistId == null){
							  this.alertContainer.textContent="The selected playlist is not valid. Try again!";
							  return;
						  }
                    	
                        switch(request.status){
                            case 200:
                                let songsReceived = JSON.parse(request.responseText);
        						
        						if(songsReceived.length > 1){
                                	document.getElementById("goToSortingPageButton").style.display = "";
                                }
                                else{
                                	document.getElementById("goToSortingPageButton").style.display = "none";
                                }
        
        
                                if(songsReceived.length == 0){
                                    //Empty the body of the table
                                    self.listContainer.style.display = "none";
            						self.listBodyContainer.innerHTML = "";
            						handleButtons.hideBefore();
            						handleButtons.hideNext();
            						songDetails.reset();
                                    document.getElementById("songTableMessage").textContent = "No songs yet";
                                    return;
                                }
								document.getElementById("songTableMessage").textContent = "";
                                self.songs = songsReceived;
                                
                                
                                //Set the playlistId
					            playListSongsToOrder.playlistId = self.playlistId;
					            //Reset the array
					            playListSongsToOrder.reset();
					            
					            //Save song titles and ids
					            self.songs.forEach( function(songToOrder) {
					                //Create a new song object
					                let song = new Song(songToOrder.songId , songToOrder.songTitle);
					                //Add it to playListSongsToOrder
					                playListSongsToOrder.addSong(song);
					            });
                                
                                
                                self.update(0);

                                //Launch the autoClick to select a song to show
                                if(self.playlistId !== songDetails.playlistId){
                              		self.autoClick();
                          		}
                                break;

                            case 403:
                                //Redirect to login.html and remove the username from the session
                                window.location.href = request.getResponseHeader("Location");
                                window.sessionStorage.removeItem("userName");
                                break;

                            default:
                                self.alertContainer.textContent = request.responseText;
                                break;
                        }
                    }
                }
            );
        }

        this.update = function(section) {
			

        	
            //Elements of the table
            let row, internalTableCell , imageRow , imageCell, songNameRow , songNameCell, internalTable , anchor, linkText , image;
            //Save this to make it visible in the function
            let self = this;
            //Empty the body of the table
            this.listBodyContainer.innerHTML = "";

            let next = false;

            //Check section and set next
            if (section < 0 || !section) {
                section = 0;
            }
            if (section * 5 + 5 > this.songs.length) {
                section = (this.songs.length / 5);
                //Save just the number before the point 
                section = parseInt(section.toString().split(".")[0]);
            }
            if ((section * 5 + 5) < this.songs.length) {
                next = true;
            }
            
        	//Set the current section
        	this.section = section;
            
            if(next){
            	handleButtons.showNext();
            }
            else{
            	handleButtons.hideNext();
            }
            
          	if(section > 0){
            	handleButtons.showBefore();
            }
            else{
            	handleButtons.hideBefore();
            }
            
            let songsToShow;

            if (this.songs.length >= section * 5 + 5){
            	songsToShow = this.songs.slice(section * 5, section * 5 + 5); // [)
            }   
               
            else{
            	songsToShow = this.songs.slice(section * 5, this.songs.length); // [)
            }

            //Create the main row of the external table
            row = document.createElement("tr");

            songsToShow.forEach( function (songToShow){
                internalTableCell = document.createElement("td");
                internalTable = document.createElement("table");

                internalTableCell.appendChild(internalTable);

                //Row for the image
                imageRow = document.createElement("tr");
                //Row for the song title
                songNameRow = document.createElement("tr");
                
                internalTable.appendChild(imageRow);
                internalTable.appendChild(songNameRow);

                imageCell = document.createElement("td");
                songNameCell = document.createElement("td");

                imageRow.appendChild(imageCell);
                songNameRow.appendChild(songNameCell);

                image = document.createElement("img");
                imageCell.appendChild(image);

                image.src = songToShow.base64String;
                
                anchor = document.createElement("a");
                songNameCell.appendChild(anchor);
                linkText = document.createTextNode(songToShow.songTitle);
                anchor.appendChild(linkText);
                anchor.setAttribute("songId" , songToShow.songId);
                anchor.href = "#";
                anchor.addEventListener("click" , (e) => {
                   songDetails.show(e.target.getAttribute("songId") , songsInPlayList.playlistId);
                });

                row.appendChild(internalTableCell);
            });
            this.listBodyContainer.appendChild(row);
            this.listContainer.style.display = "";
        }

        this.autoClick = function(songId) {
            let e = new Event("click");
            let selector = "a[songId=" + "\"" +  songId + "\"]";
            //Take the first element or the specified playList

            let anchorToClick = (songId) ?
                document.querySelector(selector) :
                this.listBodyContainer.querySelectorAll("a")[0];

            //console.log("AutoClick select song with id: " + anchorToClick.getAttribute("songId"));

            if(anchorToClick){
                anchorToClick.dispatchEvent(e);
            }else{
                //Show nothing if the playList has no song
                songDetails.reset();
            }
        }
    }

    /**
     * Function that shows songs not in the playList specified by playlistId
     * @param alertContainer is the container where set the error
     * @param listContainer is the container of the form
     * @param select is the select inside the form to be fulled
     */
    function SongsNotInPlaylist(alertContainer , listFieldset , listContainer , select){
        this.alertContainer = alertContainer;
        this.listFieldset = listFieldset;
        this.listContainer = listContainer;
        this.select = select;
        this.playlistId = null;

        this.reset = function() {
            this.listContainer.style.display = "none";
            this.alertContainer.textContent = "";
        }
        
        this.setVisible = function() {
        	this.listContainer.style.display = ""
        }

        this.show = function(playlistId) {
            this.playlistId = playlistId;
            let self = this;
            
            console.log("SongNotInPlaylist show --->");

            makeCall("GET" , "GetSongsNotInPlaylist?playlistId=" + playlistId , null ,
                function(request) {
                    if(request.readyState == XMLHttpRequest.DONE){
                        pageOrchestrator.resetErrors();
                        
                        switch(request.status){
                            case 200:
                                let songs = JSON.parse(request.responseText);

                                if(songs.length == 0){
                                	self.listFieldset.style.display = "none";
                                   	document.getElementById("addSongMessage").textContent = "All songs already in this playList";
                                    return;
                                }
                                document.getElementById("addSongMessage").textContent = "";
                                self.update(songs);
                                break;

                            case 403:
                                //Redirect to login.html and remove the username from the session
                                window.location.href = request.getResponseHeader("Location");
                                window.sessionStorage.removeItem("userName");
                                break;

                            default:
                                self.alertContainer.textContent = request.responseText;
                                break;
                        }
                    }
                }
            );
        }

        this.update = function(songsToShow) {

            let option;

            this.select.innerHTML = "";
            
            let self = this;

            //Add an option for each song
            songsToShow.forEach(function(songToShow) {
                option = document.createElement("option");
                option.setAttribute("value" , songToShow.id);
                option.appendChild(document.createTextNode(songToShow.songTitle));
                self.select.appendChild(option);
            });
            this.listFieldset.style.display = "";
            this.listContainer.style.display = "";
        }
    }

    /**
     * Function that shows the details of a selected song
     * @param alertContainer is the container of the error
     * @param listContainer is the container of the table
     * @param listBodyContainer is the body of the table where put the details
     */
    function SongDetails(alertContainer , listContainer , listBodyContainer){
        this.alertContainer = alertContainer;
        this.listContainer = listContainer;
        this.listBodyContainer = listBodyContainer;
        this.songId = null;
        this.playlistId = null;

        this.reset = function() {
            this.listContainer.style.display = "none";
        }
        
        this.setVisible = function() {
        	this.listContainer.style.display = "";
        }

        this.show = function(songId , playlistId) {
            this.songId = songId;
            this.playlistId = playlistId;

            let self = this;

            makeCall("GET" , "GetSongDetails?songId=" + this.songId + "&playlistId=" + this.playlistId , null ,
                function(request) {
                    if(request.readyState == XMLHttpRequest.DONE){
	
                    	pageOrchestrator.resetErrors();
                    	
                        switch(request.status){
                            case 200:
                                let songDetails = JSON.parse(request.responseText);
                                self.update(songDetails);
                                break;

                            case 403:
                                //Redirect to login.html and remove the username from the session
                                window.location.href = request.getResponseHeader("Location");
                                window.sessionStorage.removeItem("userName");
                                break;

                            default:
                                self.alertContainer.textContent = request.responseText;
                                break;
                        }
                    }
                }
            );
        }

        this.update = function(songDetails) {
            let row , titleCell , singerCell , albumTitleCell , publicationYearCell , genreCell , playCell;

            this.listBodyContainer.innerHTML = "";

            row = document.createElement("tr");

            titleCell = document.createElement("td");
            titleCell.appendChild(document.createTextNode(songDetails.songTitle));
            row.appendChild(titleCell);

            singerCell = document.createElement("td");
            singerCell.appendChild(document.createTextNode(songDetails.singer));
            row.appendChild(singerCell);

            albumTitleCell = document.createElement("td");
            albumTitleCell.appendChild(document.createTextNode(songDetails.albumTitle));
            row.appendChild(albumTitleCell);

            publicationYearCell = document.createElement("td");
            publicationYearCell.appendChild(document.createTextNode(songDetails.publicationYear));
            row.appendChild(publicationYearCell);

            genreCell = document.createElement("td");
            genreCell.appendChild(document.createTextNode(songDetails.genre));
            row.appendChild(genreCell);

            playCell = document.createElement("audio");
            playCell.type = "audio/mpeg";
            playCell.controls = "controls"
            playCell.src = songDetails.base64String;
            row.appendChild(playCell);

            this.listBodyContainer.appendChild(row);
            this.listContainer.style.display = "";
        }
    }

    function SortingList(alertContainer , divContainer , listContainer , listBodyContainer){
        this.alertContainer = alertContainer;
        this.divContainer = divContainer;
        this.listContainer = listContainer;
        this.listBodyContainer = listBodyContainer;
        this.playlistId = null;

        this.setPlaylistId = function(playlistId) {
            this.playlistId = playlistId;
        }

        this.reset = function() {
            this.divContainer.style.display = "none";
            alertContainer.textContent = "";
        }

        //Take the song from playListSongsToOrder and fill the table
        this.show = function() {
            //Define the components of the table
            let row , dataCell , nameCell;
            //Save this for the closure
            let self = this;
            
            //Empty the table
            this.listBodyContainer.innerHTML = "";
            
            for(let i = 0 ; i < playListSongsToOrder.songs.length ; i++){
            
            	let song = playListSongsToOrder.songs[i];

                row = document.createElement("tr");
                row.className = "draggable";
                row.setAttribute("songId" , song.id);

                dataCell = document.createElement("td");
                nameCell = document.createTextNode(song.title);
                dataCell.appendChild(nameCell);
                row.appendChild(dataCell);
                
                self.listBodyContainer.appendChild(row);
            }

            this.divContainer.style.display = "";

            //Add listeners to the new row
            handleSorting.addEventListeners();
        }
    }

    /**
     * It's the main controller of the application
     */
    function PageOrchestrator() {

		
        this.start = function() {
			console.log("START PAGE --> ");
			playListSongsToOrder = new PlayListSongsToOrder();
			
            //Set the personal message and show it.
            personalMessage = new PersonalMessage(sessionStorage.getItem("userName") , document.querySelector("#userName"));
            personalMessage.show();
            
            //handleButtons = new HandleButtons(document.getElementById("before") , document.getElementById("next"));
            handleButtons = new HandleButtons(document.getElementById("beforeButton") , document.getElementById("nextButton"));
            
            playListMessage = new PlaylistMessage(document.getElementById("playlistNameMessage"));

            //Initialize the playList table
            playlistList = new PlaylistList(document.getElementById("playlistTableError") , 
            							document.getElementById("playlistTable") , document.getElementById("playlistTableBody"));
            							
            //initializize the songs of the user
            songUser = new UserSong(document.getElementById("canzoni"));

            //Initialize the songs in the playList
            songsInPlayList = new SongsInPlaylist(document.getElementById("songTableError") , 
            							document.getElementById("songTable") , document.getElementById("songTableBody"));

            //Initialize songs not in the playlist
            songsNotInPlayList = new SongsNotInPlaylist(document.getElementById("addSongError") , document.getElementById("addSongToPlaylistFieldset") ,
                						document.getElementById("addSongToPlaylistDiv") , document.getElementById("addSongToPlayList"));

            //Initialize the songDetails
            songDetails = new SongDetails(document.getElementById("songDetailsError") ,
                                        document.getElementById("songPage") , document.getElementById("songDetailsTableBody"));

            //Initialize the sortingList
            sortingList = new SortingList(document.getElementById("sortingError") , document.getElementById("sortPlayListPage") ,
                                        document.getElementById("sortPlayListTable") , document.getElementById("sortPLayListBody"));
			//Don't show this content
			sortingList.reset();

            //Set the event of logout to the anchor
            document.querySelector("a[href='Logout']").addEventListener('click', () => {
                window.sessionStorage.removeItem('userName');
            });
            //Add listeners to 'before' and 'next' buttons
            document.getElementById("beforeButton").addEventListener("click" , () => {
            	pageOrchestrator.resetErrors();
    			songsInPlayList.update(songsInPlayList.section - 1);
    		});
        	document.getElementById("nextButton").addEventListener("click" , () => {
        		pageOrchestrator.resetErrors();
    			songsInPlayList.update(songsInPlayList.section + 1);
    		});
    		//Add the listener for the button to reorganized the playList
    		document.getElementById("goToSortingPageButton").addEventListener("click" , () => {
    			pageOrchestrator.resetErrors();
    			pageOrchestrator.showSortingPage();
    		});
    		//Add the listener for the button to reorganized the playList
    		document.getElementById("goToMainPageButton").addEventListener("click" , () => {
    			pageOrchestrator.resetErrors();
    			pageOrchestrator.showMainPage();
    		});
    		
    		this.refresh();
        }

        this.refresh = function(playlistId) {
            //Reset the errors            
			this.resetErrors();
			
			songUser.show();
			//Show a playList sending as parameter the autoclick function
			
            playlistList.show( function() {
                playlistList.autoClick(playlistId);
            });
        }
        
        this.showSortingPage = function() {
        	handleButtons.hideNext();
        	handleButtons.hideBefore();
        	playlistList.reset();
        	songsInPlayList.reset();
        	songsNotInPlayList.reset();
        	songDetails.reset();
        	document.getElementById("goToSortingPageButton").style.display = "none";
        	document.getElementById("homePage").style.display = "none";
        	document.getElementById("goToMainPageButton").style.display = "";
        	
        	playListMessage.reset();
        	document.getElementById("playlistToOrder").textContent = "Playlist name: " + playListMessage.playlistName;
        	document.getElementById("playlistToOrder").style.display = "";
        	
        	sortingList.show();
        }
        
        this.showMainPage = function() {
        	playlistList.setVisible();
        	songsInPlayList.update(0);
        	songsNotInPlayList.setVisible();
        	songDetails.setVisible();
        	document.getElementById("goToSortingPageButton").style.display = "";
        	document.getElementById("goToMainPageButton").style.display = "none";
        	document.getElementById("homePage").style.display = "";
        	
        	playListMessage.show();
        	document.getElementById("playlistToOrder").style.display = "none";
        	
        	sortingList.reset();
        }

		this.resetErrors = function() {
			document.getElementById("playlistTableError").textContent = "";
			document.getElementById("createPlaylistError").textContent = "";
			document.getElementById("songError").textContent = "";
			document.getElementById("songTableError").textContent = "";
			document.getElementById("addSongError").textContent = "";
			document.getElementById("songDetailsError").textContent = "";
			document.getElementById("sortingError").textContent = "";
	
        }
        
    }
    
    
    
    /*
     *Every time the homePage is loaded the applicatio checks if a valide session is available, if it's not it pull back the user to the loginPage
     +If a valide session is present then the pageOrchestrator is started 
    */
      window.addEventListener("load" , () => {
        if(sessionStorage.getItem("userName") == null){
            window.location.href = "login.html";
        }else{
		
            pageOrchestrator.start();
        }
    } , false);
    
}




