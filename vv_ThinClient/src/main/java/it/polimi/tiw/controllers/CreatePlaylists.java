package it.polimi.tiw.controllers;

import java.sql.Date;
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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.PlaylistDAO;
import it.polimi.tiw.dao.SongDAO;


@WebServlet("/CreatePlaylist")
public class CreatePlaylists extends HttpServlet{

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
		request.setAttribute("invalidMethod","Error! Perform a valid method to continue!");
		String path = "/GoToHomePage";
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
		dispatcher.forward(request,response);
		return;	
		}
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		
		
		//*******CHECK CORRECTNESS AREA
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		//CHECK SESSION
		if (session.isNew() || user == null) {
			response.sendRedirect("/Progetto_tiw_thin_client/loginPage.html");
			return;
		}
		
		String method = request.getMethod();
		
		
		//CHECK METHOD
		if(!method.equals("POST")) {
			request.setAttribute("invalidMethod","Error! Perform a valid method to continue!");
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
		
		
		
		String title = request.getParameter("title");
		String[] songs = request.getParameterValues("canzoni");
		Date creationDate = new Date(System.currentTimeMillis());
		String error = "";
		
		
		//CHEKC TITLE
		if(title == null || title.isEmpty())
			error += "Title is empty";
		else if(title.length() > 45)
			error += "Title is too long";
		if(!error.equals("")){
			request.setAttribute("errorPlaylist", error);
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
		
		//CHECK SONGS
		if(songs == null) {
			error+="Playlist must include at least one song!\nIf there are no songs available create one !";
		}
		if(!error.equals("")){
			request.setAttribute("errorPlaylist",error);
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
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
				// TODO Auto-generated catch block
				request.setAttribute("errorPlaylist",error);
				String path = "/GoToHomePage";
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
				dispatcher.forward(request,response);
				return;	
			}
		}
		
		if(!error.equals("")){
			request.setAttribute("errorPlaylist",error);
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
		
		
		
		//*******CHECK CORRECTNESS AREA END
		
		
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
			e.printStackTrace();
			request.setAttribute("errorPlaylist","Impossible to create a new playlist. Try later");
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
	
		
		
		if(!result) {
			request.setAttribute("errorPlaylist","Impossible to create a new playlist (error)");
			String path = getServletContext().getContextPath() + "/GoToHomePage";
			response.sendRedirect(path);
			return;
		}
		
		String path = getServletContext().getContextPath() + "/GoToHomePage";
		response.sendRedirect(path);

		
		


		
		
		
	
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


