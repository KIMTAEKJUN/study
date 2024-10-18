## URLConnection
Java의 기본 네트워크 통신 클래스로, URL로 표현되는 리소스와의 연결을 나타내고 URL의 내용을 읽어오거나 GET, POST 등의 HTTP 메서드를 사용해 데이터를 주고받을 수 있다. 이를 통해 외부 라이브러리 없이 기본적인 HTTP 통신을 구현할 때 사용한다.

### 장점
- JDK의 일부로 제공되어 외부 라이브러리의 의존성이 없음
- 저수준 API를 통해 HTTP 통신의 직접적이고 세부적인 제어가 가능
- HTTP 및 HTTPS를 지원하며, 기본적인 HTTP 통신을 다루는데 유용

### 단점
- 쿠키 제어가 불가능
- 타임아웃을 설정할 수 없음
- 연결 풀링(connection pooling)을 지원하지 않아 성능 저하 가능성
- 응답코드가 4xx 이거나 5xx일 경우 IOException이 발생하여 예외 처리가 필요함
- 저수준 I/O(InputStream, OutputStream)를 사용하여 HTTP 요청 및 응답을 처리가 복잡함

<br>

## RestTemplate
Spring 3.0부터 지원하는 HTTP 클라이언트 템플릿으로, HTTP 서버와의 통신을 단순화하고 다양한 HTTP 메서드(GET, POST, PUT, DELETE 등)에 대응하는 편리한 메서드를 제공한다. 이를 통해 Restful 원칙을 쉽게 준수하며 HTTP 통신을 효율적으로 구현할 수 있다.

### 장점
- RESTful 형식에 맞춤
- 멀티쓰레드 방식을 사용
- Blocking 방식을 사용
- 자동으로 JSON, XML 등의 응답을 객체로 변환
- 연결 풀링(connection pooling)을 지원하여 성능 향상 가능성

### 단점
- 동기 방식으로 동작하여 비효율적일 수 있음
- Non-Blocking, Reactive한 방식을 지원하지 않음
- 공식 문서에 따르면 Spring 5.0부터 향후 버전에서 제거될 예정임

<br>

## URLConnection VS RestTemplate 코드 예제
```java
// 원래 코드 (URLConnection)
@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

    @Value("${holidayKey}")
    private String holidayKey;
    private final HolidayRepository holidayRepository;

    public ArrayList<HashMap<String, Object>> getHoliday(String year) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("Holiday-OPEN-API-URL"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + holidayKey); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*타입*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("solYear", "UTF-8") + "=" + URLEncoder.encode(year, "UTF-8")); /*연*/
        
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        Map<String, Object> holidayMap = string2Map(sb.toString());
        Map<String, Object> response = (Map<String, Object>) holidayMap.get("response");
        Map<String, Object> body = (Map<String, Object>) response.get("body");
        HashMap<String, Object> items = (HashMap<String, Object>) body.get("items");
        ArrayList<HashMap<String, Object>> item = (ArrayList<HashMap<String, Object>>) items.get("item");
        
        return item;
    }
}
```

```java
// 리팩토링 코드 (RestTemplate)
@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

    @Value("${holidayKey}")
    private String holidayKey;
    private final HolidayRepository holidayRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<Map<String, Object>> getHoliday(String year) {
        String url = String.format("Holiday-OPEN-API-URL",holidayKey, year);
        String response = restTemplate.getForObject(url, String.class);
        
        try {
            Map<String, Object> holidayMap = objectMapper.readValue(response, Map.class);
            return Optional.ofNullable(holidayMap)
                    .map(map -> (Map<String, Object>) map.get("response"))
                    .map(map -> (Map<String, Object>) map.get("body"))
                    .map(map -> (Map<String, Object>) map.get("items"))
                    .map(map -> (List<Map<String, Object>>) map.get("item"))
                    .orElse(Collections.emptyList());
        } catch (IOException e) {
            log.error("Error parsing holiday data", e);
            return Collections.emptyList();
        }
    }
}
```

<br>

## WebClient
Spring 5.0부터 지원하는 비동기식 논블로킹 웹 클라이언트로, Reactive 스트림을 지원하고 효율적인 네트워크 사용과 확장성을 제공한다. RestTemplate의 대안으로 설계되었으며, Spring WebFlux와 함께 사용되어 반응형 프로그래밍 모델을 구현할 수 있다.

### 장점
- 스트리밍 지원으로 대용량 데이터 처리에 적합
- 리액티브 프로그래밍 모델을 통한 높은 확장성 및 성능 향상
- 비동기 및 논블로킹 I/O를 통한 효율적인 리소스 활용 가능
- Reactive Streams를 지원하여 백프레셔 처리 및 데이터 스트림 제어 가능

### 단점
- 기존의 동기식 코드와의 통합이 복잡할 수 있음
- 복잡한 비즈니스 로직 구현 시 코드가 복잡해질 수 있음
- 학습이 매우 어려움 (Reactive 프로그래밍에 대한 이해 필요)

<br>

## 추가로 공부할 만한 것
### HttpInterface
Spring 6.1에서 도입된 기능으로, Java의 인터페이스와 애노테이션을 통해 HTTP 요청을 선언적으로 정의한다. RestTemplate이나 WebClient와 함께 사용할 수 있어 HTTP 클라이언트 코드를 간소화할 수 있다.

### RestClient
Spring Framework 6.1에서 소개된 새로운 동기식 HTTP 클라이언트로, RestTemplate의 장점을 유지한다. 함수형 프로그래밍 스타일과 메서드 체이닝을 지원하여 더 읽기 쉬운 코드를 작성할 수 있다.