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

import org.thymeleaf.TemplateEngine;



/**
 * Servlet Filter implementation class RegistrationFilter
 */
@WebFilter()
public class RegistrationFilter extends HttpFilter implements Filter {
       
    private static final long serialVersionUID = 1L;
    
    TemplateEngine templateEngine=null;
	HttpServletRequest httpRequest=null;


	/**
     * @see HttpFilter#HttpFilter()
     */
    public RegistrationFilter() {
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
		httpRequest = (HttpServletRequest)request;

		
		
		String method = httpRequest.getMethod();
		
		request.setAttribute("error","");
		
		if(!method.equals("POST")) {
			request.setAttribute("error", "Method not available");
			
			chain.doFilter(request, response);
			return;
		}

		// pass the request along the filter chain
		String userName = request.getParameter("user");
		String password = request.getParameter("password");
		
		
		//check if the parameters are not empty or null
		if(userName == null || password == null || userName.isEmpty() || password.isEmpty()) {
			request.setAttribute("error", "Missing parameters");
			chain.doFilter(request, response);
			return;
		}
		
		String error="";
		
		
		//Check if the password contain at least one number and one special character and if it has a size bigger than 4
		if (!(password.contains("0") || password.contains("1") || password.contains("2") || password.contains("3")
				|| password.contains("4") || password.contains("5") || password.contains("6") || password.contains("7")
				|| password.contains("8") || password.contains("9"))
				|| !(password.contains("#") || password.contains("@") || password.contains("&"))
				|| password.contains("$") || password.length() < 4) {
			
		
			error += "Password must contain at least:4 character,1 number and 1 of the following @,#,&,$ ;";
			request.setAttribute("error", error);

			chain.doFilter(request, response);
			return;
		}

		// Check if the userName is too long
		if (userName.length() > 45)
			{
			error += "UserName too long;";
			request.setAttribute("error", error);

			chain.doFilter(request, response);
			return;

			}

		// Check if the password is too long
		if (password.length() > 45) {
			
		
			error += "Password too long;";
			request.setAttribute("error", error);

			chain.doFilter(request, response);
			return;
		}
		
	
		
		
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {		
;
		}

}
