package com.cryptax.controller

import com.cryptax.domain.entity.Source
import io.reactivex.Single

class SourceController {
    fun getAllSources(): Single<List<String>> {
        return Single.just(Source.values().map { source -> source.name.toLowerCase() })
    }
}
