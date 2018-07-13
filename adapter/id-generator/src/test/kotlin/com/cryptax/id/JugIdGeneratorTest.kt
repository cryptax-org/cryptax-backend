package com.cryptax.id

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@DisplayName("Id Generator")
@ExtendWith(MockitoExtension::class)
class JugIdGeneratorTest {

    private val jugIdGenerator = JugIdGenerator()

    @TestFactory
    fun testGenerateId(): List<DynamicTest> {
        return IntRange(0, 50).map {
            DynamicTest.dynamicTest("Test generate uuid") {
                // when
                val actual = jugIdGenerator.generate()

                // then
                assertNotNull(actual)
                assert(!actual.contains("-"))
            }
        }
    }
}
