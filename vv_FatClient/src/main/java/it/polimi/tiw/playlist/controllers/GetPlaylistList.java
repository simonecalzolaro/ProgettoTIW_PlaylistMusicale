package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.playlist.beans.Playlist;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/GetPlaylistList")
@MultipartConfig
public class GetPlaylistList extends HttpServlet {

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
		
		//Take the user from the session
		HttpSession s = request.getSession();
		User user = (User) s.getAttribute("user");
		ArrayList<Playlist> playlists = null;
		
	   
		
		 
	    if(s.isNew() || user == null || !user.getUserName().equals(request.getHeader("user"))) {
	    	response.sendError(HttpServletResponse.SC_FORBIDDEN); //Code 403
		    System.out.println("out");

			return;
		}
		
		
		PlaylistDAO pDao = new PlaylistDAO(connection);
				
		try {
			playlists = pDao.findPlaylist(user.getId());
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("Internal server error, retry later");
			return;
		}
		
		//Create the jSon with the answer
		Gson gSon = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
		String jSon = gSon.toJson(playlists);
		response.setStatus(HttpServletResponse.SC_OK);//Code 200
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jSon);
	}
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		doGet(request , response);
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}
















