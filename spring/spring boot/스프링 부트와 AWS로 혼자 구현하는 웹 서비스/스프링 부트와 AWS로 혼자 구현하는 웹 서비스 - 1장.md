> 더 많은 지식을 얻기 위해 이번엔 **스프링 부트와 AWS로 혼자 구현하는 웹 서비스(이동욱 지음)** 책을 읽고, 공부하고 정리했습니다.

## 인텔리제이로 스프링 부트 시작하기
### 1. 인텔리제이 소개
- 강력한 추천 기능(Smart Completion)
- 훨씬 더 다양한 리팩토링과 디버깅 기능
- 이클립스의 깃(Git)에 비해 훨씬 높은 자유도
- 프로젝트 시작할 때 인덱싱을 하여 파일 비롯한 자원들에 대한 빠른 검색 속도
- HTML과 CSS, JS, XML에 대한 강력한 기능 지원
- 자바, 스프링 부트 버전업에 맞춘 빠른 업데이트

> 실제로도 많은 IT 서비스 회사(네카라쿠베우 등등)에서는 **인텔리제이 얼티메이트(유료)를 공식 IDE**로 사용하고 있습니다.

<br>

### 2. 인텔리제이에서 프로젝트 생성하기
#### 2-1. 프로젝트 생성
> 보통 **스프링 이니셜라이저**(https://start.spring.io) 를 통해서 진행할 수 있다. **스프링 이니셜라이저**를 사용하게 되면 **build.gradle** 의 코드가 어떠한 역할인지 **스프링 이니셜라이저** 외에 추가로 **의존성** 추가가 필요할 때 어떻게 해야 할지 등을 모르는 상태로 개발하는 경우가 있습니다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/e746a05c-a097-40e9-8992-cfb93ce7c85d/image.png)

위에서와 같이 **그룹Id**와 **아티팩트**를 등록합니다. 특히 **아티팩트**는 프로젝트의 이름이 되기 때문에 원하는 이름을 작성해 주면 됩니다.

책에선 프로젝트 유형을 **그레이들(Gradle)** 로 선택해 프로젝트를 생성합니다.
저는 **인텔리제이 얼티메이트** 버전이라 편하게 **스프링 이니셜라이저**로 만들었습니다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/ef94ae3d-8acb-4306-b30a-c9fb95b47126/image.png)

이렇게 모든 설정이 끝나면 **그레이들(Gradle)** 기반의 자바 프로젝트가 생성됩니다.

#### 2-2. build.gradle 설정
```
plugins {
	id 'org.springframework.boot' version '2.7.5'
	id 'io.spring.dependency-management' version '1.0.15.RELEASE'
	id 'java'
}

group = 'com.taekjun.book'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
	mavenCentral()
	jcenter()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
	useJUnitPlatform()
}
```
저는 **스프링 이니셜라이저**로 프로젝트를 만들었기 때문에 또 다른 설정은 안하겠습니다.
위 코드는 제가 설정한 **build.gradle** 코드이고, 밑 코드가 책에서 설정한 **build.gradle** 코드 입니다.
<br>

```
buildscript {
    ext {
        springBootVersion='2.1.7.RELEASE'
    }
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-
        gradle-plugin:${springBootVersion}")
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'

group 'org.example.book'
version '1.0-SNAPSHOT'
sourceCompatibility = 1.8

repositories {
    mavenCentral()
}

dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.projectlombok:lombok')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
```
- `buildscript` : 프로젝트의 플러그인 **의존성 관리** 설정을 하는 곳
    - `ext` : **build.gradle**에서 사용하는 전역변수를 설정
    - `dependencies` : spring-boot-gradle-plugin라는 스프링 부트 그레이들 플러그인의 2.1.7.RELEASE를 **의존성**으로 받겠다는 의미


- `plugin` : **자바**와 **스프링 부트**를 사용하기 위한 플러그인
    - **컴파일**이나 **jar 파일 생성** 등을 해주는 플러그인


- `repositories` : 각종 **의존성(라이브러리)**들을 어떤 원격 저장소에서 받을 지 정함
    - 기본적으로 mavenGentral을 많이 사용하지만, 최근에는 **라이브러리 업로드 난이도** 때문에 jcenter도 많이 사용한다.


- `dependencies` : 프로젝트 개발에 필요한 의존성들을 선언하는 곳
    - 의존성을 선언할 때 특정 버전을 명시하지 않아야 buildscript에 작성한 'org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}'의 버전을 따라가게 되므로 버전 관리가 한 곳에 집중되고, 버전 충돌 문제도 해결된다.

    - `dependency configuration` : 선언된 의존성이 사용되는 특정범위
        - `Implementation` : 구현할 때만 사용
        - `compileOnly` : 컴파일할 때만 사용, 런타임시에는 X
        - `runtimeOnly` : 런타임시에만 사용
        - `testImplementation` : 테스트할 때만 사용

<br>

### 3. 인텔리제이와 깃허브 연동
> 프로젝트와 구인공고를 통해서, 버전 관리는 개발하면서 필수요소이다.
실제로 대부분의 IT서비스 회사는 깃을 통해 버전 관리를 하고 있습니다.
사실 깃 명령어가 더 익숙하지만, 책의 내용에 따라 인텔리제이를 통해서 연동을 시켜주겠습니다.

![IMG](https://velog.velcdn.com/images/kimtaekjun/post/b9534900-04a2-4ffa-ab11-fa7c8a9f6b88/image.png)

**Github에 프로젝트 공유**를 클릭하고 깃허브 저장소에 같은 이름으로 생성할 수 있다. 팝업창이 등장하고, **.idea** 디렉토리는 커밋하지 않습니다. 실행시 자동으로 생성되는 파일들이기 때문에 깃허브에 올리기에는 불필요 합니다.

`.gitignore` : 깃에서 특정 파일 혹은 디렉토리를 관리 대상에서 제외할 때 사용하는 파일이다.

### 이번 장에서는 뭘 배웠나 ❓
- 인텔리제이의 기본 사용법
- mavenCentral, jcenter 비교
- 스프링 부트 프로젝트와 그레이들 연동 방법
- 인텔리제이에서 깃허브 사용하는 방법