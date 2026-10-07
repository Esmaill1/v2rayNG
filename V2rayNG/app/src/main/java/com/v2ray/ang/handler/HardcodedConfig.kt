package com.v2ray.ang.handler

import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.fmt.VlessFmt

object HardcodedConfig {
    const val GUID = AppConfig.HARDCODED_SERVER_GUID
    const val URL = AppConfig.HARDCODED_VLESS_URL

    fun createProfile(): ProfileItem {
        val profile = VlessFmt.parse(URL) ?: error("Failed to parse hardcoded VLESS configuration")
        profile.remarks = "ToxicNet Reality"
        return profile
    }
}
