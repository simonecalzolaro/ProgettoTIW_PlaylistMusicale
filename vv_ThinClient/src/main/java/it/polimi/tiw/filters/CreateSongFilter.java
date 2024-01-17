package it.polimi.tiw.filters;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;


/**
 * Servlet Filter implementation class CreateSongFilter
 */
@WebFilter("")
public class CreateSongFilter extends HttpFilter implements Filter {
       
    private static final long serialVersionUID = 1L;

	/**
     * @see HttpFilter#HttpFilter()
     */
    public CreateSongFilter() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest)request;
	 
		
		String method = httpRequest.getMethod();
		
		if(!method.equals("POST")) {
			
			request.setAttribute("errorSong", "This form only supports POST methods");
			chain.doFilter(httpRequest, response);
			
			return;
			
		}
		
	
		
		String title = request.getParameter("title");
		String genre = request.getParameter("genre");
		String albumTitle = request.getParameter("albumTitle");
		String singer = request.getParameter("singer");
		String date = request.getParameter("date");
		Part albumImg = httpRequest.getPart("albumImg");
		Part songFile = httpRequest.getPart("songFile");
		
		
		//Check if the parameters are not empty or null
		if(title == null || title.isEmpty() || genre == null || genre.isEmpty() || albumTitle == null || albumTitle.isEmpty() || singer == null || singer.isEmpty() || date == null || date.isEmpty() || albumImg == null || songFile == null ) {
			
		
			request.setAttribute("errorSong", "Missing parameters");
			chain.doFilter(httpRequest, response);
			return;
		}
		
		String error = "";
		
		int pubYear=0;
		
		try {
			pubYear = Integer.parseInt(date);
			
			//Take the current year
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			
			//Check if the publicationYear is not bigger than the current year
			if(pubYear > currentYear)
				error += "Invalid date;";
		}catch(NumberFormatException e) {
			error += "Date not valid;";
		}
		
		
		if(!error.equals("")) {
			request.setAttribute("error1", error);
			chain.doFilter(httpRequest, response);
			return;
		}
		
		//Check if the genre is valid
		if(!genre.equals("Reggae") && !genre.equals("Pop") && !genre.equals("Rock") && !genre.equals("Rap") && !genre.equals("Hip_hop") && !genre.equals("Classical") && !genre.equals("Ambient"))  {
			error += "Invalid genre;";
		}
		
		//Check if some input are too long
		if(title.length() > 45)
			error += "Song title too long;";
		if(genre.length() > 45)
			error += "Genre name too long;";
		if(albumTitle.length() > 45)
			error += "Album title too long;";
		if(singer.length() > 45)
			error += "Singer name too long;";
		
		
		
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			chain.doFilter(httpRequest, response);
			return;
		}
		
		String contentTypeImg = albumImg.getContentType();

		//Check if the type is an image
		if(!contentTypeImg.startsWith("image"))
			error += "Image file not valid;";
		
		
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			chain.doFilter(httpRequest, response);
			return;
		}
		
		
		//Take the type of the music file uploaded
		String contentTypeMusic = songFile.getContentType();
		
		//Check the type of the music file uploaded
		if(!contentTypeMusic.startsWith("audio"))
			error += "Music file not valid";
				
		
		
		if(!error.equals("")) {
			request.setAttribute("errorSong", error);
			chain.doFilter(httpRequest, response);
			return;
		}

		
	
	
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
