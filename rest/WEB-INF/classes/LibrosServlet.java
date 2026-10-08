
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/libros")
public class LibrosServlet extends HttpServlet {

    private final Map<Integer, String> libros = new HashMap<>();

    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        libros.put(1, "El Quijote");
        libros.put(2, "Cien años de soledad");
        System.out.println("Servlet inicializado");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        resp.getWriter().println("Hola desde " + req.getContextPath());
        for (Map.Entry<Integer, String> libro : libros.entrySet()) {
            resp.getWriter().println("ID: " + libro.getKey() + ", Título: " + libro.getValue());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String id = req.getParameter("id");
        String titulo = req.getParameter("titulo");
        if (id != null && titulo != null) {

            if (libros.containsKey(Integer.parseInt(id))) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id already exists");
            } else {
                libros.put(Integer.parseInt(id), titulo);
                resp.getWriter().println("Libro agregado");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id or title is null");
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String titulo = req.getParameter("titulo");

        if (id != null && titulo != null && libros.containsKey(Integer.parseInt(id))) {
            libros.put(Integer.parseInt(id), titulo);
            resp.getWriter().println("Libro actualizado");
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id doesn't exists");
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        if (id != null && libros.containsKey(Integer.parseInt(id))) {
            libros.remove(Integer.parseInt(id));
            resp.getWriter().println("Libro eliminado");
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id doesn't exists");
        }
    }
}
