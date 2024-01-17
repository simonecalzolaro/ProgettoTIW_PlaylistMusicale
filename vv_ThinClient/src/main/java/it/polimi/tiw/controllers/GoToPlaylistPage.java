package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
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

@WebServlet("/GoToPlayListPage")
public class GoToPlaylistPage extends HttpServlet{
	
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
		String playlistId = request.getParameter("playlistId");
		String section = request.getParameter("section");//Which songs need to be shown
		String error = "";
		String error1 = "";
		int id = -1;
		int block = 0;
		int n_blocks = 0;

		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		//CHECK SESSION
		if (session.isNew() || user == null) {
			response.sendRedirect("/Progetto_tiw_thin_client/loginPage.html");
			return;
		}
		
		//Take the user
		
	
	    
		//Check if playlistId is valid
		if(playlistId == null || playlistId.isEmpty())
			error += "Playlist not defined;";
		
		//If section is null or empty set it to the default value
		if(section == null || section.isEmpty()) {
			section = "0";
		}
		
		//Check the follow only if the id is valid
		if(error.equals("")) {
			//Create the DAO to check if the playList id belongs to the user 
			PlaylistDAO pDao = new PlaylistDAO(connection);
			
			try {
				//Check if the playlistId is a number
				id = Integer.parseInt(playlistId);
				//Check if section is a number
				block = Integer.parseInt(section);
				//Check if the user can access at this playList --> Check if the playList exists
				if(!pDao.findPlayListById(id, user.getId())) {
						error += "PlayList doesn't exist";
				}
				if(block < 0)
					block = 0;
			}catch(NumberFormatException e) {
				error += "Playlist e/o section not defined;";
			}catch(SQLException e) {
				error += "Impossible comunicate with the data base;";
			}
		}	
		
		if(!error.equals("")){
			request.setAttribute("errorGoToP", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		//The user created this playList
		
		//Take the error in case of forward from AddSong
		if(request.getAttribute("error") != null) {
			error += (String) request.getAttribute("error");
		}
		//Take the error in case of forward from GoToSongPage
		else if(request.getAttribute("error2") != null) {
			error1 += (String) request.getAttribute("error2");
		}
		
		//to take songs in and not in the specified playList
		SongDAO sDao = new SongDAO(connection);
		
		//To take the title of the playList
		PlaylistDAO pDao = new PlaylistDAO(connection);
		
		//Take the titles and the image paths
		try {
			
			ArrayList<Song> songsInPlaylist = sDao.getSongTitleAndImg(id);
			ArrayList<Song> songsNotInPlaylist = sDao.getSongsNotInPlaylist(id , user.getId());
			String title = pDao.findPlayListTitleById(id);
			ArrayList<Song> songs = new ArrayList<Song>();
			boolean next = false;
			
			n_blocks = (int) Math.ceil((double)songsInPlaylist.size()/5);
			
			if(block >= n_blocks) block = 0;
			
			System.out.println(songsInPlaylist.size());
			
			System.out.println(n_blocks);
			
			for(int i=block*5;i<(block*5 + 5) && i<songsInPlaylist.size();i++ ) {
				songs.add(songsInPlaylist.get(i));
			}
			
			if(block<n_blocks-1) {
				next = true;
			}
			
			Playlist p = new Playlist();
			p.setId(id);
			p.setTitle(title);
			
			String path = "/WEB-INF/playlistPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request , response , servletContext , request.getLocale());
			ctx.setVariable("user" , user);
			ctx.setVariable("songsInPlaylist", songs);
			ctx.setVariable("songsNotInPlaylist", songsNotInPlaylist);
			ctx.setVariable("playlist", p);
			ctx.setVariable("block", block);
			ctx.setVariable("next", next);
			
			ctx.setVariable("errorMsg", error);
			ctx.setVariable("errorMsg1", error1);
			templateEngine.process(path , ctx , response.getWriter());
			
		}catch(SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An arror occurred with the db, retry later");
		}
	}
	
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		doGet(request,response);
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
