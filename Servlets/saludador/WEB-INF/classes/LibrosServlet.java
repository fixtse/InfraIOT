
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/libros")
public class LibrosServlet extends HttpServlet {

    private final Map<Integer, String> libros = new HashMap<>();

    @Override
    public void init() {
        libros.put(1, "El Quijote");
        libros.put(2, "Cien años de soledad");
        System.out.println("Servlet iniciado");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
    }

    @Override
    public void destroy() {
        System.out.println("Servlet destruido");
    }
}
