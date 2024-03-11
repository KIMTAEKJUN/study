> 더 많은 지식을 얻기 위해 이번엔 **스프링 부트와 AWS로 혼자 구현하는 웹 서비스(이동욱 지음)** 책을 읽고, 공부하고 정리했습니다.

## 스프링 시큐리티와 OAuth 2.0으로 로그인 기능 구현하기
### 1. 스프링 시큐리티(Spring Security)
- 막강한 인증(Authentication)과 인가(Authorization, 혹은 권한 부여) 기능을 가진 프레임워크이다.
- 스프링 기반의 애플리케이션에서는 보안을 위한 표준이라 볼 수 있다.
- 인터셉터, 필터 기반의 보안 기능을 구현하는 것보다 스프링 시큐리티를 통해 구현하는 것을 적극적으로 권장한다.

#### 1-1. 스프링 시큐리티와 스프링 시큐리티 Oauth2 클라이언트
- 많은 서비스에서 로그인 기능을 id/password 방식보다는 구글, 페이스북, 네이버 로그인과 같은 소셜 로그인 기능을 사용한다.
- OAuth 로그인 구현 시 비밀번호 찾기, 비밀번호 변경과 같은 기능들을 모두 구글, 페이스북, 네이버 등에 맡기면 되니 서비스 개발에 집중할 수 있다.

#### 1-2. 스프링 부트 1.5 vs 스프링 부트 2.0
스프링 부트 2.0에서의 OAuth 연동 방법이 크게 변경되었다. 하지만, 인터넷 자료들을 보면 **설정 방법에 크게 차이가 없는 경우를 자주 봅니다.**
```
spring-security-oauth2-autoconfigure
```
라이브러리를 사용할 경우 쓰던 설정을 그대로 사용할 수 있다. **기존에 안전하게 작동하던 코드**를 사용할 수 있다.

### 2. 구글 서비스 등록
- 구글 클라우드 플랫폼(https://console.cloud.google.com)에 접속해, API 및 서비스 중 OAuth 동의 화면, 사용자 인증 정보를 작성하여 클라이언트 정보(ID, 보안 비밀코드)를 발급받는다.
  - 사용자 인증 정보중 'OAuth 클라이언트 ID 만들기'에서 승인된 리디렉션 URI
    - 인증이 성공하면 구글에서 리다이렉트할 URL
    - 스프링부트2 버전의 시큐리티에서 기본으로 {도메인}/login/oauth2/code{소셜서비스코드}로 리다이렉트 URL을 지원한다.

#### 2-1. application-auth 등록
- src/main/resources/application-oauth.properties 파일 생성 후 코드 등록

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/01c7c7e4-7bc9-4001-bcaf-224765da9eb2/image.png)
- scope=profile,email
  - scope의 기본값 : openid, profile, email


- 스프링 부트에서는 properties의 이름을 application-xxx.properties 로 만들면 xxx라는 이름의 **profile**이 생성되어, profile=xxx라는 식으로 호출하면 해당 **properties의 설정들을 가져올** 수 있다.
  - 스프링 부트의 기본 설정 파일인 application.properties에서 application-oauth.properties를 포함하도록 구성한다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/2ab55330-a79a-4b68-ba65-dcd0501be784/image.png)

#### 2-2. gitignore 등록
- 구글 로그인을 위한 클라이언트 ID와 클라이언트 보안 비밀은 보안이 중요한 정보들이고, 외부에 노출될 경우 언제든 개인정보를 가져갈 수 있는 취약점이 될 수 있다.
- 보안을 위해 깃허브에 application-oauth.properties 파일이 올라가는 것을 방지해 준다. 1장에서 만들었던 .gitignore에 다음과 같이 한 줄의 코드를 추가한다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/dd78558e-d950-477f-9c34-9970b9555f37/image.png)

### 3. 구글 로그인 연동하기
#### 3-1. User 도메인
```java
@Getter
@NoArgsConstructor
@Entity
public class User extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String email;

  @Column
  private String picture;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Builder
  public User(String name, String email, String picture, Role role) {
    this.name = name;
    this.email = email;
    this.picture = picture;
    this.role = role;
  }

  public User update(String name, String picture) {
    this.name = name;
    this.picture = picture;

    return this;
  }

  public String getRoleKey() {
    return this.role.getKey();
  }
}
```
`@Enumerated(EnumType.STRING)` : JPA로 데이터베이스로 저장할 때 Enum값을 어떤 형태로 저장할지 결정하고, 기본적으로 int로 된 숫자가 저장되지만 데이터베이스로 확인할 때 그 값의 의미를 파악하기 힘들어 문자열로 저장될 수 있게 한다.

각 사용자 권한을 관리할 Enum 클래스 Role을 생성한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/e70cfaae-8de3-405f-8adc-b385bdc7ce32/image.png)

스프링 시큐리티에서는 권한 코드에 항상 **ROLE_이 앞에 있어야만** 합니다. 그래서 코드별 키 값을 ROLE_GUEST, ROLE_USER 등으로 지정한다.

마지막으로 User의 CRUD를 책임질 UserRepository도 생성한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/2b6b0b0c-47c2-4aa7-b1d8-7eff1956c8a0/image.png)

