package com.cryptax.domain.entity

// TODO: Add a alias system to handle several code
enum class Currency constructor(val code: String, val fullName: String, val symbol: String, val type: Type) {
    BTC("BTC", "Bitcoin", "฿", Type.CRYPTO),
    ETH("ETH", "Ethereum", "Ξ", Type.CRYPTO),
    GRS("GRS", "Groestlcoin", "GRS", Type.CRYPTO),
    LTC("LTC", "Litecoin", "Ł", Type.CRYPTO),
    VTC("VTC", "Vertcoin", "VTC", Type.CRYPTO),
    ETHOS("ETHOS", "Ethos", "ETHOS", Type.CRYPTO),
    CARDANO("ADA", "Cardano", "ADA", Type.CRYPTO),
    POWER_LEDGER("POWR", "Power Ledger", "POWR", Type.CRYPTO),
    ICON("ICX", "Icon", "ICX", Type.CRYPTO),
    MONERO("XMR", "Monero", "XMR", Type.CRYPTO),
    NEO("NEO", "NEO", "NEO", Type.CRYPTO),
    EOS("EOS", "EOS", "EOS", Type.CRYPTO),
    STEEM("STEEM", "Steem", "STEEM", Type.CRYPTO),
    KOMODO("KMD", "Komodo", "KMD", Type.CRYPTO),
    ARK("ARK", "Ark", "ARK", Type.CRYPTO),
    WALTON("WTC", "Walton", "WTC", Type.CRYPTO),
    NAV("NAV", "Nav Coin", "NAV", Type.CRYPTO),
    UTRUST("UTK", "Utrust", "UTK", Type.CRYPTO),

    USD("USD", "United States Dollar", "$", Type.FIAT),
    EUR("EUR", "Euro", "€", Type.FIAT),
    UNKNOWN("UNKNOWN", "Unknown", "U", Type.FIAT);

    enum class Type {
        FIAT,
        CRYPTO
    }

    companion object {
        fun findCurrency(code: String): Currency {
            // TODO find a better way to handle code change
            val str = if (code == "BQX") "ETHOS" else code
            return values()
                .find { currency ->
                    currency.code == str
                } ?: UNKNOWN
        }
    }
}

