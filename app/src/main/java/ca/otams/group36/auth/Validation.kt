package ca.otams.group36.auth

import android.util.Patterns

object Validation {
    @JvmStatic fun nonEmpty(s: String?) = !s.isNullOrBlank()
    @JvmStatic fun email(s: String?) = !s.isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(s).matches()
    @JvmStatic fun password(s: String?) = !s.isNullOrEmpty() && s.length >= 8
    @JvmStatic fun phone(s: String?) = s.isNullOrBlank() || s.matches(Regex("^\\+?[0-9]{7,15}$"))

    /** 逗号分隔的科目 → 去空格后的列表 */
    @JvmStatic fun parseSubjects(raw: String?) =
        (raw ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }

    /** 字符串转非负整数（失败返回 null） */
    @JvmStatic fun parseNonNegativeInt(raw: String?): Int? =
        raw?.trim()?.toIntOrNull()?.takeIf { it >= 0 }
}
