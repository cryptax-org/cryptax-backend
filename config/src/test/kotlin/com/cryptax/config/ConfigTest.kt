package com.cryptax.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConfigTest {

    @Test
    fun `config profile local`() {
        // given
        val config = Config()

        // when
        val profile = config.profile

        // then
        assertThat(profile).isEqualTo("local")
    }
}
