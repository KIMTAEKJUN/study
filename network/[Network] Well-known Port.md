## 포트(Port)란?
- **일종의 논리적인 접속 장소이다.**
    - 컴퓨터는 동시에 하나 이상의 프로그램을 실행하기 때문에 IP주소만으로는 특정 서비스에 접근 불가하다.
    - 포트 번호는 0부터 65535까지 사용 가능하다.

<br>

## 포트(Port)의 3가지 종류
### Well-known Port (잘 알려진 포트)
- 루트 권한으로만 포트를 사용 가능
- 특정 서비스나 프로토콜에 고정적으로 할당

### Registered Port (등록된 포트)
- 서버 소켓 포트로 사용하는 영역
- IANA에 등록되어 있지만, 강제성 없음

### Dynamic Port (동적 포트)
- 매번 접속할 때마다 포트번호가 동적으로 부여
- 서버 소켓 포트로 사용할 수 없는 영역

<br>

## Well-known Port (웰노운포트)란?
특정한 쓰임새를 위해서 IANA에서 할당한 TCP 및 UDP 포트 번호의 일부이며, 대부분의 운영 체제에서 이 포트들을 사용하려면 관리자 권한이 필요하다.

### Well-known Port (웰노운포트)의 목록
![Port](https://velog.velcdn.com/images/kimtaekjun/post/acda23ee-5554-42c0-9ff5-9751a413cbea/image.png)

### 알아두면 좋은 Well-known Port (웰노운포트)
- **SSH**: 22
- **SMTP**: 25
- **DNS**: 53
- **HTTP**: 80
- **HTTPS**: 443

### 알아두면 좋은 데이터베이스 관련 포트
- **MariaDB, MySQL**: 3306
- **PostgreSQL**: 5432
- **Redis**: 6379
- **MongoDB**: 27017
- **Elasticsearch**: 9200 (HTTP), 9300 (TCP)