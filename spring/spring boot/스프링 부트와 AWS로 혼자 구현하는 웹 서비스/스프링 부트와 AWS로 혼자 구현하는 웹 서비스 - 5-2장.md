### 5. 세션 저장소로 데이터베이스 사용하기
추가로 개선을 해보자 ❓ 지금 우리가 만든 서비스는 **애플리케이션을 재실행**하면 로그인이 풀린다.  
왜 그럴까 ❓ 세션이 **내장 톰캣의 메모리에 저장**되기 때문이다.  
기본적으로 세션은 실행되는 WAS의 메모리에서 저장되고 호출된다. 메모리에 저장되다 보니 **내장 톰캣처럼 애플리케이션 실행 시 실행 되는 구조에선 항상 초기화**가 된다.  
즉, **배포할 때마다 톰캣이 재시작**되는 것이다.  
이 외에도 한 가지 문제가 더 있다. 2대 이상의 서버에서 서비스하고 있다면 **톰캣마다 세션 동기화** 설정을 해야만 한다.  
그래서 실제 현업에서는 세션 저장소에 대해 다음의 3가지 중 한 가지를 선택한다.
- **(1) 톰캣 세션을 사용한다.**
  - 일반적으로 별다른 설정을 하지 않을 때 기본적으로 선택되는 방식이다.
  - 이렇게 될 경우 톰캣(WAS)에 세션이 저장되기 때문에 2대 이상의 WAS가 구동되는 환경에서는 톰캣들 간의 세션 공유를 위한 추가 설정이 필요하다.

- **(2) MySQL과 같은 데이터베이스를 세션 저장소로 사용한다.**
  - 여러 WAS 간의 공용 세션을 사용할 수 있는 가장 쉬운 방법이다.
  - 많은 설정이 필요 없지만, 결국 로그인 요청마다 DB IO가 발생하여 성능상 이슈가 발생할 수 있다.
  - 보통 로그인 요청이 많이 없는 백오피스, 사내 시스템 용도에서 사용한다.

- **(3) Redis, Memcached와 같은 메모리 DB를 세션 저장소로 사용한다.**
  - B2C 서비스에서 가장 많이 사용하는 방식이다.
  - 실제 서비스로 사용하기 위해서는 Embedded Redis와 같은 방식이 아닌 외부 메모리 서버가 필요하다.

여기서 두 번째 방식인 **데이터베이스를 세션 저장소**로 사용하는 방식을 선택하여 진행한다.  
선택한 이유는 **설정이 간단**하고 사용자가 많은 서비가 아니며 비용 절감을 위해서이다.

#### 5-1. spring-session-jdbc 등록
먼저 build.gradle에 다음과 같이 의존성을 등록한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/21256a72-886b-48df-980a-ec995a1228ea/image.png)

그리고 application.properties에 세션 저장소를 jdbc로 선택하도록 코드를 추가한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/249f1d5f-7dde-4621-b355-4933e2fa70e1/image.png)

다시 애플리케이션을 실행해서 로그인을 테스트한 뒤, h2-console로 접속한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/bbe31b53-77b3-4cdd-b7b0-0ac72da59c12/image.png)


h2-console을 보면 세션을 위한 테이블(SPRING_SESSION,  SPRING_SESSION_ATTRIBUTE)가 생성된것을 볼 수 있다.

JPA로 인해 세션 테이블이 자동 생성되었기 때문에 별도로 해야할 일은 없다.

방금 로그인 해기 때문에 한 개의 세션이 등록돼있는 것을 볼 수 있다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/05be19d4-88bd-4b48-a00a-4052635e2a98/image.png)

세션 저장소를 데이터베이스로 교체했다. 물론 지금은 기존과 동일하게 **스프링을 재시작 하면 세션이 풀린다.**

이유는 H2 기반으로 스프링이 재실행될 때 **H2도 재시작되기 때문**이다.

이후 AWS로 배포하게 되면 AWS의 데이터베이스 서비스인 RDS를 사용하게 되니 이 때부터는 세션이 풀리지 않는다.

### 6. 네이버 로그인
#### 6-1. 네이버 API 등록
먼저 네이버 오픈 API로 이동한다.
> https://developers.naver.com/apps/#/register?api=nvlogin

