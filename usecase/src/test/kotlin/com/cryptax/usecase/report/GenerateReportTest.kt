package com.cryptax.usecase.report

import com.cryptax.domain.entity.Currency
import com.cryptax.domain.entity.Report
import com.cryptax.domain.entity.Transaction
import com.cryptax.domain.entity.User
import com.cryptax.domain.port.PriceService
import com.cryptax.domain.port.TransactionRepository
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.objectMapper
import io.reactivex.Maybe
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.mock
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.Stream

@DisplayName("Usescase generate report")
class GenerateReportTest {

    lateinit var userRepository: UserRepository
    lateinit var transactionRepository: TransactionRepository
    lateinit var priceService: PriceService
    lateinit var generateReport: GenerateReport
    private val user: User = objectMapper.readValue(javaClass.getResourceAsStream("/report/user.json"), User::class.java)

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        transactionRepository = mock(TransactionRepository::class.java)
        priceService = PriceServiceStub()
        generateReport = GenerateReport(
            userRepository = userRepository,
            transactionRepository = transactionRepository,
            priceService = priceService)

    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    fun testGenerate(transactions: List<Transaction>, expected: Report) {
        // given
        given(userRepository.findById(user.id!!)).willReturn(Maybe.just(user))
        given(transactionRepository.getAllForUser(user.id!!)).willReturn(Single.just(transactions))

        // when
        val actual = generateReport.generate(user.id!!).blockingGet()

        // then
        assertThat(actual).isEqualTo(expected)
        then(userRepository).should().findById(user.id!!)
        then(transactionRepository).should().getAllForUser(user.id!!)
    }

    companion object {

        @JvmStatic
        fun dataProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(transactions("/report/1-transactions.json"), expected("/report/1-result.json")),
                Arguments.of(transactions("/report/2-transactions.json"), expected("/report/2-result.json")),
                Arguments.of(transactions("/report/3-transactions.json"), expected("/report/3-result.json")),
                Arguments.of(transactions("/report/4-transactions.json"), expected("/report/4-result.json"))
            )
        }

        private fun expected(path: String): Report {
            return objectMapper.readValue(GenerateReportTest::class.java.getResourceAsStream(path), Report::class.java)
        }

        private fun transactions(path: String): List<Transaction> {
            return objectMapper.readValue(GenerateReportTest::class.java.getResourceAsStream(path), objectMapper.typeFactory.constructCollectionType(List::class.java, Transaction::class.java))
        }
    }

    class PriceServiceStub : PriceService {
        override fun currencyUsdValueAt(currency: Currency, date: ZonedDateTime): Pair<String, Double> {
            return when (currency) {
                Currency.BTC -> {
                    when (date) {
                        ZonedDateTime.of(2017, 10, 1, 10, 0, 0, 0, ZoneId.of("UTC")) -> Pair("", 4403.09)
                        ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")) -> Pair("", 13444.88)
                        ZonedDateTime.of(2018, 1, 2, 0, 0, 0, 0, ZoneId.of("UTC")) -> Pair("", 14754.13)
                        else -> throw RuntimeException("Stubbing issue, date not handled")
                    }
                }
                Currency.ETH -> {
                    when (date) {
                        ZonedDateTime.of(2018, 2, 1, 0, 0, 0, 0, ZoneId.of("UTC")) -> Pair("", 1026.19)
                        ZonedDateTime.of(2018, 1, 2, 0, 0, 0, 0, ZoneId.of("UTC")) -> Pair("", 861.97)
                        else -> throw RuntimeException("Stubbing issue, date not handled")
                    }
                }
                else -> throw RuntimeException("Stubbing issue, currency not handled")
            }
        }
    }
}
