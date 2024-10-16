> 백기선님의 **"스프링 프레임워크 입문"** 인프런 강의를 수강한 후, 여러 글들을 참고하여 쓴 글입니다. 
문제가 될 시 삭제하겠습니다.

## AOP(Aspect Oriented Programming)
**AOP(Aspect Oriented Programming)**는 **관점 지향 프로그래밍**이라고 불린다. **관점 지향**은 쉽게 말해 **어떤 로직을 기준으로 핵심적인 관점**, **부가적인 관점**으로 나누어서 보고 **그 관점을 기준으로 각각 모듈화**하겠다는 것이다.
> **모듈화** : **어떤 공통된 로직**이나 **기능을 하나의 단위로 묶는 것**
> 
> **핵심적인 관점(Core Concerns)** : **업무 로직**을 **포함하는 기능** ( 우리가 적용하고자 하는 핵심 비즈니스 로직 )
> 
> **부가적인 관점(Cross-cutting Concerns)** : **핵심 기능**을 도와주는 **부가적인 기능** ( 핵심 로직을 실행하기 위해서 행해지는 데이터베이스 연결, 로깅, 파일 입출력 등 )

_**" AOP는 흩어진 관심사(Crosscutting Concerns)를 모듈화 할 수 있는 프로그래밍 기법 "**_

### AOP(Aspect Oriented Programming) 적용 방법
**1. 컴파일 타임 적용**
**컴파일 시점**에 **바이트 코드를 조작**하여 **AOP가 적용된 바이트 코드**를 생성하는 방법

**2. 로드 타임 적용**
**순수하게 컴파일**한 뒤, **클래스를 로딩**하는 시점에 **클래스 정보를 변경**하는 방법

**3. 런타임 적용**
**스프링 AOP**가 **주로 사용하는 방법**, A라는 클래스 타입의 Bean을 만들 때 A 타입의 Proxy Bean을 만들어 Proxy Bean이 Aspect 코드를 추가하여 동작하는 방법
<br>

## AOP 사용 예제
**스프링이 제공**하는 **어노테이션 기반의 AOP**를 직접 적용해보자
스프링 PetClinic 예제의 OwnerController의 메소드들에 메소드 성능을 측정하는 기능을 추가할 것이다

아래 코드와 같이 메소드들에 메소드 성능을 측정해주는 **@LogExecutionTime 어노테이션**을 만들어준다.
```java
package org.springframework.samples.petclinic.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
}

```

### LogExecutionTime 설명
**1. @Target(ElementType.METHOD)**
**어노테이션을 메소드에 사용할 것이라고 설정**한다.

**2. @Retention(RetentionPolicy.RUNTIME)**
**어노테이션이 RUNTIME까지 유지되도록 설정**한다.

어떤 메소드에 AOP를 적용할건지 알려주는 어노테이션을 정의하였다.
<br>

그 다음 성능을 측정하고자 하는 메소드에 **@LogExecutionTime 어노테이션**을 붙인다.
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/9eaf2da9-7f26-4d29-a493-a852eba59556/image.png)

이제 해당 메소드에 어떤 기능을 추가할 것인지를 알려주는 실제 aspect를 구현해야한다.

같은 패키지에 LogAspect 클래스를 만들고 아래와 같이 작성한다.
```java
package org.springframework.samples.petclinic.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Aspect
public class LogAspect {
	Logger logger = LoggerFactory.getLogger(LogAspect.class);

	@Around("@annotation(LogExecutionTime)")
	public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		// @LogExecutionTime 어노테이션이 붙어있는 타겟 메소드를 실행
		Object ret = joinPoint.proceed();

		stopWatch.stop();
		logger.info(stopWatch.prettyPrint());

		return ret;
	}
}

```
### LogAspect 설명
**1. Logger logger = LoggerFactory.getLogger(LogAspect.class);**
**slf4j로 로거**를 만든다.

**2. @Around("@annotation(LogExecutionTime)")**
**이 어노테이션을 붙인 메소드**에서는 **ProceedingJoinPoint 파라미터**를 받을 수 있다
**어노테이션의 value**를 **"@annotation(LogExecutionTime)"로 지정함**으로서
**joinPoint**는 **@LogExecutionTime를 붙인 타겟 메소드를 의미**하게 된다

**3. Object proceed = joinPoint.proceed();**
**타겟 메소드를 실행**한다
이 라인 앞 뒤로 StopWatch를 이용한 메소드 성능 측정 코드를 넣어준다
<br>

