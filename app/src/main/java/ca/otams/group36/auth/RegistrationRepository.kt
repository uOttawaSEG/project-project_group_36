package ca.otams.group36.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationRepository {

    interface RegistrationCallback {
        fun onSuccess()
        fun onError(message: String)
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /** 学生注册：Auth 建账号 -> Firestore 写 users/{uid} */
    @JvmOverloads
    fun registerStudent(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?,
        program: String,
        cb: RegistrationCallback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: run {
                    cb.onError("Auth error: uid is null"); return@addOnSuccessListener
                }
                // 可选：发送邮箱验证
                try { authResult.user?.sendEmailVerification() } catch (_: Exception) {}

                val user = hashMapOf(
                    "email" to email,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "phone" to (phone ?: ""),
                    "program" to program,
                    "approved" to false,
                    "role" to "Student"
                )

                db.collection("users").document(uid)
                    .set(user)
                    .addOnSuccessListener { cb.onSuccess() }
                    .addOnFailureListener { e -> cb.onError("Firestore error: ${e.message}") }
            }
            .addOnFailureListener { e ->
                if (e is FirebaseAuthUserCollisionException) cb.onError("Email already in use")
                else cb.onError("Auth error: ${e.message}")
            }
    }

    /** 导师注册：Auth 建账号 -> Firestore 写 users/{uid}（包含 subjects/years/bio） */
    fun registerTutor(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?,
        subjects: List<String>,
        years: Int,
        bio: String?,
        cb: RegistrationCallback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: run {
                    cb.onError("Auth error: uid is null"); return@addOnSuccessListener
                }
                try { authResult.user?.sendEmailVerification() } catch (_: Exception) {}

                val user = hashMapOf(
                    "email" to email,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "phone" to (phone ?: ""),
                    "subjects" to subjects,
                    "years" to years,     // 注意：是 years (Int)，不是 years_hint
                    "bio" to (bio ?: ""),
                    "approved" to false,
                    "role" to "Tutor"
                )

                db.collection("users").document(uid)
                    .set(user)
                    .addOnSuccessListener { cb.onSuccess() }
                    .addOnFailureListener { e -> cb.onError("Firestore error: ${e.message}") }
            }
            .addOnFailureListener { e ->
                if (e is FirebaseAuthUserCollisionException) cb.onError("Email already in use")
                else cb.onError("Auth error: ${e.message}")
            }
    }
}
