package it.polimi.tiw.playlist.controllers;

import java.io.BufferedReader;
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

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;
import it.polimi.tiw.playlist.utils.FromJsonToArray;

@WebServlet("/AddSorting")
@MultipartConfig
public class AddSorting extends HttpServlet {

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
		doPost(request , response);
	}
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		String playlistId = StringEscapeUtils.escapeJava(request.getParameter("playlistId"));
		int pId = -1;
		
		HttpSession s = request.getSession();
		//Take the user
	    User user = (User) s.getAttribute("user");
		
	    
	    if(s.isNew() || user == null || !user.getUserName().equals(request.getHeader("user"))) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); //Code 403
		    System.out.println("out");

			return;
		}
		
		
		if(playlistId == null || playlistId.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println("PlayList not specified");
		}
		
		try {
			pId = Integer.parseInt(playlistId);
		}catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println("PlayList not specified");
		}
		
		//System.out.println("In Add Sorting");
		//System.out.println("PlayList id received is: " + playlistId);
		
	    StringBuffer jb = new StringBuffer();
	    String line = null;
	    //Read the body of the request
	    try {
	        BufferedReader reader = request.getReader();
	        while ((line = reader.readLine()) != null) {
		       jb.append(line); 
	        }
	    } catch (Exception e) { 
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500	
			response.getWriter().println("Error reading the request body, retry later");
			System.out.println("Error reading the request body, retry later");
	    }
	    
		//Create the jSon with the sorting
		Gson gSon = new GsonBuilder().create();
		String newSorting = gSon.toJson(jb);
		
		if(newSorting == null || newSorting.length() <= 1) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println("Add more songs to order you playlist!");
		}
		
		//Convert the String array in an arrayList of integer in order to make some checks
		ArrayList<Integer> sortedArray = FromJsonToArray.fromJsonToArrayList(newSorting);
		SongDAO sDao = new SongDAO(connection);
		
		int currentNumber = -1;
		
		//Delete duplicate of the same song
		for(int i = 0 ; i < sortedArray.size() ; i++) {
			currentNumber = sortedArray.get(i);
			for(int j = i + 1 ; j < sortedArray.size() ; j++) {
				if(sortedArray.get(j) == currentNumber) {
					sortedArray.remove(j);
				}
			}
		}
		
		//Verify if each song id belongs to the user
		for(Integer id : sortedArray) {
			try {
				if(!sDao.findSongByUser(((int) id) , ((User) request.getSession().getAttribute("user")).getId()) ){
					//Delete this id -> it doesn't belong to this user
					sortedArray.remove(id);
				}
			}catch(SQLException e) {
				sortedArray.remove(id);
			}
		}
		
		//Re-convert the arrayList of integer in the String to upload
		String updatedSorting = gSon.toJson(sortedArray);
		
		PlaylistDAO pDao = new PlaylistDAO(connection);
		
		try {
			boolean result = pDao.addSorting(pId, updatedSorting);
			
			if(result == true) {
				response.setStatus(HttpServletResponse.SC_OK);//Code 200
			}else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
				response.getWriter().println("Internal server error, retry later");
			}
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("Internal server error, retry later");
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
