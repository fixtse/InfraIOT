import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;

@WebServlet("/saluda")
public class SaludaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String nombre = req.getParameter("nombre");
        if (nombre == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Falta el parámetro nombre");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>Hola, " + nombre + "!</h1>");
        out.println("</body></html>");
    }
}
