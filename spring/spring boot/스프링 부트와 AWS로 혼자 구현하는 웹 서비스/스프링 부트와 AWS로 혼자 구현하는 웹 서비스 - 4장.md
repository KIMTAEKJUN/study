> 더 많은 지식을 얻기 위해 이번엔 **스프링 부트와 AWS로 혼자 구현하는 웹 서비스(이동욱 지음)** 책을 읽고, 공부하고 정리했습니다.

## 머스테치로 화면 구성하기
### 1. 템플릿 엔진과 머스테치 소개
#### 1-1. 템플릿 엔진
- **지정된 템플릿 양식과 데이터**가 합쳐서 HTML 문서를 출력하는 소프트웨어


- 서버 템플릿 엔진 : **서버에서 구동**되는 템플릿 엔진(JSP, Freemarker 등등)
  - **서버에서 Java 코드로 문자열**을 만든 뒤 이 문자열을 HTML로 변환하여 **브라우저로 전달**한다.


- 클라이언트 템플릿 엔진 : 브라우저 위에서 작동하는 템플릿 엔진(React, Vue 등등)
  - 서버에서는 Json, Xml 형식의 데이터만 전달하고, 클라이언트에서 데이터를 혼합해 화면을 생성한다.

#### 1-2. 머스테치란 ❓
- **수많은 언어를 지원하는 가장 심플한 템플릿 엔진**
  - 루비, 자바스크립트, 파이썬, PHP, 자바, 펄, Go, ASP 등 현존하는 대부분 언어를 지원

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/5b1b7c0c-350f-42a7-a97b-8ce8e624e23c/image.png)

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/028cd8c5-7ec0-41ba-bfad-3ef1be8a1e38/image.png)

플러그인에서 Handlebars/Mustache를 설치하고, build.gradle에서 머스테치 스타터 의존성을 등록한다. 머스테치는 인텔리제이 커뮤니티 버전에서 무료로 사용할 수 있다.

#### 1-3. 머스테치 문법 정리
`{{> }}` : 현재 머스테치 파일을 기준으로 다른 파일을 가져온다.  
`{#posts}` : posts 라는 List를 순회한다.  
`{{변수명}}` : List에서 뽑아낸 객체의 필드를 사용한다.

### 2. 기본 페이지 만들기
머스테치의 파일 위치는 기본적으로 **src/main/resources/templates** 입니다.   
이 위치에 머스테치 파일을 두면 스프링 부트에서 자동으로 로딩합니다.   
위에서 말한 위치에다가 첫 페이지를 담당할 **index.mustache**를 생성한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/a947a773-fc53-44b1-8051-68fab3e8a9d9/image.png)

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/bd1d5db0-b723-4c12-8a5f-fef5d4d135bf/image.png)

이 머스테치에 URL을 매핑하기 위해 IndexController를 생성해 준다.

머스테치 스타터 덕분에 컨트롤러에서 문자열을 반환할 때 **앞의 경로와 뒤의 파일 확장자는 자동**으로 지정됩니다.

> 테스트 코드를 실행 중 머스테치 인코딩 문제가 있어서 스프링 부트 2.7.5 에서 2.6.13으로 다운그레이드를 하니 해결이 됐다 !

### 3. 게시글 등록 화면 만들기
HTML과 부트스트랩 관련한것들은 생략하겠습니다~~~  
index, header, footer.mustache 추가, 수정이 끝나면 컨트롤러도 수정을 합니다.  
페이지에 관련된 컨트롤러는 모두 IndexController를 사용합니다.  
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/20fa46ee-25e1-4db1-bde3-e5119329980a/image.png)

/posts/save를 호출하면 posts-save.mustache를 호출하는 메소드를 추가하였다.
이제 posts-save.mustache 파일 생성합니다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/d8e5c062-1b21-4bab-8db4-fdfd4e918b0f/image.png)

하지만, 게시글 등록 화면에 **등록 버튼은 기능이 없습니다**. API를 호출하는 JS가 전혀 없기 때문입니다.  
그래서 src/main/resources에 static/js/app 디렉토리를 생성하고, index.js를 만든다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/dd1f6d4b-5a66-48a3-9b31-4770e9651b12/image.png)

`var main = {~}` : main이라는 변수의 속성으로 function을 추가한 이유는 ❓
- 여러 사람이 참여하는 프로젝트에서 함수이름이 중복될 수 있다.
- 이러한 문제를 피하기 위해 index.js만의 유효한 범위를 만들어 사용한다.

