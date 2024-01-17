package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.SongDAO;

@WebServlet("/CreateSong")
@MultipartConfig 
public class CreateSong extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private String imgFolderPath = "";
	private String mp3FolderPath = "";
	
	public void init() {
		ServletContext context = getServletContext();
		
		//Initializing the folder where images and mp3 files will be uploaded
		imgFolderPath = context.getInitParameter("albumImgPath");
		mp3FolderPath = context.getInitParameter("songFilePath");
		
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
		
		//Take the request parameters
		String songTitle = request.getParameter("title");
		String genre = request.getParameter("genre");
		String albumTitle = request.getParameter("albumTitle");
		String singer = request.getParameter("singer");
		String date = request.getParameter("date");
		
		Part albumImg =null;
		Part songFile =null;
		
		try {
			albumImg = request.getPart("albumImg");
			songFile = request.getPart("songFile");
		}catch(Exception e) {
			request.setAttribute("errorSong", "Invalid enctype!");
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
	
		
		
		//CHECK PARAMETERS
		if (songTitle == null || songTitle.isEmpty() || genre == null || genre.isEmpty() || albumTitle == null
				|| albumTitle.isEmpty() || singer == null || singer.isEmpty() || date == null || date.isEmpty()
				|| albumImg == null || songFile == null) {

			request.setAttribute("errorSong", "Missing parameters");
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
		
		String error="";

		
		//CHECK DATE
		int pubYear=0;
		
		try {
			pubYear = Integer.parseInt(date);
			
			//Take the current year
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			
			//Check if the publicationYear is not bigger than the current year
			
			if(pubYear > currentYear || pubYear < 0)
				error += "Invalid date;";
		}catch(NumberFormatException e) {
			error += "Date not valid;";
		}
		
		
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
		
		
		//CHECK GENRE
		if (!genre.equals("Reggae") && !genre.equals("Pop") && !genre.equals("Rock") && !genre.equals("Rap")
				&& !genre.equals("Hip_hop") && !genre.equals("Classical") && !genre.equals("Ambient")) {
			error += "Invalid genre;";
		}
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
		
		//CHECK LENGTH
		if(songTitle.length() > 45)
			error += "Song title too long;";
		if(genre.length() > 45)
			error += "Genre name too long;";
		if(albumTitle.length() > 45)
			error += "Album title too long;";
		if(singer.length() > 45)
			error += "Singer name too long;";
		
		
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
		
		
		//CHECK FILES
		String contentTypeImg = albumImg.getContentType();

		//Check if the type is an image
		if(!contentTypeImg.startsWith("image"))
			error += "Image file not valid;";
		
		
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
		
		//Take the type of the music file uploaded
		String contentTypeMusic = songFile.getContentType();
		
		//Check the type of the music file uploaded
		if(!contentTypeMusic.startsWith("audio"))
			error += "Music file not valid";
				
		
		
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			String path = "/GoToHomePage";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;	
		}
		
		//*******CHECK CORRECTNESS END
		
		
		
		
		SongDAO songDao = new SongDAO(connection);
		
		int albumID=-1;
		String outputPathImg=null;
		
		try {
			albumID = songDao.getAlbum(albumTitle, singer);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An arror occurred uploading the db, retry later");
			return;
		}
		
		
		if(albumID == -1) {
			//the album doesn't exists
			String fileNameImg = Path.of(albumImg.getSubmittedFileName()).getFileName().toString();
			fileNameImg = user.getId() + "_" +albumTitle+ fileNameImg;
			fileNameImg = fileNameImg.replaceAll("\\s", fileNameImg);
			outputPathImg = imgFolderPath + fileNameImg;
			pubYear = Integer.parseInt(date);
			File fileImg = new File(outputPathImg);
			
			try (InputStream fileContent = albumImg.getInputStream()) {
				Files.copy(fileContent, fileImg.toPath());
			} catch (Exception e) {
				error += "Error in uploading the image;";
			}
			
		}
		
	
	
		
	
		//If an error occurred, redirect with errorMsg1 to the template engine  
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		

		
		//Take the name of the song uploaded
		String fileNameSong = Paths.get(songFile.getSubmittedFileName()).getFileName().toString();
		
		
		
		//Create the final part for music files adding the user id in the start to avoid error in case of duplicate name;
		fileNameSong = user.getId()  + "_" + fileNameSong;
		String outputPathSong = mp3FolderPath + fileNameSong;
		
		//Save the mp3 file
		File fileSong = new File(outputPathSong);
		
		try (InputStream fileContent = songFile.getInputStream()) {
			Files.copy(fileContent, fileSong.toPath());
		} catch (Exception e) {
			
			//se l'album non esisteva allora cancello tutto--> l'operazione è atomica
			if(albumID == -1) {
				File file = new File(outputPathImg);
				file.delete();
			}
			error += "Error in uploading the music file\n";
		}
		
		//If an error occurred, redirect with errorMsg1 to the template engine  
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			String path = "/GoToHomePage";

			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		//Now the data base can be updated
		
		SongDAO sDao = new SongDAO(connection);
		
		try {
			
			boolean result=false;
			if(albumID == -1) {
				result = sDao.createSongAndAlbum(user.getId() , songTitle, genre, albumTitle, singer, pubYear, outputPathImg , outputPathSong);

			}else {
				result = sDao.createSong(user.getId(),songTitle,genre,albumID,outputPathSong);
			}
			
			if(result == true) {
				String path = getServletContext().getContextPath() + "/GoToHomePage";
				response.sendRedirect(path);
			}
			else {
				//Delete uploaded file if something got wrong during the updating of the dataBase
				File file; 
				if(albumID == -1) {
					file = new File(outputPathImg);
					file.delete();
					
				}
				file = new File(outputPathSong);
				file.delete();
				
				error += "Impossible upload file in the database , try later";
				request.setAttribute("errorSong", error);
				String path = getServletContext().getContextPath() + "/GoToHomePage";

				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
				dispatcher.forward(request,response);
			}
			
		}catch(SQLException e) {
			//Delete uploaded file if something got wrong with the data base
			File file; 
			if(!(albumID == -1)) {
				
				file = new File(outputPathImg);
				file.delete();
				
			}
			file = new File(outputPathSong);
			file.delete();
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An arror occurred uploading the db, retry later");
		}
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





