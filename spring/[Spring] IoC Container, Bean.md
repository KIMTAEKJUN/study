> 백기선님의 **"스프링 프레임워크 입문"** 인프런 강의를 수강한 후, 여러 글들을 참고하여 쓴 글입니다. 
문제가 될 시 삭제하겠습니다.

## IoC(Inversion of Control) Container
**컨테이너(Container)**는 **보통 인스턴스의 생명주기를 관리**하며, **생성된 인스턴스들에게 추가적인 기능을 제공**하도록 하는 것이라 할 수 있다. **스프링**에도 **객체를 생성하고 관리하고 책임지고 의존성을 관리해주는 컨테이너**가 있는데, 그것이 바로 **IoC 컨테이너(=스프링 컨테이너)** 입니다.

**인스턴스 생성부터 소멸까지의 인스턴스 생명주기 관리를 개발자가 아닌 컨테이너가 대신 해줍니다.
객체관리 주체가 프레임워크(Container)가 되기 때문에 개발자는 로직에 집중할 수 있는 장점이 있습니다.**

### Ioc 컨테이너의 핵심적인 2가지 클래스
**1. BeanFactory**
**자바 객체(bean) 인스턴스**를 **생성, 설정, 관리**하는 **실질적인 컨테이너**이다.

**2. ApplicationContext**
**BeanFactory를 상속**받고 있다. **BeanFactory의 확장 버전**이다. **상속받아서 구현한 대표적인 차이점**은 **BeanFactory는 지연로딩**, **ApplicationContext는 pre로딩**이다.

**두 클래스 모두 Bean을 생성**하고, **관리하는 클래스**이다.
<br>

## 빈(Bean)
**Ioc 컨테이너가 관리하는 객체**를 **빈(Bean)**이라고 하고,

**스프링**에서 **빈(Bean)**은 보통 **싱글톤(Singleton)으로 존재**한다.
**싱글톤(Singleton)** : **어떤 클래스가 최초 한번만 메모리를 할당**하고(Static) **그 메모리에 객체를 만들어 사용**하는 **디자인 패턴**

**new 연산자로 생성하는 객체는 빈(Bean)**이 아니고, **ApplicationContext.getBean()으로 얻어질 수 있는 객체**는 **빈(Bean)**이다.

즉, **스프링**에서의 **빈(Bean)**은 **ApplicationContext가 알고있는 객체**, **ApplicationContext가 만들어서 그 안에 담고있는 객체를 의미**한다.
<br>

## 어떻게 IoC 컨테이너에 빈(Bean)을 등록할까?

### 1. Component Scanning
**스프링**에서 **@ComponentScan 어노테이션**과 **@Component 어노테이션**을 **사용해서 빈을 등록하도록 하는 방법**이다.

간단히 말하면 **@ComponentScan 어노테이션**은 **어느 지점부터 컴포넌트를 찾으라고 알려주는 역할**을 하고 **@Component는 실제로 찾아서 빈으로 등록할 클래스를 의미**한다.

**IoC 컨테이너**를 만들고 **그 안에 빈을 등록할때 사용하는 인터페이스**들을 **라이프 사이클 콜백**이라고 부른다.

**라이프 사이클 콜백** 중에는 **@Component 어노테이션**을 찾아서 **이 어노테이션**이 붙어있는 **모든 클래스의 인스턴스를 생성**해 **빈으로 등록하는 작업을 수행하는 어노테이션 프로세서가 등록**돼있다.

**스프링 부트 프로젝트**에서 **@ComonentScan 어노테이션이 붙어있는 클래스**가 이에 해당한다.

다음은 **스프링**의 **PetClinic 예제 코드**이다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/22ff4058-14d6-4627-9616-9a749b02309a/image.png)

**PetClinicApplication 클래스**에 **@SpringBootApplication 어노테이션**이 붙어있는데
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/75b6e4d2-8211-48bd-8ea3-45e94cdd623f/image.png)

**이 어노테이션은 내부적**으로 **@ComponentScan 어노테이션을 사용**한다.
**이 @ComponentScan 어노테이션**은 **어디서부터 컴포넌트를 찾아볼 것인지 알려주는 역할**을 한다.

**@ComponentScan이 붙어있는 클래스**가 있는 **패키지에서부터 모든 하위 패키지의 모든 클래스**를 훑어보며 **@Component 어노테이션(또는 @Component 어노테이션을 사용하는 다른 어노테이션)이 붙은 클래스**를 찾는다.

**스프링**이 **IoC 컨테이너를 만들때 위와 같은 과정**을 거쳐 **빈(Bean)으로 등록**해주는 것이다.

**스프링**의 **PetClinic 예제 코드**에서
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/7057c034-7fe8-4fce-9b17-6c5981b1013c/image.png)

**위의 클래스**는 **@Controller 어노테이션**이 붙어있는데
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/2039ba19-c486-47fe-9e3e-96751155895a/image.png)

**내부적**으로 **@Component 어노테이션을 사용**한다.
즉, **OwnerController는 스프링**에 의해 **IoC 컨테이너에 빈으로 등록**된다.

### 빈(Bean) 설정파일에 직접 빈(Bean)을 등록
위와 같이 **@Component 어노테이션을 사용**하는 방법 말고도 **빈(Bean) 설정파일**에 **직접 빈(Bean)으로 등록**할 수 있다.
**빈(Bean) 설정파일**은 **XML과 자바 설정파일로 작성**할 수 있는데 **최근 추세는 자바 설정파일**을 좀 더 많이 사용한다.

**자바 설정파일은 자바 클래스를 생성해서 작성**할 수 있으며 일반적으로 **xxxxConfiguration와 같이 명명**한다.
그리고 **클래스에 @Configuration 어노테이션**을 붙인다.
그 안에 **@Bean 어노테이션을 사용해 직접 빈(Bean)을 정의**한다.
```java
@Configuration
public class SampleConfiguration {
    @Bean
    public SampleController sampleController() {
        return new SampleController;
    }
}
```
**sampleController()에서 리턴되는 객체**가 **IoC 컨테이너 안에 빈(Bean)**으로 등록된다.

물론 이렇게 **빈(Bean)을 직접 정의해서 등록**하면 **@Component 어노테이션**을 붙이지 않아도 된다.

**@Configuration 어노테이션**을 보면 **이 어노테이션도 @Component를 사용**하기 때문에 **@ComponentScan의 스캔 대상**이 되고 그에 따라 **빈(Bean) 설정파일**이 읽힐때 그 안에 **정의한 빈(Bean)들이 IoC 컨테이너에 등록**되는 것이다.
<br>

## 참고했던 블로그🙇‍♂️
[빅팜님의 티스토리 글](https://leveloper.tistory.com/m/33)
[coco3o님의 티스토리 글](https://dev-coco.tistory.com/m/80)
[gillog님의 벨로그 글](https://velog.io/@gillog/Spring-Bean-%EC%A0%95%EB%A6%AC)
[버터필드님의 티스토리 글](https://atoz-develop.tistory.com/m/entry/Spring-%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B9%88Bean%EC%9D%98-%EA%B0%9C%EB%85%90%EA%B3%BC-%EC%83%9D%EC%84%B1-%EC%9B%90%EB%A6%AC)