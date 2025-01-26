## OSI 7계층이란?

**OSI(Open Systems Interconnection) 7계층**은 국제 표준화 기구(ISO)에서 개발한 컴퓨터 네트워크 프로토콜 모델입니다. 이 모델은 네트워크 통신 과정을 7개의 계층을 분리하여 각 계층이 특정 기능을 수행하도록 하는 것을 목적으로 한다.

### OSI 7계층의 장점

- **모듈화**
  - 각 계층은 독립적으로 설계되어 있으므로 하나의 계층을 수정하더라도 다른 계층에는 영향을 주지 않음.
- **표준화**
  - OSI 모델은 국제 표준화 기구에서 정의하고 있으므로 다양한 시스템과 기기 간의 호환성을 보장함.
- **간소화**
  - 각 계층은 자신의 역할에만 집중하므로 복잡한 네트워크 통신을 단순화할 수 있음.

### 각 계층의 역할

![OSI 7계층](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2F92Mwp%2FbtsdYPFHR1z%2FKr7y8I3SntYb7SZjrkSNu0%2Fimg.png)

- **7계층: 응용 계층 (Application Layer)**
  - **역할**: 사용자와 직접 상호작용
  - **내용**: HTTP, FTP, DNS 등 애플리케이션 서비스 제공함.
  - **프로토콜**: HTTP, FTP, SMTP, DNS, Telnet, SSH
- **6계층: 표현 계층 (Presentation Layer)**
  - **역할**: 데이터 변환 및 표현
  - **내용**: 암호화/복호화, 압축/해제, 데이터 형식 변환함.
  - **프로토콜**: JPEG, MPEG, SSL, TLS
- **5계층: 세션 계층 (Session Layer)**
  - **역할**: 세션 관리 (연결 생성/유지/종료)
  - **내용**: 애플리케이션 간 대화 제어 및 동기화함.
  - **프로토콜**: NetBIOS, RPC, NFS
- **4계층: 전송 계층 (Transport Layer)**
  - **역할**: 데이터의 신뢰성과 흐름 제어
  - **내용**: TCP/UDP를 통해 세그먼트 전송, 패킷 재조립 및 흐름 제어함.
  - **프로토콜**: TCP, UDP
- **3계층: 네트워크 계층 (Network Layer)**
  - **역할**: 데이터 전송 경로 결정
  - **내용**: IP 주소를 기반으로 패킷 라우팅 및 전달함.
  - **프로토콜**: IP, ICMP, ARP, RARP
- **2계층: 데이터 링크 계층 (Data Link Layer)**
  - **역할**: 신뢰성 있는 데이터 전송 보장
  - **내용**: 프레임 단위 전송, 오류 검출 및 수정, MAC 주소 기반 통신함.
  - **프로토콜**: Ethernet, Token Ring, FDDI, HDLC
- **1계층: 물리 계층 (Physical Layer)**
  - **역할**: 하드웨어적인 물리적 전송 매체를 정의
  - **내용**: 전기적 신호, 케이블, 전송 매체를 통해 비트(bit) 단위로 데이터 전송함.
  - **프로토콜**: RS-232C, V.35, Ethernet, FDDI

<br>

## TCP/IP 4계층이란?

**TCP/IP 4계층 모델**은 현재 인터넷에서 사용되는 프로토콜의 집합으로, 네트워크 통신을 4개의 계층으로 분리하여, 이는 OSI 7계층 모델을 기반으로 실무적이면서 프로토콜 중심으로 단순화된 모델이다.

### TCP/IP 4계층의 장점

- **단순화**
  - 4개의 계층으로 구성되어 이해와 구현이 용이함.
- **실용성**
  - 현재 인터넷에서 실제로 사용되는 프로토콜을 기반으로 설계됨.
- **유연성**
  - 다양한 네트워크 환경과 하드웨어를 지원함.

### 각 계층의 역할

![OSI 7계층과 TCP/IP 4계층](https://img1.daumcdn.net/thumb/R720x0.q80/?scode=mtistory2&fname=https%3A%2F%2Ft1.daumcdn.net%2Fcfile%2Ftistory%2F995EFF355B74179035)

- **4계층: 응용 계층 (Application Layer)**
  - **역할**: 사용자와 직접 상호작용하는 애플리케이션 서비스를 제공
  - **내용**: HTTP, FTP, SMTP 등 다양한 프로토콜을 통해 애플리케이션 간의 데이터 통신을 지원함.
  - **프로토콜**: HTTP, FTP, DNS, SMTP
- **3계층: 전송 계층 (Transport Layer)**
  - **역할**: 종단 간의 신뢰성 있는 데이터 전송을 보장
  - **내용**: TCP와 UDP 프로토콜을 사용하여 데이터의 흐름 제어와 오류 검출을 수행함.
  - **프로토콜**: TCP, UDP
- **2계층: 인터넷 계층 (Internet Layer)**
  - **역할**: 패킷의 주소 지정 및 경로 설정을 담당
  - **내용**: IP 주소를 기반으로 패킷을 라우팅하며, 네트워크 간의 데이터 전송을 관리함.
  - **프로토콜**: IP
- **1계층: 네트워크 인터페이스 계층 (Network Interface Layer)**
  - **역할**: 물리적 네트워크 매체와의 인터페이스를 담당
  - **내용**: 데이터 링크 및 물리 계층의 기능을 포함하며, 프레임 단위의 데이터 전송과 물리적 전송 매체를 관리함.
  - **프로토콜**: Ethernet

<br>

## **인캡슐레이션(Encapsulation)과 디캡슐레이션(Decapsulation)**

### **인캡슐레이션(Encapsulation)**

상위 계층의 데이터에 각 계층의 헤더 정보를 추가하여 하위 계층으로 내려보내는 과정이다. 간략하게 설명하면 데이터를 분할하여 패킷형태로 데이터를 보내는 것이 인캡슐레이션이다.

- 전송 계층 → 네트워크 계층 → 데이터 링크 계층 → 물리 계층

### **디캡슐레이션(Decapsulation)**

수신 측에서 하위 계층부터 상위 계층으로 데이터를 전달하면서 각 계층의 헤더를 제거하는 과정이다. 간략하게 설명하면 분할된 패킷을 받아 순서에 맞게 결합하는 것이 디캡슐레이션이다.

- 물리 계층 -> 데이터 링크 계층 -> 네트워크 계층 -> 전송 계층

![순서](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FqtYup%2FbtrC2vJGj5E%2FrK5nNMIKjoyVEoKumkiFRk%2Fimg.png)

## 참고한 자료

[1. [CS] OSI 7계층 파헤치기 / OSI 7계층이란 / 계층별 역할, 기능](https://mundol-colynn.tistory.com/167)  
[2. TCP/IP 4계층 모델 - 핵심 총정리](https://inpa.tistory.com/entry/WEB-%F0%9F%8C%90-TCP-IP-%EC%A0%95%EB%A6%AC-%F0%9F%91%AB%F0%9F%8F%BD-TCP-IP-4%EA%B3%84%EC%B8%B5)  
[3. [ WEB ] 인캡슐레이션(Encapsulation), 디캡슐레이션(Decapsulation)](https://mihee0703.tistory.com/29)
