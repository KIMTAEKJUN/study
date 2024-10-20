## DNS(Domain Name System)란?

사람이 읽을 수 있는 도메인 이름(예: www.example.com)을 컴퓨터가 이해할 수 있는 IP 주소(예: 192.0.2.1)로 변환하는 시스템이다.

### DNS 주소 등록 과정
1. 도메인 등록: 도메인 등록 업체(가비아, 카페24 등)를 통해 도메인을 구매.
2. DNS 서버 설정: 도메인을 관리할 DNS 서버를 지정.
3. 레코드 설정: A Record, CNAME 등 필요한 DNS 레코드를 설정.

### DNS Record Types
![dns record types](https://velog.velcdn.com/images/kimtaekjun/post/f53e4756-d11c-412c-af30-bce4e0451541/image.png)

- `A Record (Address Record)`:
    - 도메인을 IPv4 주소에 직접 매핑함. (메인도메인 설정)
        - 예) `example.com` → `192.0.0.1`
        - example.com을 입력하면 이 도메인에 대한 A Record를 찾아 192.0.0.1을 반환함.
        - 브라우저 주소창에는 example.com이 표시되지만, 실제 연결은 192.0.0.1로 이루어짐.

- `AAAA Record (Quad-A Record)`:
    - 도메인을 IPv6 주소에 직접 매핑함. (IPv6 지원 도메인 설정)
        - 예) `example.com` → `2001:0db8:85a3:0000:0000:8a2e:0370:7334`
        - example.com을 입력하면 이 도메인에 대한 AAAA Record를 찾아 해당 IPv6 주소를 반환함.
        - 브라우저 주소창에는 example.com이 표시되지만, 실제 연결은 IPv6 주소로 이루어짐.

- `CNAME (Canonical Name)`:
    - 도메인을 또 다른 도메인 주소로 매핑함. (서브도메인 설정)
        - 예) `blog.example.com` → `example.com`
        - blog.example.com을 입력하면 이 도메인에 대한 CNAME을 찾아 example.com을 반환함.
        - 그 후 example.com의 A Record를 찾아 IP 주소를 반환함.
        - 브라우저 주소창에는 blog.example.com이 표시되지만, 실제 연결은 example.com의 IP 주소로 이루어짐.

- `NS Record (NameServer Record)`:
    - 도메인의 권한 있는 네임 서버를 지정함. (도메인의 DNS 정보 관리 서버 설정)
        - 예) `example.com` → `ns1.dnsprovider.com`
        - DNS 쿼리 시 이 레코드를 참조하여 해당 도메인의 DNS 정보를 관리하는 서버를 찾음.
        - 일반적으로 최소 2개의 NS 레코드를 설정하여 redundancy를 확보함.
            - `redundancy`: 시스템의 안정성과 가용성을 높이기 위해 중복성을 제공하는 개념