프로젝트를 진행하면서 출발지, 도착지, 방문 주소와 같은 기능을 개발했습니다.

현재 프로젝트에서는

1. **주소 검색 및 좌표 변환 기능**
2. **사용자의 현재 위치 가져오기**

카카오맵 API를 활용하여 진행했으며, 이 글에서는 구현 과정에 대한 정리하고자 합니다.

## 1. 카카오맵 API 등록

1. **[카카오 개발자 사이트](https://developers.kakao.com/) 접속 후 앱 관리 페이지로 이동**
   ![kakao developers](https://velog.velcdn.com/images/kimtaekjun/post/5ad7d53c-ffc1-4947-984d-2134cce2e91b/image.png)

2. **앱 생성 버튼을 클릭하여, 앱 등록**
   ![kakao developers](https://velog.velcdn.com/images/kimtaekjun/post/b8df3fbc-09e9-4b22-9e66-04995b3492c8/image.png)

3. **해당 앱 관리 페이지로 이동 후 앱 > 일반 > 앱 키 > REST API 키 복사**
   ![kakao developers](https://velog.velcdn.com/images/kimtaekjun/post/2039a7e6-0f0a-4691-81b3-14c1f2a51f40/image.png)

4. **프로젝트 환경변수 파일에 추가**
   ![env](https://velog.velcdn.com/images/kimtaekjun/post/4ebf50e5-2591-4122-b6a4-40669e6d9413/image.png)

위 단계를 똑같이 진행하면 카카오맵 API를 사용하기 위한 세팅 완료!

<br>

## 2. 주소 검색 및 좌표 변환 기능

### 2-1. 주소 → 좌표 변환 함수 (utils/maps.ts)

```typescript
import { KAKAO_REST_API_KEY } from "@env";
import apiClient from "./api";

// 주소 → 좌표 변환 함수
export async function getCoordinates(address: string) {
  try {
    const response = await apiClient.get(
      `https://dapi.kakao.com/v2/local/search/address.json?query=${encodeURIComponent(
        address
      )}`,
      {
        headers: {
          Authorization: `KakaoAK ${KAKAO_REST_API_KEY}`,
          "Content-Type": "application/json;charset=UTF-8",
        },
      }
    );
    const data = response.data;
    if (data.documents && data.documents.length > 0) {
      const { x, y } = data.documents[0]; // x: 경도, y: 위도
      return { x, y };
    }
    return null;
  } catch (error) {
    console.error("주소 -> 좌표 변환 실패:", error);
    return null;
  }
}
```

**설명:**

- 사용자가 입력한 주소를 카카오맵 API로 검색하여, 위도(y)/경도(x) 좌표로 변환합니다.
- 실패 시 null 반환, 성공 시 `{ x, y }` 객체 반환.

### 2-2. 출발지 주소 검색 및 선택 화면 (app/moving/departure.tsx)

```typescript
import React, { useState } from "react";
import {
  View,
  TextInput,
  TouchableOpacity,
  Text,
  ScrollView,
} from "react-native";
import { KAKAO_REST_API_KEY } from "@env";
import apiClient from "@/utils/api";
import SafeTouchable from "@/components/ui/SafeTouchable";

export default function DepartureScreen() {
  const [keyword, setKeyword] = useState("");
  const [addresses, setAddresses] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // 주소 검색 함수
  const searchAddress = async () => {
    if (!keyword.trim()) return;
    setIsLoading(true);
    try {
      const response = await apiClient.get(
        `https://dapi.kakao.com/v2/local/search/keyword.json?query=${encodeURIComponent(
          keyword
        )}`,
        {
          headers: {
            Authorization: `KakaoAK ${KAKAO_REST_API_KEY}`,
            "Content-Type": "application/json;charset=UTF-8",
          },
        }
      );
      const keywordResults = response.data?.documents || [];
      setAddresses(keywordResults);
    } catch (error) {
      setAddresses([]);
    } finally {
      setIsLoading(false);
    }
  };

  // 주소 선택
  const handleSelectAddress = (address) => {
    // address.address_name, address.x, address.y 등 활용
    alert(
      `선택: ${address.address_name}\n위도: ${address.y}, 경도: ${address.x}`
    );
    // 실제 서비스에서는 이 값을 상태/서버에 저장하거나 다음 화면으로 전달
  };

  return (
    <View>
      <TextInput
        value={keyword}
        onChangeText={setKeyword}
        onSubmitEditing={searchAddress}
        placeholder="주소를 입력하세요"
      />
      <SafeTouchable onPress={searchAddress}>
        <Text>검색</Text>
      </SafeTouchable>
      <ScrollView>
        {isLoading ? (
          <Text>로딩 중...</Text>
        ) : (
          addresses.map((address, idx) => (
            <TouchableOpacity
              key={idx}
              onPress={() => handleSelectAddress(address)}
            >
              <Text>{address.address_name}</Text>
              <Text>
                {address.y}, {address.x}
              </Text>
            </TouchableOpacity>
          ))
        )}
      </ScrollView>
    </View>
  );
}
```

**설명:**

- 사용자가 주소를 입력하고 검색 버튼을 누르면, 카카오맵 API로 검색 결과를 받아옵니다.
- 결과 리스트에서 주소를 선택하면, 해당 주소의 위도/경도 정보를 활용할 수 있습니다.

<br>

## 3. 사용자의 현재 위치 가져오기

### 3-1. expo-location 설치

```bash
npx expo install expo-location
```

### 3-2. 현재 위치 가져오는 함수 (app/moving/departure.tsx)

```typescript
import * as Location from "expo-location";

const findCurrentLocation = async () => {
  try {
    const { status } = await Location.requestForegroundPermissionsAsync();
    if (status !== "granted") {
      alert("위치 권한이 필요합니다.");
      return;
    }
    const location = await Location.getCurrentPositionAsync({
      accuracy: Location.Accuracy.Balanced,
    });
    const { latitude, longitude } = location.coords;

    // 좌표 → 주소 변환 (카카오맵 API)
    const response = await apiClient.get(
      `https://dapi.kakao.com/v2/local/geo/coord2address.json?x=${longitude}&y=${latitude}&input_coord=WGS84`,
      {
        headers: {
          Authorization: `KakaoAK ${KAKAO_REST_API_KEY}`,
        },
      }
    );
    const addressInfo = response.data?.documents[0];
    if (addressInfo) {
      alert(
        `현재 위치 주소: ${addressInfo.address?.address_name}\n위도: ${latitude}, 경도: ${longitude}`
      );
    }
  } catch (error) {
    alert("현재 위치를 가져오지 못했습니다.");
  }
};
```

**설명:**

- expo-location으로 현재 위치(위도/경도)를 가져옵니다.
- 카카오맵의 coord2address API로 좌표를 주소로 변환합니다.
- 결과를 alert 등으로 확인하거나, 출발지/도착지 입력에 바로 활용할 수 있습니다.

<br>

## 4. 마무리

이번 글에서는 카카오맵 API를 활용한 위치 기반 서비스 구현 과정에 대해 정리해보았습니다.
이 글이 저와 같은 기능을 구현하려는 분들께 도움이 되었길 바랍니다 🙌
끝까지 읽어주셔서 감사합니다 😊
