package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;


/**
 * Servlet Filter implementation class CreatePlaylistFilter
 */
@WebFilter()
public class CreatePlaylistFilter extends HttpFilter implements Filter {
       
    private static final long serialVersionUID = 1L;

	/**
     * @see HttpFilter#HttpFilter()
     */
    public CreatePlaylistFilter() {
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
		// TODO Auto-generated method stub
		// place your code here
		
		HttpServletRequest httpRequest = (HttpServletRequest)request;
	 
	
		
		String method = httpRequest.getMethod();
		
		
		if(!method.equals("POST")) {
			
			System.out.println("Inside");
			
			request.setAttribute("error", "This form only supports POST methods");
			chain.doFilter(request, response);
			return;
			
		}
		
		String title = request.getParameter("title");
		String[] songs = request.getParameterValues("canzoni");

		String error = "";
		

		if(title == null || title.isEmpty())
			error += "Title is empty";
		else if(title.length() > 45)
			error += "Title is too long";
		if(!error.equals("")){
			request.setAttribute("error", error);
			chain.doFilter(request, response);
			return;
		}
		
		if(songs == null) {
			error+="Playlist must include at least one song!\nIf there are no songs availabe create one !";
		}
		if(!error.equals("")){
			request.setAttribute("error", error);
			chain.doFilter(request, response);
			return;
		}

		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
