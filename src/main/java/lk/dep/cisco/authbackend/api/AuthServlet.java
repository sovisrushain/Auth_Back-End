package lk.dep.cisco.authbackend.api;

import jakarta.annotation.Resource;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.dep.cisco.authbackend.dto.UserDTO;
import lk.dep.cisco.authbackend.security.SecurityContext;
import lk.dep.cisco.authbackend.service.UserService;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "AuthServlet", value = "/authenticate", loadOnStartup = 0)
public class AuthServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/cp")
    private DataSource dataSource;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("application/x-www-form-urlencoded")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request (Not a valid content-type)");
            return;
        }
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username == null || password == null){
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad login credentials");
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            UserService userService = new UserService(connection);
            try {
                UserDTO user = userService.authenticate(username, password);
                SecurityContext.setPrincipal(user);
                resp.setContentType("application/json");
                resp.getWriter().println(JsonbBuilder.create().toJson(user));
            }catch (RuntimeException e){
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad login credentials");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
