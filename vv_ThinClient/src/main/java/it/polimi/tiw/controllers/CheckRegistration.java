package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.dao.UserDAO;

@WebServlet("/Registration")
public class CheckRegistration extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
	public CheckRegistration() {
		super();
	}
	
	public void init() {
		ServletContext context = getServletContext();
		
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
		try {			
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url , user , password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
	    }
	}
	
	protected void doGet(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException{
		String path = "registrationPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("invalidMethod", "Error! Perform a valid method to continue!" );
		templateEngine.process(path, ctx, response.getWriter());
		return;
	}
	
	protected void doPost(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException{
		
		
		//****CORRECTNESS CHECK ARE
		String method = request.getMethod();

		
		
		
		//CHECK METHOD
		if(!method.equals("POST")) {
			String path = "registrationPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("invalidMethod", "Error! Perform a valid method to continue!" );
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		String userName = request.getParameter("user");
		String password = request.getParameter("password");
		String error="";
		
		
		
		//CHECK PARAMETERS
		if (userName == null || password == null || userName.isEmpty() || password.isEmpty()) {
			String path = "registrationPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("missingParameters", "Missing parameters!" );
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		
		//CHECK PASSWORD
		if (!(password.contains("0") || password.contains("1") || password.contains("2") || password.contains("3")
				|| password.contains("4") || password.contains("5") || password.contains("6") || password.contains("7")
				|| password.contains("8") || password.contains("9"))
				|| !(password.contains("#") || password.contains("@") || password.contains("&"))
				|| password.contains("$") || password.length() < 4) {
			
		
			String path = "registrationPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("wrongPassword", "Password must contain at least:4 character,1 number and 1 of the following @,#,&,$" );
			templateEngine.process(path, ctx, response.getWriter());
			return;
			
		}	
		
		
		
		//CHECK PARAMETERS LENGHT
		if (userName.length() > 45) {
			String path = "registrationPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("wrongUsername", "Username's too long! Choose another one" );
			templateEngine.process(path, ctx, response.getWriter());
			return;

		}
		
		
		// Check if the password is too long
		if (password.length() > 45) {
			
			String path = "registrationPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("wrongPassword", "Password's too long! Choose another one" );
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		
		
		//*******END CORRECTNESS CHECK AREA --> Do the same work that the filters did before
		
		
		boolean result = false;
		
		
		

		
		UserDAO userDao = new UserDAO(connection);
		
		try {
			result = userDao.addUser(userName,password);
			
			if(result == true) {
				//Redirect to the login page
				String path = "loginPage.html";
				ServletContext servletContext = getServletContext();
				final WebContext cty = new WebContext(request, response, servletContext, request.getLocale());
				cty.setVariable("error", "You are registred successfully. Now you can login!");
				templateEngine.process(path, cty, response.getWriter());
				return;
			}
			else {
				String path = "registrationPage.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctz = new WebContext(request, response, servletContext, request.getLocale());
				error += "UserName is not available";
				ctz.setVariable("error", error);
				templateEngine.process(path, ctz, response.getWriter());
				return;
			}
		}catch(SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue with DB");
			return;
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