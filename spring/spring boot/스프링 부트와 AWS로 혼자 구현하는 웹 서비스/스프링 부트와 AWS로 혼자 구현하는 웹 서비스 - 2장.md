> 더 많은 지식을 얻기 위해 이번엔 **스프링 부트와 AWS로 혼자 구현하는 웹 서비스(이동욱 지음)** 책을 읽고, 공부하고 정리했습니다.

## 스프링 부트에서 테스트 코드를 작성하자
### 1. 테스트 코드 소개

- **TDD**와 **단위 테스트**는 **다른 이야기**다.

- **TDD**는 **테스트가 주도하는 개발**이고, **테스트 코드를 먼저 작성**한다.
- 반면, **단위 테스트**는 TDD의 첫 번째 단계인 **기능 단위의 테스트 코드를 작성**한다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/ac0b5aee-6540-4142-b6c8-d57a1dd2c651/image.gif)

- 순서
1. 항상 실패하는 테스트를 먼저 작성하고(RED)
2. 테스트가 통과하는 프로덕션 코드를 작성하고(Green)
3. 테스트가 통과하면 프로덕션 코드를 리팩토링합니다(Refactor)

#### 1-1. 테스트 코드는 왜 작성해야 할까 ❓

- **단위 테스트**는 개발단계 초기에 문제를 발견하게 도와줍니다.
- **단위 테스트**는 개발자가 나중에 코드를 리팩토링하거나 라이브러리 업그레이드 등에서 기존 기능이 올바르게 작동하는지 확인할 수 있습니다.(예, 회귀 테스트)
- **단위 테스트**는 기능에 대한 불확실성을 감소시킬 수 있습니다.
- **단위 테스트**는 시스템에 대한 실제 문서를 제공합니다. 즉, 단위 테스트 자체가 문서로 사용할 수 있습니다.

테스트 코드를 작성하면 더는 사람이 눈으로 검증하지 않게 **자동검증**이 가능하고, **개발자가 만든 기능을 안전하게 보호**해준다.

<br>

### 2. 테스트 코드 작성하기
> Application 클래스는 앞으로 만들 프로젝트의 **메인 클래스**가 된다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/ddb18848-adb4-4b68-9539-e06eb006bcb4/image.png)

`@SpringBootApplication` : 스프링 부트의 자동 설정, 스프링 빈 읽기와 생성을 모두 자동으로 설정한다. 특히 **이 위치부터 읽어**가기 때문에 항상 **프로젝트의 최상단**에 위치해야만 한다.  
`SpringApplication.run` : 별도로 외부에 WAS를 두지 않고 애플리케이션을 실행할 때 내부에서 WAS를 실행한다. 항상 서버에 **톰캣을 설치할 필요가 없게 되고**, 스프링 부트로 만들어진 Jar 파일로 실행된다.

내장 WAS를 사용하는 이유는 ❓
'**언제 어디서나 같은 환경에서 스프링 부트를 배포**'할 수 있기 때문이다. 외장 WAS를 쓴다고 하면 종류와 버전, 설정을 일치시켜야 하고, 새로운 서버를 추가하면 모든 서버에 같은 환경을 구축해야 하기 때문이다.

#### 2-1. 컨트롤러 테스트

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/a5ee187f-a3e4-4da2-8f7c-79a0d4bbc1ef/image.png)

HelloController이라는 클래스를 생성하였다.

`@RestController` : 컨트롤러를 JSON으로 반환할 수 있게 만들어준다.  
`@GetMapping` : HTTP Method인 Get의 요청을 받을 수 있는 API를 만들어 줍니다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/ffa1f5a5-983e-4aeb-8aaf-a77cb41b99f1/image.png)

HelloController의 테스트 코드를 작성했다.

`@RunWith(SpringRunner.class)` : 스프링 부트 테스트와 JUnit 사이에 연결자 역할을 합니다.  
`@WebMvcTest(controllers = HelloController.class)` : 여러 스프링 테스트 어노테이션 중, Spring Mvc에 집중할 수 있는 어노테이션입니다.  
`@Autowired` : 스프링이 관리하는 빈(Bean)을 주입 받습니다.  
`private MockMvc mockMvc` : 웹 API를 테스트할 때 사용하고, 스프링 MVC 테스트의 시작점입니다.  
`mockMvc.perform(get("/hello"))` : MockMvc를 통해 /hello 주소로 HTTP GET 요청을 합니다.  
`.andExpect(status().isOk())` : HTTP Header의 Status를 검증하고, 흔히 알고 있는 200, 404, 500 등의 상태를 검증합니다.  
`.andExpect(content().string(hello)` : 결과를 검증하고, Controller에서 "hello"를 리턴하기 때문에 이 값이 맞는지 검증합니다.

> 계속 테스트할 때 Execution failed for task ':test'. 라는 에러가 떠, 구글링을 해본 결과
Preferences 에서 빌드 도구 -> Gradle 항목에서 다음을 사용하여 빌드 및 실행, 다음을 사용하여 테스트 실행을 Gradle(디폴드)에서 IntelliJ IDEA로 변경하면 해결이 됩니다.

#### 2-2. 롬복 기능 테스트
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/31e8d8f5-ce46-4a4e-8528-6ac1c8d1694f/image.png)

HelloResponseDto라는 클래스를 생성하였다.

`@Getter` : 선언된 모든 필드의 get 메소드를 생성해 줍니다.  
`@RequiredArgsConstructor` : 선언된 모든 final 필드가 포함된 생성자를 생성해 줍니다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/48a76da9-4e21-4635-acd3-57de4ee83e56/image.png)

HelloResponseDto의 테스트 코드를 작성했다.

`assertThat` : assertj라는 테스트 검증 라이브러리의 검증 메소드이고, 검증하고 싶은 대상을 메소드 인자로 받는다.  
`isEqualTo` : assertj의 동등 비교 메소드이고, assertThat에 있는 값과 isEqualTo의 값을 비교해서 같을 때만 성공입니다.

`JUnit`의 `assertThat()`보다 `assertj`의 `assertThat()`이 더 좋은 이유❓
- assertj는 JUnit과 다르게 추가적인 라이브러리가 필요하지 않다.
- 자동완성이 좀 더 확실하게 지원된다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/c010770f-d7f7-4729-a5b0-7b29187b70f2/image.png)

HelloController에도 새로 만든 ResponseDto를 사용하기 위해 코드를 추가했다.

`@RequestParam` : 외부에서 API로 넘긴 파라미터를 가져오는 어노테이션입니다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/56efb668-cf81-4fb8-bce1-bf4269e27368/image.png)

HelloControllerTest에 테스트 코드를 추가했다.

`param` : API 테스트할 때 사용될 요청 파라미터를 설정하고, 값은 String만 허용하기 때문에 숫자/날짜 등의 데이터도 등록할 때 문자열로 변경해야만 가능하다.
`jsonPath` : JSON 응답값을 필드별로 검증할 수 있고, $를 기준으로 필드명을 명시한다. $ root를 명시하고 .을 통해 원하는 필드명을 검증할 수 있다.

### 이번 장에서는 뭘 배웠나 ❓
- TDD와 단위 테스트란
- 스프링 부트 환경에서 테스트 코드를 작성하는 방법
- 자바의 필수 유틸 롬복의 사용법