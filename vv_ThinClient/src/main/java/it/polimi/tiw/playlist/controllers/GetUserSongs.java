package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.polimi.tiw.playlist.beans.SongDetails;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;
import it.polimi.tiw.playlist.utils.GetEncoding;

@WebServlet("/GetUserSongs")
@MultipartConfig
public class GetUserSongs extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection;

	public void init() {
		ServletContext context = getServletContext();
		
		try {
			connection = ConnectionHandler.getConnection(context);
		} catch (UnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void doGet(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		//Take the playList id

		
		
		HttpSession s = request.getSession();
		
		//Take the user
	    User user = (User) s.getAttribute("user");
	    
	    

	    
	    if(s.isNew() || user == null || !user.getUserName().equals(request.getHeader("user"))) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); //Code 403
		    System.out.println("out");

			return;
		}
		
	
			

	
		
		//The user created this playList
		
		//to take songs in and not in the specified playList
		SongDAO sDao = new SongDAO(connection);
		
		//To take a specific ordering for songs (if present)

		// Take the titles and the image paths
		try {

			ArrayList<SongDetails> songs = sDao.getUserSongs(user.getId());

			// Send all the song of the playList
			JSONArray jArray = new JSONArray();
			JSONObject jSonObject;

			for (SongDetails song : songs) {
				

				// Here to reset the attribute for each song
				jSonObject = new JSONObject();

				jSonObject.put("songId", song.getId());
				jSonObject.put("songTitle", song.getSongTitle());
				jSonObject.put("fileName", song.getImgFile());
				try {
					jSonObject.put("base64String",
							GetEncoding.getImageEncoding(song.getImgFile(), getServletContext(), connection, user));
				} catch (IOException e) {
					jSonObject.put("base64String", "");
				}

				jArray.put(jSonObject);



			}
			
	


			response.setStatus(HttpServletResponse.SC_OK);// Code 200
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(jArray);

		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);// Code 500
			response.getWriter().println("Internal server error, retry later");
		} catch (JSONException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);// Code 500
			response.getWriter().println("Internal server error, error during the creation of the response");
		}
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}