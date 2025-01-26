> 더 많은 지식을 얻기 위해 이번엔 **스프링 부트와 AWS로 혼자 구현하는 웹 서비스(이동욱 지음)** 책을 읽고, 공부하고 정리했습니다.

## 스프링 부트에서 JPA로 데이터베이스 다뤄보자
### 1. JPA 소개
#### 1-1. JPA 등장배경
- 패러다임 불일치
  - 관계형 데이터베이스는 **어떻게 데이터를 저장**할지에 초점이 맞춰진 기술, 객체지향 프로그래밍 언어는 메세지를 기반으로 **기능과 속성을 한 곳에서 관리**하는 기술이다.

  - JPA는 이런 문제점을 해결하기 위해 **중간에서 패러다임 일치**를 시켜주기 위한 기술이다.
  - 개발자는 **객체지향적으로 프로그래밍을 하고**, JPA가 이를 관계형 데이터베이스에 맞게 SQL를 생성해서 실행한다.

#### 1-2. Spring Data JPA
- JPA는 인터페이스이고, 사용하기 위해서는 구현체(Hibernate 등등)가 필요하다.


- JPA를 사용할 때는 구현체들을 직접 다루진 않고, 구현체들을 좀 더 쉽게 사용하고자 추상화시킨 **Spring Data JPA**라는 모듈을 이용하여 JPA를 다룬다.
  - 관계도 : JPA ← Hibernate ← Spring Data JPA


- Spring Data JPA의 장점
  - 구현체 교체의 용이성
    - **Hibernate 외에 다른 구현체로 쉽게 교체하기 위함**
  - 저장소 교체의 용이성
    - **관계형 데이터베이스 외에 다른 저장소로 쉽게 교체하기 위함**

### 2. 프로젝트에 Spring Data JPA 적용
#### 2-1. 요구사항 분석
- 게시판 웹 애플리케이션의 요구사항


- 게시판 기능
  - 게시글 조회
  - 게시글 등록
  - 게시글 수정
  - 게시글 삭제
- 회원 기능
  - 구글 / 네이버 로그인
  - 로그인한 사용자 글 작성 권한
  - 본인 작성 글에 대한 권한 권리

#### 2-2. build.gradle에 의존성 등록
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/7b5ef728-0cc1-4267-8c8a-50beeb41deb2/image.png)

`spring-boot-starter-data-jpa` : 스프링 부트용 Spring Data Jpa 추상화 라이브러리이고, 버전에 맞춰 자동으로 JPA관련 라이브러리들의 버전을 관리해 줍니다.
`h2` : 인메모리 관계형 데이터베이스이고, 별도의 설치가 없이 프로젝트 의존성만으로 관리할 수 있습니다.

#### 2-3. Posts domain 생성
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/33dd6f4d-44c2-4ecd-82af-8ae5fddc8b13/image.png)

`@Entity` : 테이블과 링크될 클래스임을 나타내고, 클래스의 이름을 테이블 이름으로 매칭합니다.  
`@Id` : 해당 테이블의 PK 필드를 나타냅니다.  
`@GeneratedValue` : PK의 생성 규칙을 나타내고, 스프링 부트 2.0 에서는 GenerationType.IDENTITY 옵션을 추가해야만 auto_increment가 됩니다.  
`@Column` : 테이블의 칼럼을 나타내며 굳이 선언하지 않더라도 해당 클래스의 필드는 모두 칼럼이 되고, 기본값 외에 추가로 변경이 필요한 옵션이 있으면 사용한다.  
`@NoArgsConstructor` : 기본 생성자 자동 추가, public Posts() {}와 같은 효과  
`@Getter` : 클래스 내 모든 필드의 Getter 메소드를 자동생성  
`@Builder` : 해당 클래스의 빌더 패턴 클래스를 생성, 생상자 상단에 선언 시 생성자에 포함된 필드만 빌더에 포함

Posts 클래스에 `@Setter`가 없는 이유 ❓
- 해당 클래스의 인스턴스 값들이 언제 어디서 변해야하는지 코드상으로 명확하게 구분할 수가 없어, 차후 기능 변경 시 정말 복잡해지기 때문이다.

