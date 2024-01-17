package it.polimi.tiw.playlist.controllers;

import java.sql.Date;
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

import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/CreatePlaylist")
@MultipartConfig
public class CreatePlaylist extends HttpServlet{

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
		String title = StringEscapeUtils.escapeJava(request.getParameter("name"));
		String[] songs = request.getParameterValues("canzoni");
		Date creationDate = new Date(System.currentTimeMillis());
		String error = "";

		HttpSession s = request.getSession();
		User user = (User) s.getAttribute("user");
		
		 
	    if(s.isNew() || user == null || !user.getUserName().equals(request.getHeader("user"))) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN); //Code 403
		    System.out.println("out");

			return;
		}
		
		
		if(title == null || title.isEmpty())
			error += "Title is empty";
		else if(title.length() > 45)
			error += "Title is too long";
		
		if(!error.equals("")){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println(error);
			return;
		}
		
		if(songs == null) {
			error+="Playlist must include at least one song!\nIf there are no songs availabe create one (server check) !";
		}
		if(!error.equals("")){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println(error);
			return;
		}
		
		
		
		
		
		SongDAO songDao = new SongDAO(connection);
		
		for(int i=0;i<songs.length;i++) {
			try {
				if(!songDao.findSongByUser(Integer.parseInt(songs[i]),user.getId())) {
					error+="Impossible to create a new playlist(song parsing)";
					i=songs.length;
				}
			} catch (NumberFormatException | SQLException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
				response.getWriter().println(error);
				return;
			}
		}
		
		if(!error.equals("")){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println(error);
			return;
		}
		
		
		
		boolean result = false;
		
		ArrayList<Integer> existingSongs = new ArrayList<>();

		
		
		PlaylistDAO playistDao = new PlaylistDAO(connection);
		
		try {
			for(int i=0; i<songs.length;i++) {
				if(songDao.findSongByUser(Integer.parseInt(songs[i]), user.getId())) {
					existingSongs.add(Integer.parseInt(songs[i]));
				}
			}
			
		
			result = playistDao.createPlaylist(title, creationDate, user.getId(), existingSongs);

			
		}catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println("Impossible to create a new playlist. Try later");
			return;
		}
		
	
		
		
		if(!result) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println("Impossible to create a new playlist. Try later");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);

	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
