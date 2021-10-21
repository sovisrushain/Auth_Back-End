package lk.dep.cisco.authbackend.api;

import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.dep.cisco.authbackend.dto.UserDTO;
import lk.dep.cisco.authbackend.service.UserService;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "UserServlet", value = "/users", loadOnStartup = 0)
public class UserServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/cp")
    private DataSource dataSource;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().startsWith("application/json")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Request (Only support JSON)");
            return;
        }
        try {
            UserDTO userDTO = JsonbBuilder.create().fromJson(req.getReader(), UserDTO.class);
            if (userDTO.getUsername() == null || userDTO.getUsername().trim().length() < 3) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid username");
                return;
            } else if (userDTO.getFullName() == null || !userDTO.getFullName().trim().matches("^[A-Za-z ]+$")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid full name");
                return;
            } else if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid password");
                return;
            }
            try (Connection connection = dataSource.getConnection()) {
                UserService userService = new UserService(connection);
                userService.saveUser(userDTO);
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (JsonbException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
        }
    }
}