각 항목을 다 채우고 등록을 완료하면 다음과 같이 CilentID와 ClientSecret가 생성된다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/b663e185-7988-4415-86dd-22be5155f37d/image.png)

해당 키값들을 application-oauth.properties에 등록한다.

네이버에서는 스프링 시큐리티를 공식 지원하지 않기 때문에 그동안 Common-OAuth2Provider에서 해주던 값들도 전부 수동으로 입력해야 한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/4a30caac-cfc3-4164-9bde-f2ad929b0d53/image.png)

`user-name-attribute=response` : 기준이 되는 user_name의 이름을 네이버에서는 response로 해야 한다. 이유는 네이버의 회원 조회 시 반환되는 JSON 형태 때문이다.

네이버의 오픈 API의 로그인 회원 결과는 다음과 같다.
```json
{
	"resultcode": "00",
    "message": "success",
    "response": {
    	"email": "openapi@naver.com",
        "nickname": "OpenAPI",
        "profile_image": "https://ssl.pstaric.net/static/pwe/
        							  address/nodata_33x33.gif",
        "age": "40-49",
        "gender": "F",
        "id": "32742776",
        "name": "오픈API",
        "birhtday": "10-01"
  }
}
```
스프링 시큐리티에선 **하위 필드를 명시할 수 없다.** 최상위 필드들만 user_name으로 지정 가능하다. 하지만 네이버의 응답값 최상위 필드는 **resultCode, message, response**이다.

스프링 시큐리티에서 인식 가능한 필드는 저 3개 중에 골라야한다. 본문에서 담고 있는 response를 user_name으로 지정하고 이후 **자바 코드로 response의 id를 user_name**으로 지정하겠다.

#### 6-2. 스프링 시큐리티 설정 등록
**OAuthAttributes**에 다음과 같이 **네이버인지 판단하는 코드와 네이버 생성자**만 추가해 주면 된다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/8d2a64ff-3a4f-43de-8c30-35f3e9f84dc7/image.png)



마지막으로 index.mustache에 네이버 로그인 버튼을 추가한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/67e189c6-5981-464f-81c5-647431c01a7e/image.png)

`/oauth2/authorization/naver` : 네이버 로그인 URL은 application-oauth.properties에 등록한 redirect-uri 값에 맞춰 자동으로 등록된다.

### 7. 기존 테스트에 시큐리티 적용하기
마지막으로 **기존 테스트에 시큐리티 적용으로 문제가 되는 부분**들을 해결하겠습니다. 문제가 되는 부분들은 대표적으로 다음과 같은 이유 때문입니다.  
기존에는 바로 API를 호출할 수 있어 테스트 코드 역시 바로 API를 호출하도록 구성하였습니다. 하지만, 시큐리티 옵션이 활성화되면 인증된 사용자만 API를 호출할 수 있습니다. 기존의 API 테스트 코드들이 모두 인증에 대한 권한을 받지 못하였으므로, 테스트 코드마다 인증한 사용자가 호출한 것처럼 작동하도록 수정하겠습니다.

#### 7-1. properties 적용되는 범위에 대한 문제
- test에 application.properties가 없으면 main의 설정을 그대로 가져온다.
  - 자동으로 가져오는 옵션의 범위는 application.properties 파일 까지이다.
  - 즉, application-oauth.properties는 test에 파일이 없다고 자동으로 main에서 가져오는 것이 아니다.


- 위와 같은 문제를 해결하기 위해 test환경에 application.properties를 새로 작성
  - main의 application-oauth.properties의 설정값들을 가짜 설정값을 작성

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/1d2b8bae-167d-45d7-98ee-0de1d8a80937/image.png)

#### 7-2. 인증된 가짜 사용자 만들기
- build.gradle에 spring-security-test를 추가
  - 스프링 시큐리티 테스트를 위한 여러 도구를 지원


- 인증된 가짜 사용자 추가
  - PostsApiControllerTest의 Posts_등록된다(), 수정된다()

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/37dfbab5-c3eb-47c1-968a-46092f441c22/image.png)

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/92b99176-0aea-4251-a08e-b562ea17ea50/image.png)

`@WithMockUser(roles = "USER")` : 인증된 모의(가짜) 사용자를 만들어서 사용, ROLE_UESR 권한을 가진 사용자가 API를 요청하는 것과 같다.

