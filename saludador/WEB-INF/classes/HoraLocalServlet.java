import jakarta.servlet.*; // In Tomcat 9 javax.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.time.LocalTime;

public class HoraLocalServlet extends HttpServlet {
@Override // Evita que escribamos mal la declaración de doGet
protected void doGet(HttpServletRequest req, HttpServletResponse resp)

throws IOException {
		resp.setContentType("text/html");
		resp.setContentType("text/html; charset=UTF-8");
		PrintWriter out = resp.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head><title>Hora Local</title></head>");
		out.println("<body>");
		out.println("<h1>Hora Local</h1>");
		out.println("<p>La hora actual es: " + LocalTime.now() +"</p>");
		out.println("</body>");
		out.println("</html>");
	}
}