`findByEmail` : 소셜 로그인으로 반환되는 값 중 email을 통해 이미 생성된 사용자인지 처음 가입하는 사용자인지 판단하기 위한 메소드이다.

#### 3-2. 스프링 시큐리티 설정
먼저 build.gradle에 스프링 시큐리티 관련 의존성 하나를 추가해 준다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/0aad075b-f8a9-431c-a21c-d4b391992b96/image.png)

`spring-boot-starter-oauth2-client` : 소셜 로그인 등 클라이언트 입장에서 소셜 기능 구현 시 필요한 의존성이다.

#### 3-3. SecurityConfig
- SecurityConfig의 용도 : 프로젝트에서 원하는 인증 메커니즘으로 커스텀하기 위함

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/5d87dccb-d2bb-47ca-affd-ed565cc10b8a/image.png)

`@EnableWebSecurity` : Spring Security 설정들을 활성화시켜 준다.  
`csrf().disable().headers().frameOptions().disable()` : h2-console 화면을 사용하기 위해 해당 옵션들을 disable 한다.  
`authorizeRequests` : URL별 권한 관리를 설정하는 옵션의 시작점이다.  
`antMatchers` : 권한 관리 대상을 지정하는 옵션, URL, HTTP 메소드별로 관리가 가능하다.  
`anyRequest` : 설정된 값들 이외 나머지 URL들을 나타낸다.  
`logout().logoutSuccessUrl("/")` : 로그아웃 기능에 대한 여러 설정의 진입점, 로그아웃 성공 시 / 주소로 이동한다.  
`oauth2Login` : OAuth 2 로그인 기능에 대한 여러 설정의 진입점이다.  
`userInfoEndpoint` : OAuth 2 로그인 성공 이후 사용자 정보를 가져올 때의 설정들을 담당한다.  
`userService` : 소셜 로그인 성공 시 후속 조치를 진행할 UserService 인터페이스의 구현체를 등록한다.

#### 3-3. CustomOAuth2UserService
- CustomOAuth2UserService의 용도 : 소셜 로그인 이후 가져온 사용자의 정보들을 기반으로 가입 및 정보수정, 세션 저장등의 기능 지원
```java
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
  private final UserRepository userRepository;
  private final HttpSession httpSession;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

    OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

    User user = saveOrUpdate(attributes);
    httpSession.setAttribute("user", new SessionUser(user));

    return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
            attributes.getAttributes(),
            attributes.getNameAttributeKey());
  }

  private User saveOrUpdate(OAuthAttributes attributes) {
    User user = userRepository.findByEmail(attributes.getEmail())
            .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
            .orElse(attributes.toEntity());

    return userRepository.save(user);
  }
}
```
`registrationId` : 현재 로그인 진행 중인 서비스를 구분하는 코드이다. (ex. 구글 로그인인지, 네이버 로그인인지)  
`userNameAttributeName` : OAuth2 로그인 진행 시 키가 되는 필드값을 이야기이다. (Primary Key와 같은 역할), 구글의 경우 기본 코드는 "sub", 네이버나 카카오는 따로 지원하지 않는다. 네이버 로그인과 구글 로그인을 동시 지원할 때 사용한다.  
`OAuthAttributes` : OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담을 클래스이다.  
`SessionUser` : 세션에 사용자 정보를 저장하기 위한 Dto 클래스이다.


#### 3-4. OAuthAttributes
- OAuthAttributes의 용도 : OAuth2User의 attribute에서 원하는 사용자의 정보만(name, email, picture등) 담을 클래스
  - registrationId를 통해 어떤 소셜 로그인인지를 구별하여 각각에 맞는 처리를 하기 위해, 구글에는 primary key값이 있지만 네이버나 카카오는 없다.
```java
@Getter
public class OAuthAttributes {
  private Map<String, Object> attributes;
  private String nameAttributeKey;
  private String name;
  private String email;
  private String picture;

  @Builder
  public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture) {
    this.attributes = attributes;
    this.nameAttributeKey = nameAttributeKey;
    this.name = name;
    this.email = email;
    this.picture = picture;
  }

  public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
    return ofGoogle(userNameAttributeName, attributes);
  }

  private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
    return OAuthAttributes.builder()
            .name((String) attributes.get("name"))
            .email((String) attributes.get("email"))
            .picture((String) attributes.get("picture"))
            .attributes(attributes)
            .nameAttributeKey(userNameAttributeName)
            .build();
  }

  public User toEntity() {
    return User.builder()
            .name(name)
            .email(email)
            .picture(picture)
            .role(Role.GUEST)
            .build();
  }
}
```
`of()` : OAuth2User에서 반환하는 사용자 정보는 Map이기 때문에 값 하나하나를 변환해야만 한다.  
`toEntity()` : User 엔티티를 생성, OAuthAttributes에서 엔티티를 생성하는 시점은 처음 가입할 때이다. (가입할 때 기본 권한을 GUEST로 주기 위해 role 빌더값엔 Role.GUEST를 사용)