- Entity 클래스에서는 절대 Setter 메소드를 만들지 않고, 해당 필드의 값 변경이 필요하면 명확히 그 목적과 의도를 나타낼 수 있는 메소드를 추가해야만 한다.


- Setter가 없는 이 상황에서 어떻게 값을 채워 DB에 삽입해야 할까 ❓
  - 생성자를 통해 최종값을 채운 후 DB에 삽입하고, 값 변경 시 해당 이벤트에 맞는 메소드를 호출

### 3. Spring Data JPA 테스트 코드 작성
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/da97da47-5469-43e5-9b7b-491843e9dd32/image.png)

`@After` : JUnit에서 단위 테스트가 끝날 때마다 수행되는 메소드를 지정, 전체 테스트를 수행할 때 테스트간 데이터 침범을 막기 위해 사용한다.  
`postsRepository.save` : 테이블 posts에 insert/update 쿼리를 실행, id 값이 있다면 update, 없다면 insert 쿼리가 실행된다.  
`postsRepository.findAll` : 테이블 posts에 있는 모든 데이터를 조회해오는 메소드입니다.

> 에러가 발생하여, 구글링을 해본 결과 스프링 부트 2.1.10 이후에는 좀 더 복잡한 설징이 필요하다.
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL57Dialect
spring.jpa.properties.hibernate.dialect.storage_engine=innodb
spring.datasource.hikari.jdbc-url=jdbc:h2:mem://localhost/~/testdb;MODE=MYSQL

### 4. 등록/수정/조회 API 만들기
#### 4-1. 비지니스 로직은 어디서 처리해줄까용 ❓
- Spring 웹 계층
  ![IMG](https://velog.velcdn.com/images/kimtaekjun/post/b488ce93-0f97-4a34-8b3c-2f44f5f9383a/image.png)


- Web Layer
  - 흔히 사용하는 @Controller와 JSP/Freemarker 등의 뷰 템플릿 영역이다
    필터(@Filter), 인터셉터, 컨트롤러 어드바이스(@ControllerAdvice)등 외부 요청과 응답에 대한 전반적인 영역을 이야기 한다.

- ServiceLayer
  - @Service에 사용되는 서비스 영역이다.
  - 일반적으로 Controller와 Dao의 중간 영역에서 사용된다.
  - @Transactional이 사용되어야 하는 영역이다.

- Repository Layer
  - Database와 같이 데이터 저장소에 접근하는 영역

- Dtos
  - Dto(Data Transfer Obejct)는 계층 간에 데이터 교환을 위한 객체를 이야기 하며, Dtos는 이들의 영역을 얘기한다.

- Domain Model
  - 도메인이라 불리는 개발 대상을 모든 사람이 동일한 관점에서 이해할 수 있고 공유할 수 있도록 단순화시킨 것을 도메인 모델이라고 한다.
  - @Enitiy가 사용된 영역 역시 도메인 모델이다.

- **Service 계층에서는 트랜잭션과 도메인 간 순서 보장의 역할을 하고, 비지니스 로직은 처리하지 않는다.**

- **비지니스 처리는 Domain 에서 해준다.**

#### 4-2. Dto 클래스를 추가하는 이유는 ❓
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/c401d2a6-ac33-490d-99d2-21d17bf85c6e/image.png)

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/42a81781-3e8d-41f7-9970-d7a2a410c796/image.png)

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/07e8a4cd-6e4e-421a-b1de-0c1fffe941ad/image.png)


- 여기서 Entity 클래스와 거의 유사한 형태임에도 Dto 클래스를 추가로 생성했습니다. 하지만, 절대로 Entity 클래스를 Request/Response 클래스로 사용해서는 안된다.
  - Entity를 dto처럼 사용하면 사소한 변경인 화면 변경을 위해 테이블과 연결된 Entity를 수정하는 것은 너무 큰 변경이기 때문