### 어플리케이션을 실행 시 결과
![IMG](https://velog.velcdn.com/images/kimtaekjun/post/25e548c0-ecf1-4d3e-9458-e4c68291e372/image.png)

위와 같이 OwnerController의 메소드 실행 시 LogAspect에 추가한 메소드 성능 측정 결과가 콘솔에 출력된다
<br>

## PSA(Portable Service Abstraction)
**PSA = 잘 만든 인터페이스**
**PSA가 적용된 코드**라면 나의 코드가 바뀌지 않고, **다른 기술**로 **간편**하게 바꿀 수 있도록 **확장성**이 좋고, 기술에 특화되어 있지 않는 코드를 의미
**스프링**은 **Spring Web MVC, Spring Transaction, Spring Cache 등**의 **다양한 PSA를 제공**

**1. Spring Web MVC**
```java
@Controller
class OwnerController {  
	
    do something.. 
    
@GetMapping("/owners/new")
@LogExecutionTime
public String initCreationForm(Map<String, Object> model) {	
	Owner owner = new Owner();	
    model.put("owner", owner);	
    return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
} 

@PostMapping("/owners/new")
@LogExecutionTime
public String processCreationForm(@Valid Owner owner, BindingResult result) {	
	if (result.hasErrors()) {		
    	return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;	
    } else {		
        this.owners.save(owner);		
        return "redirect:/owners/" + owner.getId();	
    }
} 
do something.. 
} // end OwnerController
```
일반 클래스에 @Controller 어노테이션을 사용하면 요청을 매핑할 수 있는 컨트롤러 역할을 수행하는 클래스가 됩니다
그 클래스에서는 @GetMapping과 @PostMapping 어노테이션을 사용해서 요청을 매핑할 수 있습니다

서블릿을 Low level 로 개발하지 않고도, Spring Web MVC를 사용하면 이렇게 서블릿을 간편하게 개발할 수 있습니다. 그 이유는 뒷단에 spring이 제공해주는 여러 기능들이 숨겨져 있기 때문입니다
즉, HttpServlet을 상속받고 doGet(), doPost()를 구현하는 등의 작업을 하지 않아도 되는 것입니다

Service Abstraction(서비스 추상화)의 목적 중 하나가 이러한 편의성을 제공하는 것입니다

또한, Spring Web MVC는 코드를 거의 그대로 둔 상태에서 톰캣이 아닌 다른 서버로 실행하는 것도 가능합니다

프로젝트의 spring-boot-starter-web 의존성 대신 spring-boot-starter-webflux 의존성을 받도록 바꿔주기만 하면 Tomcat이 아닌 netty 기반으로 실행하게 할 수 있습니다

이렇게 Spring Web MVC는 @Controller, @RequestMapping 과 같은 어노테이션과 뒷단의 여러가지 복잡한 인터페이스들
그리고 기술들을 기반으로 하여 사용자가 기존 코드를 거의 변경하지 않고, 웹 기술 스택을 간편하게 바꿀 수 있도록 해줍니다

**2. Spring Transaction**
Low level로 트랜잭션 처리를 하는 간단한 예제 코드를 보겠습니다.
```java
 try (Connection conn = DriverManager.getConnection(
 "jdbc:coco://127.0.0.1:5432/test", "coco", "password"); 
 Statement statement = conn.createStatement();
 ) {                          
 //start transaction block
 conn.setAutoCommit(false); //default true               
 
 String SQL = "INSERT INTO Employees " + 
 		      "VALUES (101, 20, 'Rita', 'Tez')";         
stmt.executeUpdate(SQL);                          

String SQL = "INSERTED INT Employees " + 
			 "VALUES (107, 22, 'Kita', 'Tez')"; 
stmt.executeUpdate(SQL);                          

// end transaction block, commit changes           
conn.commit();                          

// good practice to set it back to default true           
conn.setAutoCommit(true);                          

} catch(SQLException e) {             	               
	System.out.println(e.getMessage());                
    conn.rollback();             
}
```
conn.setAutoCommit(false);을 하여 자동커밋을 막아주고, 오류 없이 진행된다면 conn.commit();으로 커밋 될 것 입니다.
하지만, 2번째 SQL문에 INSERTED INT 의 오타로 인해 커밋 되지 않고 catch문으로 가게되어 
conn.rollback();으로 롤백 하는 코드입니다
 
위의 코드와 같이 Low level로 트랜잭션 처리를 하려면 명시적으로 setAutoCommit()과 commit(), rollback()을 호출해야 합니다
 
하지만 Spring이 제공하는 **@Transactional 어노테이션**을 사용하면 단순하게 메소드에 어노테이션을 붙여줌으로써 트랜잭션 처리가 간단하게 이루어집니다
```java
@Transactional(readOnly = true)
Employees findById(Integer id);
```
이 또한 PSA로써 다양한 기술 스택으로 구현체를 바꿀 수 있습니다

예를들어, JDBC를 사용하는 DatasourceTransactionManager, JPA를 사용하는 JpaTransactionManager, Hibernate를 사용하는 HibernateTransactionManager를 유연하게 바꿔서 사용할 수 있습니다
 
즉, 기존 코드는 변경하지 않은 채로 트랜잭션을 실제로 처리하는 구현체를 사용 기술에 따라 바꿀 수 있는 것입니다.

**3. Spring Cache**
Cache도 마찬가지로  JCacheManager, ConcurrentMapCacheManager, EhCacheCacheManager와 같은
여러가지 구현체를 사용할 수 있습니다
```java
@Transactional
@Cacheable("users")
List<User>findAllUser();
```
사용자는 **@Cacheable 어노테이션**을 붙여줌으로써 구현체를 크게 신경쓰지 않아도 필요에 따라 바꿔 쓸 수 있습니다
 
스프링은 이렇게 특정 기술에 직접적 영향을 받지 않게끔 객체를 POJO 기반으로 한번씩 더 추상화한 Layer를 갖고 있으며,
이를통해 일관성있는 Service Abstraction(서비스 추상화)를 만들어 냅니다
덕분에 코드는 더 견고해지고 기술이 바뀌어도 유연하게 대처할 수 있게 됩니다

<br>

## 참고했던 블로그🙇‍♂️
[새로비님의 티스토리 글](https://engkimbs.tistory.com/m/746)
[코딩하는 핑가님의 티스토리 글](https://ss-o.tistory.com/m/137)
[코드연구원님의 티스토리 글](https://code-lab1.tistory.com/m/193)
[버터필드님의 티스토리 글](https://atoz-develop.tistory.com/m/entry/Spring-%EC%8A%A4%ED%94%84%EB%A7%81-AOP-%EA%B0%9C%EB%85%90-%EC%9D%B4%ED%95%B4-%EB%B0%8F-%EC%A0%81%EC%9A%A9-%EB%B0%A9%EB%B2%95)
[coco3o님의 티스토리 글](https://dev-coco.tistory.com/83)