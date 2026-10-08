package com.v2ray.ang.handler

import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.UrlContentRequest
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.fmt.Hysteria2Fmt
import com.v2ray.ang.fmt.ShadowsocksFmt
import com.v2ray.ang.fmt.SocksFmt
import com.v2ray.ang.fmt.TrojanFmt
import com.v2ray.ang.fmt.VlessFmt
import com.v2ray.ang.fmt.VmessFmt
import com.v2ray.ang.fmt.WireguardFmt
import com.v2ray.ang.util.HttpUtil
import com.v2ray.ang.util.LogUtil
import com.v2ray.ang.util.Utils

object HardcodedConfig {
    const val GUID = AppConfig.HARDCODED_SERVER_GUID
    const val URL = AppConfig.HARDCODED_VLESS_URL

    fun getCurrentConfigUrl(): String {
        val cached = MmkvManager.getCachedConfigUrl()
        if (!cached.isNullOrBlank() && parseProfileFromUrl(cached) != null) {
            return cached
        }
        return URL
    }

    fun parseProfileFromUrl(rawUrl: String): ProfileItem? {
        val trimmed = rawUrl.trim()
        val direct = parseScheme(trimmed)
        if (direct != null) return direct

        val decoded = tryDecodeBase64(trimmed)
        if (decoded.isNotBlank()) {
            val candidate = decoded.lines()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() && !it.startsWith("#") }
            if (candidate != null) {
                val parsed = parseScheme(candidate)
                if (parsed != null) return parsed
            }
        }
        return null
    }

    private fun parseScheme(str: String): ProfileItem? {
        val clean = str.trim()
        val profile = when {
            clean.startsWith("vless://", ignoreCase = true) -> VlessFmt.parse(clean)
            clean.startsWith("vmess://", ignoreCase = true) -> VmessFmt.parse(clean)
            clean.startsWith("trojan://", ignoreCase = true) -> TrojanFmt.parse(clean)
            clean.startsWith("ss://", ignoreCase = true) -> ShadowsocksFmt.parse(clean)
            clean.startsWith("socks://", ignoreCase = true) -> SocksFmt.parse(clean)
            clean.startsWith("hy2://", ignoreCase = true) || clean.startsWith("hysteria2://", ignoreCase = true) -> Hysteria2Fmt.parse(clean)
            clean.startsWith("wireguard://", ignoreCase = true) -> WireguardFmt.parse(clean)
            else -> null
        }
        if (profile != null && profile.remarks.isNullOrBlank()) {
            profile.remarks = "ToxicNet Reality"
        }
        return profile
    }

    private fun tryDecodeBase64(text: String): String {
        return try {
            val bytes = java.util.Base64.getDecoder().decode(text)
            String(bytes, Charsets.UTF_8)
        } catch (_: Throwable) {
            try {
                Utils.decode(text).orEmpty()
            } catch (_: Throwable) {
                ""
            }
        }
    }

    fun createProfile(): ProfileItem {
        val currentUrl = getCurrentConfigUrl()
        val profile = parseProfileFromUrl(currentUrl) ?: parseProfileFromUrl(URL)
        return profile ?: error("Failed to parse configuration")
    }

    fun fetchAndApplyRemoteConfig(): Boolean {
        val urls = listOf(
            AppConfig.REMOTE_CONFIG_URL,
            AppConfig.REMOTE_CONFIG_MIRROR_URL
        )

        for (remoteUrl in urls) {
            try {
                val content = HttpUtil.getUrlContent(UrlContentRequest(url = remoteUrl, timeout = 10000))
                if (!content.isNullOrBlank()) {
                    val candidate = content.lines()
                        .map { it.trim() }
                        .firstOrNull { it.isNotEmpty() && !it.startsWith("#") }

                    if (!candidate.isNullOrBlank()) {
                        val parsed = parseProfileFromUrl(candidate)
                        if (parsed != null) {
                            val current = MmkvManager.getCachedConfigUrl()
                            if (current != candidate) {
                                MmkvManager.setCachedConfigUrl(candidate)
                                LogUtil.i(AppConfig.TAG, "Successfully updated remote config from $remoteUrl")
                            }
                            return true
                        }
                    }
                }
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Failed fetching remote config from $remoteUrl", e)
            }
        }
        return false
    }
}
