package it.polimi.tiw.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 * Servlet implementation class TestSQLInjection
 */
@WebServlet("/TestSQLInjection")
public class TestSQLInjection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestSQLInjection() {
        super();
        // TODO Auto-generated constructor stub
    }
    
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	String userID = req.getParameter("user");
    	String query = "SELECT * FROM User WHERE username = " + userID; // unsafe data inserted into the query
    	  ResultSet result = null;
    	  Statement statement = null;
    	  res.setContentType("text/plain");
    	  PrintWriter out = res.getWriter();
    	  out.println(query);
    	try { 
    	
    	statement = (Statement) connection.createStatement();
    	result =  statement.executeQuery(query); // query executed on the fly while(result.next()){
    	out.println(result.getString("username") +  " " + result.getString("email") + " " + result.getString("password"));
    	         
    	        } 
    	catch (SQLException e) {
    		
    	out.append("SQL ERROR IN INSECURE CODE"+e);
    	}
    	 finally { 
    		 try {
    			 result.close();
    		 }
    	     catch (Exception e1) {out.append("SQL RES ERROR");}
    	} }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
