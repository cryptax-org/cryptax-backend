package com.cryptax.domain.entity

// Consider storing that into the db
enum class Currency constructor(internal val codes: List<Code>, val fullName: String, val symbol: String, val type: Type) {
    BTC(listOf(Code("BTC")), "Bitcoin", "฿", Type.CRYPTO),
    ETH(listOf(Code("ETH")), "Ethereum", "Ξ", Type.CRYPTO),
    GRS(listOf(Code("GRS")), "Groestlcoin", "GRS", Type.CRYPTO),
    LTC(listOf(Code("LTC")), "Litecoin", "Ł", Type.CRYPTO),
    VTC(listOf(Code("VTC")), "Vertcoin", "VTC", Type.CRYPTO),
    ETHOS(listOf(Code("ETHOS"), Code("BQX", false)), "Ethos", "ETHOS", Type.CRYPTO),
    CARDANO(listOf(Code("ADA")), "Cardano", "ADA", Type.CRYPTO),
    POWER_LEDGER(listOf(Code("POWR")), "Power Ledger", "POWR", Type.CRYPTO),
    ICON(listOf(Code("ICX")), "Icon", "ICX", Type.CRYPTO),
    MONERO(listOf(Code("XMR")), "Monero", "XMR", Type.CRYPTO),
    NEO(listOf(Code("NEO")), "NEO", "NEO", Type.CRYPTO),
    EOS(listOf(Code("EOS")), "EOS", "EOS", Type.CRYPTO),
    STEEM(listOf(Code("STEEM")), "Steem", "STEEM", Type.CRYPTO),
    KOMODO(listOf(Code("KMD")), "Komodo", "KMD", Type.CRYPTO),
    ARK(listOf(Code("ARK")), "Ark", "ARK", Type.CRYPTO),
    WALTON(listOf(Code("WTC")), "Walton", "WTC", Type.CRYPTO),
    NAV(listOf(Code("NAV")), "Nav Coin", "NAV", Type.CRYPTO),
    UTRUST(listOf(Code("UTK")), "Utrust", "UTK", Type.CRYPTO),

    USD(listOf(Code("USD")), "United States Dollar", "$", Type.FIAT),
    EUR(listOf(Code("EUR")), "Euro", "€", Type.FIAT),
    UNKNOWN(listOf(Code("UNKNOWN")), "Unknown", "U", Type.FIAT);

    val code get() = codes.find { code -> code.default }!!.code

    enum class Type {
        FIAT,
        CRYPTO
    }

    companion object {
        fun findCurrency(code: String): Currency {
            return values().find { currency -> currency.codes.any { c -> c.code == code } } ?: UNKNOWN
        }
    }
}

class Code(val code: String, val default: Boolean = true)
