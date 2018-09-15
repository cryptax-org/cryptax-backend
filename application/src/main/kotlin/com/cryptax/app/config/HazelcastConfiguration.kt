package com.cryptax.app.config

import com.hazelcast.config.Config
import com.hazelcast.config.EvictionPolicy
import com.hazelcast.config.MapConfig
import com.hazelcast.config.MaxSizeConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HazelcastConfiguration {

    @Bean
    fun hazelCastConfig(): Config {
        val config = Config()
        config.setInstanceName("hazelcast-instance")
            .addMapConfig(MapConfig()
                .setName("price")
                .setMaxSizeConfig(MaxSizeConfig(600, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setTimeToLiveSeconds(-1))
        return config
    }
}
