package ca.otams.group36.auth;

/** Abstraction for authentication backend. */
public interface AuthRepository {
    /** Returns the role if login succeeds; throws on failure. */
    Role login(String email, String password) throws Exception;
    /** Clears session/server tokens if needed. */
    void logout();
}