#### 4-3. PostsApiControllerTest
```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @After
    public void tearDown() throws Exception {
        postsRepository.deleteAll();
    }

    @Test
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
        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, requestDto, Long.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
    }
}
```
`TestRestTemplate` : REST방식으로 개발한 API의 Test를 최적화 하기 위해 만들어진 클래스  
`ResponseEntity<>` : 사용자의 Http Request에 대한 응답 데이터를 가지고 있고, Http Status, Header, Body를 포함한다.  
`restTemplate.postForEntity()` : POST 요청을 보내고 헤더에 저장된 URI를 결과로 반환받는다.

#### 4-4. 수정/조회 기능
PostsApiController에 Update와 findById를 추가
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/43b37f40-bdb4-4283-a974-193b10daca60/image.png)

PostsResponseDto를 만듬
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/cf4e4d60-f405-4fdd-a0e6-e1a8856a7140/image.png)


PostsUpdateRequestDto를 만듬
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/dfa14d07-193c-4327-9fc8-e3c169964fa1/image.png)

Posts에 update를 추가
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/2531b208-47e0-4ff9-a54f-51d1345a987b/image.png)

PostsService에 update와 findById를 추가
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/b7f94964-8e19-423c-b46f-bed036efd84d/image.png)

update 기능에서 데이터베이스에 **쿼리를 날리는 부분이 없습니다.**
이게 가능한 이유는 JPA의 영속성 컨텍스트 때문입니다.
영속성 컨텍스트란 ❓ **엔티티를 영구 저장하는 환경**입니다.

> H2 콘솔 접속이 되지않아, 구글링을 통해 찾아본 결과
URL에 jdbc:h2:mem:testdb 대신 이렇게 적어서 jdbc:h2:mem://localhost/~/testdb;MODE=MYSQL으로 접속 성공 !

### 5. JPA Auditing으로 생성시간/수정시간 자동화하기
보통 엔티티에는 해당 데이터의 생성시간과 수정시간을 포함한다.
언제 만들어졌는지, 언제 수정되었는지 등 차후 유지보수에 있어서 매우 중요한 정보이기 때문이다.

#### 5-1. LocalDate 사용
Java8부터 LocalDate와 LocalDateTime이 등장했습니다.
Java의 기본 날짜 타입인 Date의 문제점을 제대로 고친 타입이라 Java8일 경우 무조건 써야 한다고 생각하면 됩니다.

#### 5-2. BaseTimeEntity 추상 클래스
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/3f4ffb9b-6fb4-4992-a45a-85f8fbdd4346/image.png)

BaseTimeEntity 클래스는 모든 Entity의 상위 클래스가 되어 **Entity들의 createdDate, modifiedDate를 자동으로 관리하는 역할**입니다.

`MappedSuperclass` : JPA Entity 클래스들이 BaseTimeEntity을 상속할 경우 필드들도 칼럼으로 인식하도록 합니다.  
`@EntityListeners(AuditingEntityListener.class)` : BaseTimeEntity 클래스에 Auditing 기능을 포함시킵니다.  
`@CreatedDate` : Entity가 생성되어 저장될 때 시간이 자동 저장됩니다.
`@LastModifiedDate` : 조회한 Entity의 값을 변경할 때 시간이 자동 저장됩니다.

그리고 Posts 클래스가 BaseTimeEntity를 상속받도록 변경한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/15066d8e-f8dd-4ea0-88bc-48a7224c5fd7/image.png)

마지막으로 JPA Auditing 어노테이션들을 모두 활성화할 수 있도록 Application 클래스에 활성화 어노테이션 하나를 추가하겠습니다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/e0a22327-1b71-4566-adbe-966e52c6a050/image.png)

### 이번 장에서는 뭘 배웠나 ❓
- JPA / Hibernate / Spring Data JPA의 관계
- Spring Data JPA를 이용하여 관계형 데이터베이스를 객체지향적으로 관리하는 방법
- JPA의 더티 체킹을 이용하면 Update 쿼리 없이 테이블 수정이 가능하다는 것
- JPA Auditing을 이용하여 등록/수정 시간을 자동화하는 방법