package com.cryptax.app

import com.cryptax.domain.entity.User
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull

fun createUser(user: User) {
    // @formatter:off
    given().
        log().all().
        body(user).
        contentType(ContentType.JSON).
    post("/users").
    then().
        log().all().
        assertThat().body("id", notNullValue()).
        assertThat().body("email", IsEqual(user.email)).
        assertThat().body("password", IsNull.nullValue()).
        assertThat().body("lastName", IsEqual(user.lastName)).
        assertThat().body("firstName", IsEqual(user.firstName)).
        assertThat().statusCode(200)
    // @formatter:on
}

fun getToken(credentials: String): JsonPath {
    // @formatter:off
    return given().
               log().all().
               body(credentials).
               contentType(ContentType.JSON).
           post("/token").
               then().
               log().all().
               assertThat().body("token", notNullValue()).
               assertThat().body("refreshToken", notNullValue()).
               assertThat().statusCode(200).
           extract().
               body().jsonPath()
     // @formatter:on
}
