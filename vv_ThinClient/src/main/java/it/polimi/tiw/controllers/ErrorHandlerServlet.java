package it.polimi.tiw.controllers;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/ErrorHandler")

public class ErrorHandlerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processError(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processError(request, response);
    }

    private void processError(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Personalizza la logica di gestione degli errori qui
        // Puoi ottenere informazioni sull'errore da request.getAttribute(...)
        // Ad esempio, Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        
        // Esegue un inoltro a una pagina di errore personalizzata
    	HttpSession session = request.getSession(false);
		
		//Invalidate session
		if (session != null) {
			session.invalidate();
		}
		
		//Redirect to the login page
		String path = getServletContext().getContextPath() +  "/error.html";
		response.sendRedirect(path);
    }
}
