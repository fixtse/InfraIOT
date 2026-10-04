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
            req.getRequestDispatcher("/saluda.html").forward(req, resp);
            return;
        }

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>Hola, " + (nombre != null ? nombre : "anónimo")

                + "!</h1>");
        out.println("</body></html>");
    }
}
