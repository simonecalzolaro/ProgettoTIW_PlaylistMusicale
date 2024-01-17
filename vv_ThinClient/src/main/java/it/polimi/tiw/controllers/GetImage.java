package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.User;


@WebServlet("/GetImage/*")
public class GetImage extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	String folderPath = "";
	private Connection connection;
	
	public void init() {
		folderPath = getServletContext().getInitParameter("albumImgPath");
		
		try {
			//Initializing the connection
			ServletContext context = getServletContext();
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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		//Take the pathInfo from the request
		String pathInfo = request.getPathInfo();
		
		
		
		// CHECK SESSION
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (session.isNew() || user == null) {
			response.sendRedirect("/Progetto_tiw_thin_client/loginPage.html");
			return;
		}
		//Check if the path info is valid
		if (pathInfo == null || pathInfo.equals("/")) {
			//Set an error and return nothing
			return;
		}
		
		//Take the fileName from the pathInfo without the "/" character
		String filename = URLDecoder.decode(pathInfo.substring(68), "UTF-8");

		
		
		
		
		//Open the file
		File file = new File(pathInfo);
		
		if (!file.exists() || file.isDirectory()) {

			return;
		}
		
		//Set headers for browser
		response.setHeader("Content-Type", getServletContext().getMimeType(filename));
		response.setHeader("Content-Length", String.valueOf(file.length()));
		
		//inline     -> the user will watch the image immediately
		//attachment -> the user has to do something to watch the image
		//filename   -> used to indicate a fileName if the user wants to save the file
		response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
		
		//Copy the file to the output stream
		Files.copy(file.toPath(), response.getOutputStream());
		
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