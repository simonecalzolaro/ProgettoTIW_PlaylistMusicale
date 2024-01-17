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
 * Servlet Filter implementation class AuthenticationFilter
 */
@WebFilter()
public class AuthenticationFilter extends HttpFilter implements Filter {
       
    private static final long serialVersionUID = 1L;


	/**
     * @see HttpFilter#HttpFilter()
     */
    public AuthenticationFilter() {
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
			request.setAttribute("error", "Method not available");
			
			chain.doFilter(request, response);
			return;
		}
		
		String userName = request.getParameter("user");
		String password = request.getParameter("password");
		
		//Check if the parameters are not empty or null
		if(userName == null || password == null || userName.isEmpty() || password.isEmpty()) {
			
		
			request.setAttribute("error", "Missing parameters");			
			chain.doFilter(request, response);
			return;
		}

		
	
	
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		
}}