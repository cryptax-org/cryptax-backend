package com.cryptax.domain.entity

enum class Source {
	COINBASE, BINANCE, KUCOIN, MANUAL;

	companion object {
		fun contains(str: String): Boolean {
			return Source.values().map { it.name }.contains(str)
		}
	}
}
