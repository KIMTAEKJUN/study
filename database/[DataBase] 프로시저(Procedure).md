> 개발 팀장님께서 **프로시저(Procedure)**에 대해 공부해 보면 좋겠다 하셔서 정리합니다. 앞으로는 회사를 다니며 많은 걸 정리해 볼 예정입니다.

## 프로시저란?
일련의 SQL 쿼리를 하나의 함수처럼 실행하기 위한 쿼리의 집합이고 반복되거나 또는 구현이 복잡한 작업들을 효율적으로 처리 가능할 수 있으며, 이러한 작업을 하나의 트랜잭션으로 처리하여 데이터 무결성을 보장한다.
- 재사용성이 높아 코드 중복을 줄일 수 있다.
- 네트워크 트래픽을 줄일 수 있어 성능 향상에 도움이 된다.

## 프로시저 사용하기(MySQL or MariaDB)
### 테이블 생성
아래와 같은 테이블이 존재한다고 가정해보겠습니다.
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE user_points (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    points INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 프로시저 생성/수정
MySQL과 MariaDB에서는 기존 프로시저를 수정하는 직접적인 명령어는 없고 대신, 기존 프로시저를 삭제한 후 다시 생성하는 방식으로 수정할 수 있다.
```sql
DELIMITER //

CREATE PROCEDURE AddUserAndUpdatePoints(
    IN userName VARCHAR(50), 
    IN userEmail VARCHAR(100), 
    IN initialPoints INT
)
BEGIN
	# 트랜잭션 시작(트랜잭션을 직접 관리하고 싶다면 추가)
	START TRANSACTION;
    
    # 예외 처리 핸들러 선언
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        # 오류가 발생하면 롤백
        ROLLBACK;
    END;

    # 사용자 정보 삽입
    INSERT INTO users (name, email) 
    VALUES (userName, userEmail);
    
    # 방금 삽입된 사용자의 ID 가져오기
    DECLARE newUserId INT;
    SET newUserId = LAST_INSERT_ID();
    
    # 해당 사용자의 포인트 업데이트
    INSERT INTO user_points (user_id, points) 
    VALUES (newUserId, initialPoints);
    
    # 모든 작업이 성공하면 커밋
    COMMIT;
END //

DELIMITER ;
```
#### BEGIN ... END 란?
일련의 SQL 쿼리를 하나의 그룹으로 묶어서 실행할 수 있도록 하는 블록 문법

### 프로시저 삭제
```sql
DROP PROCEDURE IF EXISTS AddUserAndUpdatePoints;
```

### 프로시저 호출
```sql
CALL AddUserAndUpdatePoints('Taekjun', 'Taekjun@example.com', 100);
```