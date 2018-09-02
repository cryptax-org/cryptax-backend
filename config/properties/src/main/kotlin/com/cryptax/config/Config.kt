package com.cryptax.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule

abstract class Config(private val overrideProfile: String? = null) {

    val profile: String by lazy { profile() }

    val properties: AppProps = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule())
        .readValue(Config::class.java.classLoader.getResourceAsStream("config-$profile.yml"), AppProps::class.java)

    private fun profile(): String {
        if (overrideProfile != null) return overrideProfile
        return Companion.profile()
    }

    companion object {
        fun profile(): String {
            val profileEnv = System.getenv("PROFILE")
            return profileEnv ?: return "local"
        }
    }
}

class DefaultConfig : Config()
