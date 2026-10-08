
import java.io.IOException;
import java.util.Base64;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/privado")
public class AuthenticationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (req.getHeader("Authorization") != null && req.getHeader("Authorization").substring(0, 5).contains("Basic")) {
            String base64Credentials = req.getHeader("Authorization").substring(6);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes);
            String[] partes = credentials.split(":");

            if (partes[0].equals("sergio") && partes[1].equals("1234")) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }

        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setHeader("WWW-Authenticate", "Basic realm=alumnos");
        }

        //resp.setContentType("text/html; charset=UTF-8");
        //resp.setStatus(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
        //req.getHeader("Authorization");
    }
}
