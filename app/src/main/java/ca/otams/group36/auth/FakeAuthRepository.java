package ca.otams.group36.auth;

import java.util.HashMap;
import java.util.Map;

/** In-memory stub; replace with real DB later. */
public class FakeAuthRepository implements AuthRepository {
    private final Map<String, UserRow> users = new HashMap<>();

    public FakeAuthRepository() {
        // Seed admin account for testing
        users.put("admin@otams.ca", new UserRow("admin123", Role.ADMIN));
        // Example extra accounts for later integration:
        // users.put("student@uott.ca", new UserRow("123456", Role.STUDENT));
        // users.put("tutor@uott.ca",   new UserRow("abcdef", Role.TUTOR));
    }

    @Override
    public Role login(String email, String password) throws Exception {
        UserRow row = users.get(email);
        if (row == null) throw new Exception("User not found");
        if (!row.password.equals(password)) throw new Exception("Invalid credentials");
        return row.role;
    }

    @Override
    public void logout() { /* no-op for stub */ }

    private static class UserRow {
        final String password; final Role role;
        UserRow(String p, Role r) { this.password = p; this.role = r; }
    }
}
