package lk.dep.cisco.authbackend.api;

import jakarta.annotation.Resource;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.dep.cisco.authbackend.dto.StudentDTO;
import lk.dep.cisco.authbackend.security.SecurityContext;
import lk.dep.cisco.authbackend.service.StudentService;
import lk.dep.cisco.authbackend.service.UserService;


import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "StudentServlet", value = "/students", loadOnStartup = 0)
public class StudentServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/cp")
    private DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Connection connection = dataSource.getConnection()) {
            StudentService studentService = new StudentService(connection);
            List<StudentDTO> students = studentService.getAllStudents();
            resp.setContentType("application/json");
            resp.getWriter().println(JsonbBuilder.create().toJson(students));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType()== null || !req.getContentType().startsWith("application/json")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid request (Only accept JSON)");
            return;
        }
        try {
            StudentDTO studentDTO = JsonbBuilder.create().fromJson(req.getReader(), StudentDTO.class);

            if (studentDTO.getId() != null ){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID can't be specified when saving");
                return;
            }else if (studentDTO.getName() == null || !studentDTO.getName().trim().matches("^[A-Za-z ]+$")){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student name");
                return;
            }else if(studentDTO.getAddress() == null || studentDTO.getAddress().trim().length() < 3){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student address");
                return;
            }
            try (Connection connection = dataSource.getConnection()) {
                /* Todo: remove in the future */
                SecurityContext.setPrincipal(new UserService(connection).authenticate("admin", "admin"));
                StudentService studentService = new StudentService(connection);
                String id = studentService.saveStudent(studentDTO);
                resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().println(JsonbBuilder.create().toJson(id));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (JsonbException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");;
            e.printStackTrace();
        }
    }
}