테스트가 될 것 같지만, 실제로 작동하진 않는다. **@WithMockUser가 MockMvc에서만 작동하기 때문**이다. 현재 PostsApiControllerTest는 @SpringBootTest로만 되어있으며 MockMvc를 전혀 사용하지 않는다. 그래서 **@SpringBootTest에서 MockMvc를 사용하는 방법**을 소개한다.  
코드를 다음과 같이 수정한다.

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {
  @LocalServerPort
  private int port;

  @Autowired
  private PostsRepository postsRepository;

  @Autowired
  private WebApplicationContext context;

  private MockMvc mvc;

  @Before
  public void setup() {
    mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
  }

  @After
  public void tearDown() throws Exception {
    postsRepository.deleteAll();
  }

  @Test
  @WithMockUser(roles = "USER")
  public void Posts_등록된다() throws Exception {
    // given
    String title = "title";
    String content = "content";
    PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
            .title(title)
            .content(content)
            .author("author")
            .build();

    String url = "http://localhost:" + port + "/api/v1/posts";

    // when
    mvc.perform(post(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto)))
            .andExpect(status().isOk());

    // then
    List<Posts> all = postsRepository.findAll();
    assertThat(all.get(0).getTitle()).isEqualTo(title);
    assertThat(all.get(0).getContent()).isEqualTo(content);
  }

  @Test
  @WithMockUser(roles = "USER")
  public void Posts_수정된다() throws Exception {
    // given
    Posts savedPosts = postsRepository.save(Posts.builder()
            .title("title")
            .content("content")
            .author("author")
            .build());

    Long updateId = savedPosts.getId();
    String expectedTitle = "title2";
    String expectedContent = "content2";

    PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
            .title(expectedTitle)
            .content(expectedContent)
            .build();

    String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

    // when
    mvc.perform(put(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto)))
            .andExpect(status().isOk());

    // then
    List<Posts> all = postsRepository.findAll();
    assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
    assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
  }
}
```
`import ----` : 모두 새로 추가되는 부분이다.  
`@Before` : 매번 테스트가 시작되기 전에 MockMvc 인스턴스를 생성한다.  
`mvc.perform` : 생성된 MockMvc를 통해 API를 테스트 한다.

#### 7-3. Component Scan 대상에 대한 문제
- HelloControllerTest와 같이 @WebMvcTest는 @Service를 스캔하지 않기 때문에 CustomOAuth2UserService를 찾을 수 없다는 에러가 발생
  - SecurityConfig를 스캔하면서 발생

- 위와 같은 문제를 해결하기 위해 스캔 대상에서 SecurityConfig를 제거하는 코드 추가

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/04c16fbb-c5ef-44a2-afb5-3d627fbefe39/image.png)

여기서도 마찬가지로 @WithMockUser를 사용해서 가짜로 인증된 사용자를 생성한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/3b16f68a-e552-4da1-97c0-1a90c5860abd/image.png)

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/8ef9ad6d-d3b7-42c9-a816-11fa4e02c199/image.png)

다시 테스트를 돌려보면 @EnableJpaAuditing로 인해 에러 발생, Application.java에서 @EnableJpaAuditing를 제거한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/ddeaf966-1c64-4ff7-85a8-c79028ec6a8c/image.png)

그리고 config 패키지에 JpaConfig를 생성하여 @EnableJpaAuditing를 추가한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/db798e97-b71a-4b2b-9dff-039f4eba7207/image.png)

그리고 다시 전체 테스트를 수행해보면, 모든 테스트가 통과했다 !!
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/3d1ede9f-c939-40c2-94c3-eb69c4c18606/image.png)

### 저번 장과 이번 장에서는 뭘 배웠나 ❓
- 스프링 부트 1.5와 스프링 부트 2.0에서 시큐리티 설정의 차이점
- 스프링 시큐리티를 이용한 구글/네이버 로그인 연동 방법
- 세션 저장소로 톰캣 / 데이터베이스 / 메모리 DB가 있으며 이 중 데이터베이스를 이용하는 이유
- ArgumentResolver 를 이용하면 어노테이션으로 로그인 세션 정보를 가져올 수 있다는 것
- 스프링 시큐리티 적용 시 기존 테스트 코드에서 문제 해결 방법