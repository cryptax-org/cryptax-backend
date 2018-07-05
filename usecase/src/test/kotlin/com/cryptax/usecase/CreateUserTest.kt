package com.cryptax.usecase

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.PasswordEncoder
import com.cryptax.domain.port.UserRepository
import com.cryptax.usecase.utils.MockitoUtils.anyNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.verify
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CreateUserTest {

	@Mock
	lateinit var userRepository: UserRepository
	@Mock
	lateinit var passwordEncoder: PasswordEncoder
	@Mock
	lateinit var idGenerator: IdGenerator
	@InjectMocks
	lateinit var createUser: CreateUser

	val userCaptor: ArgumentCaptor<User> = ArgumentCaptor.forClass(User::class.java)

	@Test
	fun testCreate() {
		//given
		val id = "random id"
		val user = User("eeqqqw", "email@email.com", "eeeee", "ee", "ee")
		given(userRepository.findByEmail(user.email)).willReturn(null)
		given(idGenerator.generate()).willReturn(id)
		given(passwordEncoder.encode("email@email.comeeeee")).willReturn("random-id")
		given(userRepository.create(anyNotNull())).willReturn(user)


		//when
		val actual = createUser.create(user)

		//then
		assertNotNull(actual)
		then(userRepository).should().findByEmail(user.email)
		then(idGenerator).should().generate()
		then(passwordEncoder).should().encode(user.email + user.password)
		verify(userRepository).create(userCaptor.captureNotNull())
		//then(userRepository).should().create(userCaptor.capture())
		assertEquals(id, userCaptor.value.id)
	}

	fun <T> ArgumentCaptor<T>.captureNotNull(): T {
		val derp =  this.capture()
		return derp
	}
}
