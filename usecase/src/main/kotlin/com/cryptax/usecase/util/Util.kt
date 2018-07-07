package com.cryptax.usecase.util

object Util {

	fun nullifyCharArray(str: CharArray) {
		str.forEachIndexed { index, _ -> str[index] = '\u0000' }
	}
}
