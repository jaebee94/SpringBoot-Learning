package com.rest.api.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic().disable() // resp api 니까 기본설정 사용안함. (기본설정: 비인증시 로그인폼 화면으로 리다이렉트)
            .csrf().disable() // rest api 이므로 csrf 보안 필요 X -> 좀 더 찾아보기
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt 인증방식: 세션 필요 X
            .and()
                .authorizeRequests() // 다음 리퀘스트에 대한 사용권한 체크
                    .antMatchers("/*/signin", "/*/signup").permitAll() // 가입, 인증은 누구나 접근 가능
                    .antMatchers(HttpMethod.GET, "/exception/**", "helloworld/**").permitAll() // 누구나 접근 가능한 GET요청 리소스
                    .antMatchers("/*/users").hasRole("ADMIN")
                    .anyRequest().hasRole("USER") // 그 외 요청은 인증된 회원만 접근 가능
            .and()
                .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
            .and()
                .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // jwt 필터를 id/password 인증 필터 전에 넣는다.

    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs", "swagger-resources/**",
                "/swagger-ui.html", "/webjars/**", "/swagger/**");
    }
}
