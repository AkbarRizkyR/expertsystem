package akbar.rizki.expertsystem.service;

import akbar.rizki.expertsystem.dto.CreateUserRequest;
import akbar.rizki.expertsystem.entity.Role;
import akbar.rizki.expertsystem.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class UserService {

    public List<User> list() {
        return User.listAll();
    }

    @Transactional
    public User create(CreateUserRequest request) {
        if (User.findByEmail(request.email) != null) {
            throw new BadRequestException("Email sudah terdaftar: " + request.email);
        }

        Role role = Role.findById(request.roleId);
        if (role == null) {
            throw new NotFoundException("Role tidak ditemukan: " + request.roleId);
        }

        User user = new User();
        user.name = request.name;
        user.email = request.email;
        user.passwordHash = BcryptUtil.bcryptHash(request.password);
        user.role = role;
        user.persist();
        return user;
    }
}
