package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Playlist;
import it.polimi.tiw.beans.Song;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.PlaylistDAO;
import it.polimi.tiw.dao.SongDAO;

@WebServlet("/GoToHomePage")
public class GoToHomePage extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
	public void init() {
		ServletContext context = getServletContext();
		
		//Initializing the template engine
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
		try {
			
			//Initializing the connection
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			
			Class.forName(driver);
			connection = DriverManager.getConnection(url , user , password);
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void doGet(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		//Need to take the user from the session and make the control
		
		
		//***CHECK AREA
		
		//CHECK SESSION
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (session.isNew() || user == null) {
			response.sendRedirect("/Progetto_tiw_thin_client/loginPage.html");
			return;
		}
		
		ArrayList<Playlist> playlists = null;
		ArrayList<Song> songs = null;
		String error = "";
		String error1 = "";
		String error2 = "";
		String error3 ="";
		
		
	
		
		PlaylistDAO playlistDao = new PlaylistDAO(connection);
		SongDAO songDao = new SongDAO(connection);
		
		//In case of forward from CreatePlaylist , CreateSong and GoToPlayistPage 
		if(((String) request.getAttribute("invalidMethod")) != null) 
			error = (String) request.getAttribute("invalidMethod");
		else if(((String) request.getAttribute("errorPlaylist")) != null) 
			error1 = (String) request.getAttribute("errorPlaylist");
		else if(((String) request.getAttribute("errorSong")) != null) //from GoToPlaylistPage
			error2 = (String) request.getAttribute("errorSong");
		else if((String) request.getAttribute("errorGoToP") != null)
			error3 = (String) request.getAttribute("errorGoToP");
			
			
		try {
			playlists = playlistDao.findUserPlaylists(user.getId());
			songs = songDao.getSongsByUser(user.getId());
			
			
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database extraction");
		}
		
		String path = "/WEB-INF/homePage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request , response , servletContext , request.getLocale());
		ctx.setVariable("playlists" , playlists);
		ctx.setVariable("songs", songs);
		ctx.setVariable("user", user);
		ctx.setVariable("invalidMethod", error);
		ctx.setVariable("errorPlaylist", error1);
		ctx.setVariable("errorSong", error2);
		ctx.setVariable("errorGoToP", error3);

		templateEngine.process(path , ctx , response.getWriter());
		
	}
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		doGet(request , response);
	}
	
	
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}


