프로젝트를 진행하면서 출발지와 도착지 간의 예상 거리를 계산하는 기능을 개발했습니다.

현재 프로젝트에서는

1. **카카오 모빌리티 API를 활용한 실제 도로 거리 계산(km)**
2. **계산된 거리 정보를 백엔드 DB에 저장**
3. **관리자 화면에서 거리 정보(km) 표시**

이 기능들을 구현했으며, 이 글에서는 실제 구현 과정을 코드와 함께 정리합니다.

## 1. 카카오 모빌리티 API 등록

카카오 모빌리티 API는 카카오맵 API와 동일한 REST API 키를 사용합니다.

1. **[카카오 개발자 사이트](https://developers.kakao.com/)에서 기존 앱 선택**
2. **앱 > 일반 > 앱 키 > REST API 키 확인** (카카오맵 API와 동일한 키 사용)
3. **REST API 키 복사 > 프로젝트 환경변수 파일에 추가**

<br>

## 2. 왜 두 개의 API가 필요한가?

### 카카오맵 API vs 카카오 모빌리티 API

| API                     | 역할                | 입력     | 출력                        |
| ----------------------- | ------------------- | -------- | --------------------------- |
| **카카오맵 API**        | 주소 → 좌표 변환    | "서울역" | { x: 126.9707, y: 37.5555 } |
| **카카오 모빌리티 API** | 실제 도로 거리 계산 | 좌표     | 12.5km                      |

**설명:**

- 카카오 모빌리티 API는 입력 값으로 실제 좌표 값만 받습니다.
- 먼저, 사용자가 입력한 주소를 먼저 좌표로 변환해야 합니다.

<br>

## 3. 거리 계산 함수 구현 (utils/maps.ts)

### 3-1. 주소 → 좌표 변환 함수

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

### 3-2. 카카오 모빌리티 API를 활용한 거리 계산 함수

```typescript
// 거리 계산 함수
export async function calculateDistance(
  origin: string,
  destination: string
): Promise<string | null> {
  try {
    // 1. 출발지 주소를 좌표로 변환
    const originCoord = await getCoordinates(origin);
    // 2. 도착지 주소를 좌표로 변환
    const destCoord = await getCoordinates(destination);

    if (!originCoord || !destCoord) {
      throw new Error("좌표 변환 실패");
    }

    // 3. 카카오 모빌리티 API로 실제 도로 거리 계산
    const response = await apiClient.get(
      `https://apis-navi.kakaomobility.com/v1/directions?origin=${originCoord.x},${originCoord.y}&destination=${destCoord.x},${destCoord.y}`,
      {
        headers: {
          Authorization: `KakaoAK ${KAKAO_REST_API_KEY}`,
          "Content-Type": "application/json;charset=UTF-8",
        },
      }
    );

    const data = response.data;

    if (data.routes && data.routes.length > 0) {
      // 미터 단위를 킬로미터로 변환 (소수점 1자리)
      const distanceInKm = (data.routes[0].summary.distance / 1000).toFixed(1);
      return distanceInKm;
    }
    return null;
  } catch (error) {
    console.error("거리 계산 실패:", error);
    return null;
  }
}
```

**설명:**

- 출발지와 도착지 주소를 각각 좌표로 변환합니다.
- 카카오 모빌리티 API의 `/v1/directions` 엔드포인트를 사용해 실제 도로를 따라간 거리를 계산합니다.
- 결과는 미터 단위로 반환되므로 1000으로 나누어 킬로미터로 변환합니다.

<br>

## 4. 백엔드 데이터베이스 스키마

### 4-1. Reservation 엔티티 (TypeORM)

```typescript
// database/entities/reservations.entity.ts
import { Entity, Column, PrimaryGeneratedColumn } from "typeorm";

@Entity("reservations")
export class Reservation {
  @PrimaryGeneratedColumn()
  id: number;

  // ... 기타 필드들

  @Column({ type: "decimal", precision: 5, scale: 1, nullable: true })
  estimatedDistance: number;
}
```

**설명:**

- `precision: 5`: 전체 자릿수 5자리 (예: 123.4)
- `scale: 1`: 소수점 이하 1자리
- `nullable: true`: NULL 값 허용 (거리가 계산되지 않은 경우)

### 4-2. DTO 정의 (NestJS)

```typescript
// reservation/dtos/reservations-request.dto.ts
import { IsNumber, IsOptional } from "class-validator";
import { ApiProperty } from "@nestjs/swagger";

export class UpdateEstimatedDistanceRequestDto {
  @IsNumber()
  @IsOptional()
  @ApiProperty({ description: "예상 거리", default: 0 })
  estimatedDistance?: number;
}
```

**설명:**

- `@IsNumber()`: 숫자 타입 검증
- `@IsOptional()`: 선택적 필드 (undefined 허용)
- `@ApiProperty()`: Swagger 문서화를 위한 메타데이터

### 4-3. 프론트엔드 타입 정의 (TypeScript)

```typescript
// types/reservation.ts
export interface Reservation {
  id: number;

  // ... 기타 필드들