#### 3-5. SessionUser
- SessionUser의 용도 : 직렬화 기능을 가진 세션 Dto

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/742df75c-d51a-4918-bafc-0fc41df26a78/image.png)


- SessionUser에는 인증된 사용자 정보만 필요하기 때문 name, email, picture만 필드로 선언한다.


- **httpSession.setAttribute("user", new SessionUser(user))**
  - setAttribute 두번째 인자로 직렬화를 구현한 클래스 객체이어야 한다.
  - User 클래스는 엔티티이기 때문에 직렬화 코드를 넣기에는 이슈가 발생할 가능성이 있다.
    - User엔티티 클래스가 다른 엔티티와 관계가 형성되게 되면 직렬화 대상에 자식들까지 포함되기 때문이다.

#### 3-6. 로그인 관련 코드 수정 및 추가
index.mustache에 로그인 버튼과 로그인 성공 시 사용자 이름을 보여주는 코드이다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/df074f5c-0249-43df-8aaa-dc8c562f27bc/image.png)

`{{#userName}}` : userName이 있으면 다음 줄 부터 {{/userName}}까지 화면에 보여준다.  
`{{^userName}}` : userName이 없으면 다음 줄 부터 {{/userName}}까지 화면에 보여준다.  
`a href="/logout"` : 스프링 시큐리티에서 기본적으로 제공하는 로그아웃 URL, 별도의 Controller를 생성할 필요가 없다.  
`a href="/oauth2/authorization/google"` : 스프링 시큐리티에서 기본적으로 제공하는 로그인 URL, 별도의 Controller를 생성할 필요가 없다.

index.mustache에서 userName을 사용할 수 있게 IndexController에서 userName을 model에 저장하는 코드를 추가해 준다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/9d338995-aba0-4d77-8c8e-afa480ca7efd/image.png)

`(SessionUser) httpSession.getAttribute("user")` : CustomOAuth2UserService에서 로그인 성공 시 세션에 SessionUser를 저장하도록 구성했다. 즉, 로그인 성공 시 httpSession.getAttribute("user")에서 값을 가져올 수 있다.  
`if (user != null)` : 세션에 저장된 값이 있을 때만 model에 userName으로 등록한다.

### 4. 어노테이션 기반으로 개선하기(반복코드 개선)
```java
SessionUser user = (SessionUser) httpSession.getAttribute("user");
```
- 세션값 가져오는 부분은 다른 컨트롤러와 메소드에서 필요로 할 수 있다.

  - 같은 코드가 계속해서 반복되는 것은 개선이 필요하다.
    - 어노테이션을 통해 메소드 인자로 세션값을 바로 받을 수 있도록 변경한다.

#### 4-1. LoginUser 어노테이션 생성
config.auth 패키지에 @LoginUser 어노테이션을 생성한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/d7883c5b-6432-4795-958b-35a0db95f22a/image.png)

`@Target(ElementType.PARAMETER)` : 해당 어노테이션이 생성될 수 있는 위치를 지정, PARAMETER로 지정했으니 메소드의 파라미터로 선언된 객체에서만 사용할 수 있다.
`@interface` : 해당 파일을 어노테이션 클래스로 지정한다.

#### 4-2. LoginUserArgumentResolver 생성
같은 위치에 LoginUserArgumentResolver를 생성한다. Login-UserArgumentResolver라는 HandlerMethodArgumentResolver 인터페이스를 구현한 클래스이다.
```java
@RequiredArgsConstructor
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
  private final HttpSession httpSession;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    boolean isLoginUserAnnotation = parameter.getParameterAnnotation(LoginUser.class) != null;
    boolean isUserClass = SessionUser.class.equals(parameter.getParameterType());
    return isLoginUserAnnotation && isUserClass;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    return httpSession.getAttribute("user");
  }
}
```
`HandlerMethodArgumentResolver` : 컨트롤러에서 구현체가 지정한 값으로 파라미터를 바인딩해주는 역할, view페이지에서 전달해주는 파라미터가 아닌 파라미터들을 바인딩해줄 때 사용한다.  
`supportsParameter()` : 컨트롤러 메서드의 특정 파라미터를 지원하는지 판단한다.
`resolveArgument()` : 파라미터에 전달할 객체를 생성, 여기선 세션에서 user를 가져온다.

#### 4-3. WebConfig
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/65a3fdef-dabe-49a2-90db-07c68601bb52/image.png)

- LoginUserArgumentResolver가 **스프링에서 인식될 수 있도록** WebMvcConfigurer에 추가한다.

- addArgumentResolvers()에 구현한 HandlerMethodArgumentResolver를 등록한다.
  - HandlerMethodArgumentResolver는 항상 WebMvcConfigurer의 **addArgumentResolvers()**를 통해 추가해야 한다.

#### 4-4. IndexController 코드를 @LoginUser로 개선
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/a5768dd7-469e-4bb5-afa5-7e88012fda4a/image.png)

`@LoginUser SessionUser user` : 기존에 (User) httpSession.getAttribute("user") 로 가져오던 세션 정보 값이 개선, 어느 컨트롤러든지 @LoginUser만 사용하면 세션 정보를 가져올 수 있다.