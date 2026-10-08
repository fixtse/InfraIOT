import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String AUTHENTICATED = "authenticated";

    private String username;
    private String password;

    @Override
    public void init() throws ServletException {
        username = getServletConfig().getInitParameter("username");
        password = getServletConfig().getInitParameter("password");
        if (username == null || password == null) {
            throw new ServletException("Authentication credentials are not configured");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if ("logout".equals(req.getParameter("action"))) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\"><head><meta charset=\"UTF-8\">"
                    + "<title>Authentication</title></head><body>");
            if (isAuthenticated(req)) {
                out.println("<h1>Welcome</h1>");
                out.println("<p>You are authenticated.</p>");
                out.println("<a href=\"auth?action=logout\">Log out</a>");
            } else {
                out.println("<h1>Log in</h1>");
                out.println("<form method=\"post\" action=\"auth\">");
                out.println("<label>Username <input name=\"username\" required></label><br>");
                out.println("<label>Password <input type=\"password\" name=\"password\" required></label><br>");
                out.println("<button type=\"submit\">Log in</button>");
                out.println("</form>");
                if ("invalid".equals(req.getParameter("error"))) {
                    out.println("<p role=\"alert\">Invalid username or password.</p>");
                }
            }
            out.println("</body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String submittedUsername = req.getParameter("username");
        String submittedPassword = req.getParameter("password");

        if (username.equals(submittedUsername) && password.equals(submittedPassword)) {
            HttpSession previousSession = req.getSession(false);
            if (previousSession != null) {
                previousSession.invalidate();
            }
            req.getSession(true).setAttribute(AUTHENTICATED, Boolean.TRUE);
            resp.sendRedirect(req.getContextPath() + "/auth");
        } else {
            resp.sendRedirect(req.getContextPath() + "/auth?error=invalid");
        }
    }

    private boolean isAuthenticated(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute(AUTHENTICATED));
    }
}