  estimatedDistance?: number; // 예상 거리 (km)
}
```

<br>

## 5. 백엔드 API 구현

### 5-1. 예상 거리 업데이트 서비스 (NestJS)

```typescript
// admin/reservation/admin-reservation.service.ts
async updateEstimatedDistance(id: number, body: UpdateEstimatedDistanceRequestDto): Promise<Reservation> {
  const reservation = await this.reservationRepository.findOne({
    where: { id },
  });

  if (!reservation) {
    throw new NotFoundException(AppError.RESERVATION.NOT_FOUND);
  }

  // 소수점 한 자리로 반올림
  const distance = body.estimatedDistance || 0;
  const roundedDistance = Math.round(distance * 10) / 10;

  reservation.estimatedDistance = roundedDistance;

  return await this.reservationRepository.save(reservation);
}
```

### 5-2. 컨트롤러 엔드포인트

```typescript
// admin/reservation/admin-reservation.controller.ts
@Patch(':id/distance')
async updateEstimatedDistance(
  @Param('id') id: number,
  @Body() body: UpdateEstimatedDistanceRequestDto
): Promise<ReservationResponseDto> {
  return {
    status: 'success',
    data: {
      reservation: await this.adminReservationService.updateEstimatedDistance(id, body)
    },
  };
}
```

**설명:**

- `PATCH /admin/reservations/:id/distance` 엔드포인트로 예상 거리 업데이트
- 요청 본문에 `estimatedDistance` 필드 포함
- 응답은 `ReservationResponseDto` 형태로 성공 상태와 업데이트된 예약 정보 반환
- 예약이 존재하지 않으면 NotFoundException을 발생시킵니다.

<br>

## 6. 프론트엔드 거리 계산 및 저장 기능

### 6-1. 거리 계산 및 DB 저장 함수

```typescript
// app/(admin)/reservations/[id].tsx
import { calculateDistance } from "@/utils/maps";

// 거리 계산 및 저장 함수
const calculateAndSaveDistance = async () => {
  if (
    reservation?.departureAddress &&
    reservation?.arrivalAddress &&
    !reservation.estimatedDistance
  ) {
    try {
      // 출발지와 도착지가 동일한지 확인
      if (reservation.departureAddress === reservation.arrivalAddress) {
        setDistance("0km");

        // DB에 0으로 설정
        const token = await SecureStore.getItemAsync("accessToken");
        await apiClient.patch(
          `/admin/reservations/${id}/distance`,
          { estimatedDistance: 0 },
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        setReservation((prev) => ({
          ...prev!,
          estimatedDistance: 0,
        }));
        return;
      }

      // 카카오 모빌리티 API로 거리 계산
      const result = await calculateDistance(
        reservation.departureAddress,
        reservation.arrivalAddress
      );

      if (result) {
        const distanceValue = parseFloat(result);

        // DB에 계산된 거리 저장
        const token = await SecureStore.getItemAsync("accessToken");
        await apiClient.patch(
          `/admin/reservations/${id}/distance`,
          { estimatedDistance: distanceValue },
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        // 상태 업데이트
        setDistance(`${result}km`);
        setReservation((prev) => ({
          ...prev!,
          estimatedDistance: distanceValue,
        }));
      }
    } catch (error) {
      console.error("거리 계산 실패:", error);
      showGenericErrorToast("거리 계산에 실패했습니다");
      setDistance("0km");

      // 에러 시 DB에 0으로 설정
      try {
        const token = await SecureStore.getItemAsync("accessToken");
        await apiClient.patch(
          `/admin/reservations/${id}/distance`,
          { estimatedDistance: 0 },
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        setReservation((prev) => ({
          ...prev!,
          estimatedDistance: 0,
        }));
      } catch (dbError) {
        console.error("거리 정보 저장 실패:", dbError);
        showGenericErrorToast("거리 정보 저장에 실패했습니다");
      }
    }
  } else if (reservation?.estimatedDistance) {
    // DB에 이미 저장된 거리가 있으면 그것을 사용
    setDistance(`${reservation.estimatedDistance}km`);
  }
};
```

### 6-2. 자동 거리 계산 실행

```typescript
// useEffect로 예약 정보 로드 시 자동 거리 계산
useEffect(() => {
  if (reservation && reservation.serviceType !== ServiceType.HOME_CARE) {
    calculateAndSaveDistance();
  }
}, [reservation?.id]);
```

### 6-3. 거리 정보 UI 표시

```typescript
// 예상 이동거리 표시 컴포넌트
<View className="flex-row items-start">
  <View className="w-6 h-6 rounded-full bg-orange-100 items-center justify-center mt-0.5">
    <Ionicons name="speedometer" size={14} color="#EA580C" />
  </View>
  <View className="ml-3 flex-1">
    <Text className="text-gray-500 text-sm">예상 이동거리</Text>
    {reservation.estimatedDistance ? (
      <Text className="text-gray-800 font-medium">
        {reservation.estimatedDistance}km
      </Text>
    ) : distance ? (
      <Text className="text-gray-800 font-medium">{distance}</Text>
    ) : (
      <Text className="text-gray-400">계산 중...</Text>
    )}
  </View>
</View>
```

<br>

## 7. 전체 흐름 요약

![카카오 모빌리티 API 전체 흐름](https://velog.velcdn.com/images/kimtaekjun/post/17969975-0c92-4f09-988c-11f5384b58ce/image.png)

<br>

## 8. 마무리

이번 글에서는 카카오 모빌리티 API를 활용한 실제 도로 거리 계산 기능의 구현 과정을 정리해보았습니다.
이 글이 저와 같은 기능을 구현하려는 분들께 도움이 되었길 바랍니다 🙌  
끝까지 읽어주셔서 감사합니다 😊
