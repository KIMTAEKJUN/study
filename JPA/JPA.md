> 패스트캠퍼스의 한 번에 끝내는 Spring 강의에서 Spring Data JPA 파트를 보고 정리합니다.

## Spring Data JPA
### 시작 전 정확한 개념 알기 (ORM, JPA, JPQL)
### 1. ORM (Object-Relational Mapping)
**객체 지향 언어**를 이용하여, 서로 호환되지 않는 타입 간의 **데이터를 변환**하는 기술
  - **DB(RDBMS) 테이블** 데이터를 **자바 객체와 매핑**하는 기술
- **ORM (Object-Relational Mapping)** 으로 얻고자 하는 것
  - **DB의 추상화**: 특정 DB에 종속된 표현(ex: SQL)이나 구현이 사라지고, DB 변경에 좀 더 유연해짐
  - **객체의 이점 활용**: 객체간 참조, type-safety
  - **관심사 분리**: DB 동작에 관한 코드 작성의 반복을 최소화하고 비즈니스 로직에 집중

<br>

### 2. JPA (Java Persistence API)
자바에서 ORM 기술을 사용해 RDBMS를 다루기 위한 인터페이스 표준 명세
- API + JPQL + metadata (+ Criteria API)
- 기본적으로 관계형 데이터베이스의 영속성(persistence)만을 규정
  - JPA 구현체 중에 다른 유형의 데이터베이스 모델을 지원하는 경우가 있지만, 원래 JPA 스펙과는 무관

#### 2-1. JPA: Persistence (영속성)
데이터를 생성한 프로그램이 종료되더라도 사라지지 않는 데이터의 특성
- **사라지는 데이터** - 주기억장치(휘발성 스토리지)에 저장된 데이터
  - 프로세스 메모리 안의 데이터 (변수, 상수, 객체, 함수 등)
- **사라지지 않는 데이터** - 보조기억장치(비휘발성 스토리지)에 저장된 데이터
  - 하드디스크, SSD 에 기록된 데이터 (파일, 데이터베이스 등)
- **영속성 프레임워크**: 영속성을 관리하는 부분을 Persistence layer 로 추상화하고, 이를 전담하는 프레임워크에게 관리를 위임
  - **JPA에서 Persistence란?** : 프로세스가 DB로부터 읽거나 DB에 저장한 정보의 특성

<br>

### 3. JPQL (Java Persistence Query Language)
플랫폼으로부터 독립적인 객체 지향 쿼리 언어
- JPA 표준의 일부로 정의됨
- RDBMS의 엔티티(Entity)를 다루는 쿼리를 만드는데 사용
- SQL의 영향을 받아서 형식이 매우 유사

#### JPQL vs SQL(MySQL)
```sql
-- JPQL
SELECT DISTINCT a
FROM Author a
INNER JOIN a.books b
WHERE b.publisher.name = 'zzz'
  AND a.lastName IS NULL;
```
- 객체적인 방식으로 관계를 표현했다.

```sql
-- SQL(MySQL)
SELECT DISTINCT a.*
FROM author a
INNER JOIN book b ON b.id = a.book_id
INNER JOIN publisher p ON p.id = b.publisher_id
WHERE p.name = 'zzz'
  AND a.last_name IS NULL;
```
- 테이블과 컬럼을 기준으로 관계를 표현했다.

#### SQL 과 JPQL 은 다른 언어이다.
  - **SQL**: 표준 ANSI SQL 을 기준으로 만든, 특정 DB에 종속적인 언어
  - **JPQL**: 특정 DB에 종속적인 언어가 아님
- JPA 프레임워크를 사용한다면
  - 특별한 요구사항이 있지 않은 한, JPQL 을 몰라도 된다
  - JPQL을 직접 코드에서 사용하고 있다면, 반드시 필요했던 일인지 검토하기