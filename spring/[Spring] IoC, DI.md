> 백기선님의 **"스프링 프레임워크 입문"** 인프런 강의를 수강한 후, 여러 글들을 참고하여 쓴 글입니다. 
문제가 될 시 삭제하겠습니다.

## IoC(Inversion of Control)
**IoC(Inversion of Control)**란 "제어의 역전" 이라는 의미로, 말 그대로 **메소드나 객체의 호출작업을 개발자가 결정하는 것이 아니라, 외부에서 결정되는 것을 의미**한다.

객체의 **의존성을 역전시켜 객체 간의 결합도를 줄이고 유연한 코드를 작성**할 수 있게 하여 **가독성 및 코드 중복, 유지 보수를 편하게** 할 수 있게 한다.
<br>

### 예제 코드
일반적으로 의존성에 대한 제어권은 객체 자기 자신이 갖는다. 아래의 코드는 OwnerController이라는 클래스에서 OwnerRepository 객체를 불러오는 예제이다. 의존관계는 간단히 말해 new라는 키워드를 통해 생성된다. 
```java
class OwnerController {
	private OwnerRepository repository = new OwnerRepository();
}
```

<br>
OwnerControllerTest라는 클래스에서 OwnerRepository 객체를 생성한 뒤 OwnerController 클래스의 생성자로 주입시켜 준다. 
여기서는 OwnerController이 직접 OwnerRepository를 생성하는 것이 아니라 생성자로 주입받는다.

```java
// IoC
class OwnerController {
	private OwnerRepository repo;
    
    public OwnerController(OwnerRepository repo) {
    this.repo = repo;
    }
    
    // repo를 사용합니다.
}

class OwnerControllerTest {
	@Test
    public void create() {
    	OwnerRepository repo = new OwnerRepository();
        // OwnerRepository란 의존성을 OwnerController한테 의존성 주입
        OwnerController controller = new OwnerController(repo);
    }
}
```
이처럼 첫 번째 예시에서는 OwnerRepository 객체의 제어권이 OwnerController에게 있었다면, 두 번째 예시에서는 OwnerController에게 있는 것이 아니라 OwnerControllerTest에게 있다.
<br>

## DI(Dependency Injection)
**DI(Dependency Injection)**란 **스프링이 다른 프레임워크와 차별화되어 제공하는 의존 관계 주입 기능**으로,
**객체를 직접 생성하는 게 아니라 외부에서 생성한 후 주입 시켜주는 방식**이다. (**new 연산자를 이용**해서 **객체를 생성하는 것이라고 생각**하면 된다)

### 의존성을 주입하는 방법
- **생성자**에서 주입
- **필드**에서 주입
- **setter** 에서 주입

**스프링**에서 **@Autowired 어노테이션**은 **filed, 생성자, setter 메소드** 등에 붙여서 사용할 수 있습니다.

이는 **의존성을 주입**할 때 붙이게 되는데, **스프링 4.3 이상**부터는 **생성자로 의존성을 주입**할 때에는 **@Autowired 어노테이션을 생략**할 수 있습니다.

**1. 생성자 활용** : OwnerController에 OwnerRepository 의존성을 생성자를 통해 주입하고 있다.
```java
public OwnerController(OwnerRepository clinicService, VisitRepository visits) {
	this.owners = clinicService;
    this.visits = visits;
}
```

**2. 필드에서 주입받을 때** :
```java
@Autowired
private OwnerRepository owners;
// OwnerRepository가 이미 Bean으로 등록된 상태에서, owners에 의존성을 주입해 달라는 의미입니다.
// 필드가 final 변수일 경우 주입받을 수 없습니다.
```

**3. setter 메소드** :
```java
@Autowired
public void setOwners(OwnerRepository owners){
	this.owners = owners;
}
```
<br>

**세 가지 방법** 중 **스프링 레퍼런스에서 권장하는 방법**은 **1. 생성자에서 주입**받는 것 입니다.

이것이 **권장되는 이유**는, **필수적으로 사용해야 하는 레퍼런스(객체, 의존성) 없이**는 **인스턴스를 만들지 못하도록 강제하는 역할**을 할 수 있기 때문입니다.

가령 위의 1번 예시에서는 **생성자의 인자에 OwnerRepository type의 객체**를 받고 있기 때문에, **OwnerController는 OwnerRepository 없이는 객체 생성**이 불가능해집니다.
<br>

## 참고했던 블로그🙇‍♂️
[henry-jo님의 티스토리 글](https://jobc.tistory.com/m/30)
[빅팜님의 티스토리 글](https://leveloper.tistory.com/m/33)
[coco3o님의 티스토리 글](https://dev-coco.tistory.com/m/80)
[대학생Devlog님의 티스토리 글](https://devlog-wjdrbs96.tistory.com/m/165)
[ChanBlog님의 블로그 글](https://chanhuiseok.github.io/posts/spring-5/)