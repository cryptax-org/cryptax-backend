package com.cryptax.usecase.utils

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers

object MockitoUtils {

	fun <T> anyNotNull(): T {
		return ArgumentMatchers.any<T>()
	}

	fun <T> ArgumentCaptor<T>.captureNotNull(): T {
		return capture()
	}

	fun MutableList<Int>.swap(index1: Int, index2: Int) {
		val tmp = this[index1] // 'this' corresponds to the list
		this[index1] = this[index2]
		this[index2] = tmp
	}
}
