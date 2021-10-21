package lk.dep.cisco.authbackend.service;

import lk.dep.cisco.authbackend.dto.UserDTO;

import java.sql.*;

public class UserService {

    private Connection connection;

    public UserService(Connection connection){
        this.connection = connection;
    }

    public void saveUser(UserDTO user){
        try {
            PreparedStatement stm = connection.prepareStatement("INSERT INTO user VALUES (?,?,?)");
            stm.setString(1, user.getUsername());
            stm.setString(2, user.getFullName());
            stm.setString(3, user.getPassword());

            if(stm.executeUpdate() != 1){
                throw new RuntimeException("Failed to save the user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UserDTO authenticate(String username, String password){
        try {
            Statement stm = connection.createStatement();
            String sql = String.format("SELECT full_name FROM user WHERE username=%s AND password=%s", username, password);
            ResultSet rst = stm.executeQuery(sql);
            if(rst.next()){
                return new UserDTO(username, password, rst.getString("full_name"));
            }
            throw new RuntimeException("Invalid logIn credentials");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to authenticate", e);
        }
    }
}
