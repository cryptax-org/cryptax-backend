package com.cryptax.app.config

//import com.cryptax.app.jwt.JwtTokenFilter
import com.cryptax.app.jwt.AuthenticationManager
import com.cryptax.app.jwt.JwtTokenFilter
import com.cryptax.app.jwt.JwtTokenProvider
import com.cryptax.app.jwt.SecurityContextRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfig {

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Autowired
    lateinit var securityContextRepository: SecurityContextRepository

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf().disable()

        http
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange()
            .pathMatchers(HttpMethod.POST, "/users").permitAll()
            .pathMatchers(HttpMethod.GET, "/users/*/allow").permitAll()
            .pathMatchers(HttpMethod.POST, "/token").permitAll()
            .anyExchange().authenticated()
            .and()
            .exceptionHandling()
            .accessDeniedHandler(HttpStatusServerAccessDeniedHandler(HttpStatus.UNAUTHORIZED))

        http.addFilterAt(JwtTokenFilter(jwtTokenProvider), SecurityWebFiltersOrder.AUTHENTICATION)

        return http.build()
    }
}
//@Configuration
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//class WebSecurityConfig : WebSecurityConfigurerAdapter() {
//
//    @Autowired
//    lateinit var jwtTokenProvider: JwtTokenProvider
//
//    override fun configure(http: HttpSecurity) {
//
//        // Disable CSRF (cross site request forgery)
//        http.csrf().disable()
//        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//
//        // Entry points
//        http.authorizeRequests()//
//            .antMatchers(HttpMethod.POST, "/users").permitAll()
//            .antMatchers(HttpMethod.GET, "/users/*/allow").permitAll()
//            .antMatchers(HttpMethod.POST, "/token").permitAll()
//            .anyRequest().authenticated()
//
//        // Apply JWT
//        //http.addFilterBefore(JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter::class.java)
//    }
//
//    override fun configure(web: WebSecurity) {
//        // Allow swagger to be accessed without authentication
////        web.ignoring().antMatchers("/v2/api-docs")//
////            .antMatchers("/swagger-resources/**")//
////            .antMatchers("/swagger-ui.html")//
////            .antMatchers("/configuration/**")//
////            .antMatchers("/webjars/**")//
////            .antMatchers("/public")
////
////            // Un-secure H2 Database (for testing purposes, H2 console shouldn't be unprotected in production)
////            .and()
////            .ignoring()
////            .antMatchers("/h2-console/**/**")
//    }
//}


