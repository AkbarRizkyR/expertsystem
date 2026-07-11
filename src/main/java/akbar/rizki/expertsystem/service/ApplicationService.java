package akbar.rizki.expertsystem.service;

import akbar.rizki.expertsystem.dto.ApplicationRequest;
import akbar.rizki.expertsystem.entity.Application;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class ApplicationService {

    @Inject
    CurrentUser currentUser;

    @Transactional
    public Application create(ApplicationRequest request) {
        Application app = new Application();
        app.name = request.name;
        app.description = request.description;
        app.ownerId = currentUser.id();
        app.persist();
        return app;
    }

    public List<Application> list() {
        if (currentUser.isAdmin()) {
            return Application.listAll();
        }
        return Application.list("ownerId", currentUser.id());
    }

    /** Ambil aplikasi sekaligus pastikan pemanggil berhak (owner atau ADMIN). */
    public Application getOwned(Long id) {
        Application app = Application.findById(id);
        if (app == null) {
            throw new NotFoundException("Aplikasi tidak ditemukan: " + id);
        }
        if (!currentUser.isAdmin() && !app.ownerId.equals(currentUser.id())) {
            throw new ForbiddenException("Anda tidak punya akses ke aplikasi ini");
        }
        return app;
    }

    @Transactional
    public Application update(Long id, ApplicationRequest request) {
        Application app = getOwned(id);
        app.name = request.name;
        app.description = request.description;
        app.persist();
        return app;
    }

    @Transactional
    public void delete(Long id) {
        Application app = getOwned(id);
        app.delete();
    }
}