`정적파일` :
- 스프링 부트는 기본적으로 src/main/resources/static에 위치한 정적파일들은 URL에서 **절대 경로**(/)로 설정된다.
- ex) src/main/resources/static/js/index.js (http://도메인/js/index.js

> 등록 버튼을 눌러도 게시글 등록이 되지않아서, 구글링을 해봤다.
파일을 만들 때 static/js/app 으로 만들고 posts-save.mustache 코드를 수정했더니 됐따!!!!!

### 4. 전체 조회 화면 만들기
전체 조회를 위해 index.mustache의 UI를 변경했다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/5dfe5d67-3a5d-48d6-bf4b-f538e7a9977b/image.png)

이제 Controller, Service, Repository 코드를 작성해 준다.
먼저 기존에 있던 PostsRepository 인터페이스에 쿼리가 추가된다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/8ae85584-5a0c-4f5c-8c4b-d365e5e2b266/image.png)

Spring Data JPA에서 제공하지 않는 메소드는 위처럼 쿼리로 작성해도 되는 것을 보여드리고자 @Query를 사용했다.

Repository 다음으로 PostsService에 코드를 추가한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/d0e74fc2-5ff0-43b6-9060-1aa8b84144fa/image.png)

`@Transactional` : readOnly = true를 주면 **트랜잭션 범위는 유지**하되, 조회 기능만 남겨두어 **조회 속도가 개선**된다.

아직 PostsListResponseDto 클래스가 없기 때문에 생성해야합니다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/ee7c737b-6386-4096-872e-44a27e582e8e/image.png)

마지막으로 Controller를 변경해 준다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/a61e5d39-0839-4b81-a752-30b2af786124/image.png)

`Model` : 서버 템플릿 엔진에서 사용할 수 있는 객체를 저장할 수 있습니다.

### 5. 게시글 수정, 삭제 화면 만들기
#### 5-1. 게시글 수정
게시글 수정 API는 이미 만들었으니, 해당 API로 요청하는 화면을 개발한다.
posts-update.mustache 파일을 만들어 준다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/0c10aa5b-6ebb-4846-8fd6-c882812b7330/image.png)

`{{ . }}` : 머스테치는 객체의 필드 접근 시 점(Dot)으로 구분합니다.  
`readonly` : input 태그에 읽기 가능만 허용하는 속성입니다.

그리고 btn-update 버튼을 클릭하면 update 기능을 호출할 수 있게 index.js 파일에도 update function을 하나 추가하겠습니다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/463b7035-47eb-40dc-a569-dfc7b9cb7b20/image.png)

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/0c261ff4-b1a5-41f5-87e2-8963731dfb67/image.png)

마지막으로 전체 목록에서 **수정 페이지로 이동할 수 있게** 페이지 이동 기능을 추가해 보겠습니다. index.mustache 코드를 다음과 같이 '살짝' 수정합니다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/8dde5604-b908-4a2c-a969-435081cf023f/image.png)

IndexController에 다음과 같이 메소드를 추가한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/e6a6c8ea-cdc7-4d82-976b-fcfea8c56cdd/image.png)

#### 5-2. 게시글 삭제
삭제 기능을 구현 해보기 전, 삭제 버튼을 수정 화면에 추가한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/0eb346f2-6eff-4387-b0f2-bccb26ba111d/image.png)

삭제 이벤트를 진행할 JS 코드도 추가해 준다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/fc95acbd-466b-436c-bb73-fdf2c2f2e4ac/image.png)

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/c1ff3241-bbbd-4147-a62d-961abf1060a7/image.png)

이제 삭제 API를 만들어 준다. 먼저 서비스 메소드임
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/ed6c938e-88c0-4376-949a-9afc217177cd/image.png)

`postsRepository.delete(posts)` : JpaRepository에서 이미 delete 메소드를 지원하고 있으니 이를 활용한다. 엔티티를 파라미터로 삭제할 수도 있고, deleteById 메소드를 이용하면 id로 삭제할 수도 있고, 존재하는 Posts인지 확인을 위해 엔티티 조회 후 그대로 삭제합니다.

서비스에서 만든 delete 메소드를 컨트롤러가 사용하도록 코드를 추가한다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/077911c3-f30c-4694-a522-0120b442bb23/image.png)

수정/삭제 기능까지 완성 !!!!!!!!!!!!

### 이번 장에서는 뭘 배웠나 ❓
- 서버 템플릿 엔진과 클라이언트 템플릿 엔진의 차이
- 머스테치의 기본 사용 방법
- 스프링 부트에서의 화면 처리 방식
- js/css 선언 위치를 다르게 하여 웹사이트의 로딩 속도를 향상하는 방법
- js 객체를 이용하여 브라우저의 전역 변수 충돌 문제를 회피하는 방법