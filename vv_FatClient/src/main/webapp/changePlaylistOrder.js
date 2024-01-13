/**
 * 
 */

(function() {
	document.getElementById("orderPlaylistButton").addEventListener("click", (e) => {

		e.preventDefault();

		console.log("invoked order choosing");
		

		let name = sessionStorage.getItem("userName");
		console.log(name);

		let order = document.getElementById("orderPlaylistButton").value;
		

		let reversePlaylists = [];

		for (let i = userPlaylists.length - 1; i >= 0; i--) {
			reversePlaylists.push(userPlaylists[i]);
		}

		userPlaylists = reversePlaylists;
		
		playlistList.update(userPlaylists);
		
		if(order == "desc") document.getElementById("orderPlaylistButton").value = "asc";
		else document.getElementById("orderPlaylistButton").value = "desc";

		




	});
})();