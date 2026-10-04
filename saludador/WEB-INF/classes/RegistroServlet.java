import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

public class RegistroServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        resp.setContentType("text/html; charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html><head><meta charset=\"UTF-8\"><title>Registro recibido</title></head><body>");
            out.println("<h1>Datos recibidos</h1>");
            out.println("<ul>");

            Enumeration<String> nombres = req.getParameterNames();
            while (nombres.hasMoreElements()) {
                String nombre = nombres.nextElement();
                String valor = req.getParameter(nombre);
                out.println("<li><strong>" + escaparHtml(nombre) + ":</strong> "
                        + escaparHtml(valor) + "</li>");
            }

            out.println("</ul>");
            out.println("</body></html>");
        }
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
