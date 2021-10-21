package lk.dep.cisco.authbackend.security;

import lk.dep.cisco.authbackend.dto.UserDTO;

public class SecurityContext {

    private static ThreadLocal<UserDTO> principal = new ThreadLocal<>();

    public static UserDTO getPrincipal() {
        if(principal.get() == null) throw new RuntimeException("There is no principal");
        return principal.get();
    }

    public static void setPrincipal(UserDTO principal) {
        SecurityContext.principal.set(principal);
    }
}
