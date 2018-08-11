package com.cryptax.domain.port

interface Pingable {
    // TODO: make that method reactive
    fun ping(): Boolean
}
