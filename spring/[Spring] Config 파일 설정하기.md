## 1. JpaConfig
### 1-1. BaseTimeEntity
데이터의 생성, 수정 시간은 매우 자주 활용되고, 하지만 각각의 Entity의 생성 수정 시간을 매번 작성하는 건 너무 비효율적입니다.
Spring Data JPA에서는 위 값들에 대해서 자동으로 값을 넣어주는 기능인 JPA Auditing을 제공하고 있습니다.

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;
}
```

이런 식으로 BaseTimeEntity 클래스를 만들어주고 생성, 수정 시간에 대한 값을 넣어주고 싶은 클래스에 상속을 하면 됩니다.

### 1-2. JpaConfig를 통한 JPA Auditing 설정
JPA Auditing 설정엔 두 가지 방법이 있습니다. SpringBootApplication 클래스에 **@EnableJpaAuditing 어노테이션**을 추가하여 설정할 수 있는 방법이 있으며, JpaConfig 클래스에 **@Configuration 어노테이션**과 **@EnableJpaAuditing 어노테이션**를 사용하여 스프링 부트 서버 전역적으로 설정할 수 있는 방법이 있습니다.

꼭 설정하셔야 합니다! JPA Auditing 기능을 사용하겠다는 정보를 전달해 줄 수 있기 때문에 **@EnableJpaAuditing**을 추가해야 합니다.

저는 위 두 가지 방법 중 JpaConfig 클래스를 통해 전역적으로 설정할 수 있는 방법을 채택했습니다.

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

<br>

## 2. WebConfig
### 2-1. WebConfig를 통한 커스텀 ArgumentResolver 설정
```java
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final UserArgumentResolver userArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }
}
```

Config 패키지에 WebConfig 클래스를 만들어주고, WebMvcConfigurer를 상속받은 후addArugmentResolvers 클래스를 오버라이드하여 커스텀 ArgumentResolver를 추가해줍니다. 그 외 코드들은 나중에 제대로 한번 올려보겠습니다~!

### 2-2. WebConfig를 통한 CORS 설정
**CORS(Cross-Origin Resource Sharing)**란 교차 출처 리소스 공유를 의미하며, 웹 브라우저에서 다른 출처 간의 자원 공유에 대한 허용/비허용을 다룬 보안 정책입니다. 

프론트/백엔드를 통한 프로젝트들은 CORS 설정을 해주지 않으면 CORS 에러가 발생하게 됩니다.

CORS 설정엔 두 가지 방법이 있습니다. **@CrossOrigin 어노테이션**을 사용하여 특정 컨트롤러나 특정 API에만 CORS 설정을 할 수 있는 방법이 있으며, WebConfig 클래스에 **@Configuration 어노테이션**을 사용하여 스프링 부트 서버 전역적으로 설정할 수 있는 방법이 있습니다.

저는 위 두 가지 방법 중 WebConfig 클래스를 통해 전역적으로 설정할 수 있는 방법을 채택했습니다.

```java
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 요청 허용 도메인
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 요청 허용 메소드
                .allowedHeaders("*") // 요청 허용 헤더
                .allowCredentials(true) // 요청 허용 쿠키
                .maxAge(3600);
    }
}
```

위 커스텀 ArgumentResolver를 추가한 거처럼 WebConfig 클래스에 addCorsMappings 클래스를 오버라이드하여 제 입맛대로(?) 설정해줍니다.

<br>

## 3. WebsocketConfig
### 3-1. WebsocketConfig를 통한 설정
```java
@RequiredArgsConstructor
@EnableWebSocketMessageBroker // (1)
@Configuration
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    // JWT 인증을 위한 인터셉터
    private final StompHandler stompHandler;

    // 클라이언트에서 웹소켓 서버로 접속할때 사용할 웹소켓 엔드포인트를 등록
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { // (2)
        registry.addEndpoint("/ws/chat") // (3)
                // .setAllowedOrigins("*");
                .setAllowedOriginPatterns("*");
                // .withSockJS();
    }

    // 한 클라이언트에서 다른 클라이언트로 메시지를 라우팅하는데 사용될 메시지 브로커를 구성
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) { // (4)
        registry.setApplicationDestinationPrefixes("/sub"); // (5)
        registry.enableSimpleBroker("/pub"); // (6)
    }

    // 클라이언트로부터 받은 메시지를 처리하는데 사용할 채널을 구성
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) { // (7)
        registration.interceptors(stompHandler); // (8)
    }
}
```
(1) 메시지 브로커가 지원하는 WebSocket 메시지 처리를 활성화

(2) 핸드셰이크와 통신을 담당할 EndPoint 지정

(3) 웹소켓 연결 시 요청을 보낼 EndPoint 지정

(4) 메모리 기반의 Simple Message Broker 활성화

(5) 메시지 브로커가 Subscriber들에게 메시지를 전달할 URL 지정(메시지 구독 요청)

(6) 클라이언트가 서버로 메시지 보낼 URL 접두사 지정(메시지 발행 요청)

(7) 클라이언트로부터 받은 메시지를 처리하는데 사용할 채널을 구성

(8) 클라이언트로부터 받은 메시지를 처리하는데 사용할 인터셉터 지정

.setAllowedOrigins() 메서드 대신 .setAllowedOriginPatterns() 메서드를 사용하는 이유는 패턴 매칭을 사용하여 출처를 허용함과 동시에 특정 패턴에 맞는 출처만 허용할 수 있도록 가능하다. 예를 들면, https://*.example.com 와 같은 특정 도메인 패턴을 허용할 수도 있다.

따라서 .setAllowedOriginPatterns() 메서드를 사용하면 보다 세밀한 제어와 보안을 제공한다.

### 3-2. StompHandler 
```java
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // 우선순위 설정
public class StompHandler implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    // WebSocket 메시지를 처리하기 전에 실행되는 메소드
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 각 명령에 대한 처리
        if (accessor.getCommand() == CONNECT || accessor.getCommand() == SUBSCRIBE || accessor.getCommand() == SEND) {
            // Authorization 헤더에서 토큰 추출
            String authorization = accessor.getFirstNativeHeader("Authorization");

            // 토큰 유효성 검사
            if (!jwtTokenProvider.validateToken(authorization)) {
                throw InvalidTokenException.EXCEPTION;
            }
        }
        return message;
    }
}
```
<br>

## 4. RedisConfig
### 4-1. redisConnectionFactory
Redis와의 연결을 담당하는 **LettuceConnectionFactory**를 생성합니다. **LettuceConnectionFactory**는 기본적으로 비동기 방식으로 Redis 서버와 통신을 할 수 있는 클라이언트입니다.

### 4-2. redisTemplate
**RedisTemplate**은 Redis 데이터를 읽고 쓸 때 사용하는 주요 클래스입니다. 여기서는 키와 값을 직렬화/역직렬화할 때 사용할 **Serializer**를 지정합니다.

**setKeySerializer()**와 **setValueSerializer()**는 키와 값을 String으로 직렬화합니다.
**setHashKeySerializer()**와 **setHashValueSerializer()**는 Hash 데이터 구조를 위한 **Serializer**입니다. 값은 JSON 형태로 직렬화하기 위해 **GenericJackson2JsonRedisSerializer**를 사용합니다.

```java
@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    // redis 연결을 위해 connection 생성하는 메서드
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    // redis 데이터 처리를 위한 redisTemplate 생성하는 메서드
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // redis 연결
        redisTemplate.setConnectionFactory(connectionFactory);

        // key-value 형태로 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        // hash key-value 형태로 직렬화
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }
}
```
<br>

## 5. SecurityConfig
### 5-1. passwordEncoder
비밀번호를 안전하게 해시하기 위해 **BCryptPasswordEncoder** 생성하며, **@Lazy 어노테이션**을 사용하여 필요한 시점에 빈을 로드합니다.

### 5-2. SecurityFilterChain를 통한 필터 체인 설정
**SecurityFilterChain**을 통해 **HTTP 보안 설정**을 정의하며, 다음은 **configure 메서드**에서의 주요 설정입니다.

**CSRF 비활성화**: CSRF 공격 방어는 주로 세션 기반 인증에서 사용되지만, JWT를 사용하는 경우 불필요하므로 비활성화합니다.

**CORS 설정**: **corsConfigurationSource()** 메서드에서 설정한 CORS 규칙을 적용합니다. 모든 도메인과 메서드에 대한 허용이 가능하지만, 실제 운영 환경에서는 특정 도메인으로 제한하는 것이 좋습니다.

**폼 로그인 및 HTTP Basic 인증 비활성화**: JWT를 사용하는 경우, 폼 로그인이나 기본 인증 방식을 사용하지 않으므로 비활성화합니다.

**세션 관리 비활성화**: **SessionCreationPolicy.STATELESS**를 통해 세션을 사용하지 않고, JWT 토큰을 통해 인증을 관리합니다..

**요청에 대한 권한 관리**: 각 요청 경로에 대해 접근 권한을 설정합니다.

### 5-3. OAuth2 로그인 설정
OAuth2Login을 통해 소셜 로그인을 처리하고 OAuth2 인증 성공 시 **OAuth2AuthenticationSuccessHandler**를 통해 추가적인 로직을 처리합니다. 실패할 경우 **OAuth2AuthenticationFailureHandler**가 호출되며,
사용자 정보를 **OAuth2 공급자(Google, Facebook 등)**로부터 받아오기 위해 **CustomOAuth2UserService**를 사용합니다.
-> Spring Security를 이용한 로그인 방법은 너무 어렵다... 다시 공부하기

#### 예외 처리 설정
**JwtAuthenticationEntryPoint**: 인증되지 않은 사용자가 보호된 자원에 접근하려 할 때 401 응답을 반환합니다.
**JwtAccessDeniedHandler**: 권한이 없는 사용자가 보호된 자원에 접근하려 할 때 403 응답을 반환합니다.

### 5-4. JWT 인증 필터 추가
**JwtAuthenticationFilter**를 **UsernamePasswordAuthenticationFilter** 앞에 추가하여, 사용자의 요청에 담긴 JWT를 먼저 검증한 후에 인증 처리를 진행합니다.

### 5-5. CORS 설정
**CorsConfigurationSource**를 통해 **CORS 설정**을 정의하고 모든 도메인과 메서드를 허용하도록 구성하였으며, 실제 운영 환경에서는 보안을 위해 적절히 제한해야 합니다.

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService CustomOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


    @Bean
    @Lazy
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);

        http
                .csrf(CsrfConfigurer::disable) // CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화 (JWT 기반 인증 사용 시 필요 없음)
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // Clickjacking 방어를 위한 frameOptions 비활성화

                // 세션 관리 비활성화
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증 규칙 설정
                .authorizeHttpRequests((authorizeRequests) -> authorizeRequests
                        .requestMatchers("/", "/favicon.ico", "/css/**", "/js/**", "/images/**", "/error").permitAll() // 정적 리소스는 인증 없이 접근 가능
                        .requestMatchers("/auth/**", "/oauth2/**").permitAll() // 회원가입, 로그인은 인증 없이 접근 가능
                        .requestMatchers("/user/**").hasRole("USER") // 사용자 정보 조회는 USER 권한이 있어야 접근 가능
                        .requestMatchers(HttpMethod.GET).permitAll() // GET 요청은 인증 없이 접근 가능 (테스트용)
                        .requestMatchers(HttpMethod.POST).permitAll() // POST 요청은 인증 없이 접근 가능 (테스트용)
                        .requestMatchers(HttpMethod.PUT).permitAll() // PUT 요청은 인증 없이 접근 가능 (테스트용)
                        .requestMatchers(HttpMethod.DELETE).permitAll() // DELETE 요청은 인증 없이 접근 가능 (테스트용)
                        .anyRequest().authenticated())

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorize"))
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/auth/**/callback"))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(CustomOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                        .defaultSuccessUrl("/", true))

                // 예외 처리
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 인증되지 않은 사용자가 접근할 경우
                        .accessDeniedHandler(jwtAccessDeniedHandler)) // 403 인가되지 않은 사용자가 접근할 경우

                // JWT 인증 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

```
-> Spring Security는 정확히 이해하지 못했기 때문에 추후에 공부 더 열심히하기 !

## 6. 더 공부하여 추가해보겠습니다.