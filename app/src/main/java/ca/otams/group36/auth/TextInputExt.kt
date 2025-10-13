@file:JvmName("TextInputExt")  // Java 中类名是 TextInputExt
package ca.otams.group36.auth

import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/** Java 调用：TextInputExt.clearErrorOnChange(til, et) */
@JvmName("clearErrorOnChange")
fun clearErrorOnChange(til: TextInputLayout, et: TextInputEditText) {
    et.doAfterTextChanged { til.error = null }
}
