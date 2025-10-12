package ca.otams.group36.auth;

import android.content.Context;
import android.content.SharedPreferences;

/** Minimal session storage using SharedPreferences. */
public class SessionManager {
    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        this.sp = ctx.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    /** Persist current role. */
    public void save(Role role) { sp.edit().putString("role", role.name()).apply(); }

    /** Load role if present; null if not logged in. */
    public Role load() {
        String v = sp.getString("role", null);
        return v == null ? null : Role.valueOf(v);
    }

    /** Clear all session data. */
    public void clear() { sp.edit().clear().apply(); }
}
