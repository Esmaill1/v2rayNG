package com.v2ray.ang.handler

import com.v2ray.ang.AppConfig
import com.v2ray.ang.enums.EConfigType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class HardcodedConfigTest {

    @Test
    fun testHardcodedConfigParsing() {
        val profile = HardcodedConfig.createProfile()

        assertNotNull(profile)
        assertEquals(EConfigType.VLESS, profile.configType)
        assertEquals("de-2.toxicnet.top", profile.server)
        assertEquals("28394", profile.serverPort)
        assertEquals("58dceae9-b361-4b88-bf14-04661dc8aee4", profile.password)
        assertEquals(AppConfig.REALITY, profile.security)
        assertEquals("ea.com", profile.sni)
        assertEquals("random", profile.fingerPrint)
        assertEquals("Y3jq8s1mgrqg6HGKzUh64OtolQNMRMRg18CPVmYZmEc", profile.publicKey)
        assertEquals("6ebb2d5b12c735", profile.shortId)
        assertEquals("/", profile.spiderX)
        assertEquals("tcp", profile.network)
        assertEquals("none", profile.method)
    }

    @Test
    fun testMmkvManagerServesHardcodedServer() {
        assertEquals(AppConfig.HARDCODED_SERVER_GUID, MmkvManager.getSelectServer())
        assertEquals(listOf(AppConfig.HARDCODED_SERVER_GUID), MmkvManager.decodeServerList(""))
        assertEquals(listOf(AppConfig.HARDCODED_SERVER_GUID), MmkvManager.decodeAllServerList())

        val profile = MmkvManager.decodeServerConfig("any-guid")
        assertNotNull(profile)
        assertEquals("de-2.toxicnet.top", profile?.server)
    }
}
