안드로이드/iOS 앱에서 SecureStore를 사용하여 데이터를 저장할 때 "Value being stored in SecureStore is larger than 2048 bytes and it may not be stored successfully" 경고하는 문제가 발생했습니다.

## 문제 상황

React Native 앱에서 사용자 프로필 정보를 SecureStore에 저장할 때 다음과 같은 경고가 발생했습니다.

```javascript
Value being stored in SecureStore is larger than 2048 bytes and it may not be stored successfully.
In a future SDK version, this call may throw an error.
```

### 원인 분석

SecureStore는 보안을 위해 2048바이트 제한이 있으며, JSON.stringify()로 직렬화된 사용자 데이터가 이 제한을 초과하여 발생하는 문제입니다.

```javascript
// 문제가 되는 코드
await SecureStore.setItemAsync("userProfile", JSON.stringify(userData));
```

<br>

## 해결 방법

### **1. 데이터 크기 제한 및 필수 필드만 저장**

필요한 데이터만 추출하여 저장하는 유틸리티 함수를 생성합니다.

```typescript
// utils/userProfile.ts
import * as SecureStore from "expo-secure-store";
import { UserRole, AuthProvider } from "../types/user";

// SecureStore에 저장할 최소한의 사용자 프로필
export interface MinimalUserProfile {
  id: number;
  name: string;
  email: string;
  phone?: string;
  role: UserRole;
  authProvider: AuthProvider;
}

// 전체 사용자 프로필에서 필수 정보만 추출
export function extractMinimalUserProfile(userData: any): MinimalUserProfile {
  return {
    id: userData.id,
    name: userData.name,
    email: userData.email,
    phone: userData.phone,
    role: userData.role,
    authProvider: userData.authProvider,
  };
}

// 안전한 사용자 프로필 저장
export async function saveUserProfile(userData: any): Promise<void> {
  try {
    const minimalProfile = extractMinimalUserProfile(userData);
    const profileString = JSON.stringify(minimalProfile);

    // 데이터 크기 확인 (2048바이트 제한)
    const dataSize = new Blob([profileString]).size;
    console.log(`사용자 프로필 데이터 크기: ${dataSize}바이트`);

    if (dataSize > 2000) {
      // 여유를 두고 2000바이트로 제한
      console.warn(`사용자 프로필 데이터가 너무 큽니다: ${dataSize}바이트`);

      // 더 필수적인 데이터만 저장
      const essentialProfile = {
        id: userData.id,
        name: userData.name,
        email: userData.email,
        role: userData.role,
        authProvider: userData.authProvider,
      };
      await SecureStore.setItemAsync(
        "userProfile",
        JSON.stringify(essentialProfile)
      );
    } else {
      await SecureStore.setItemAsync("userProfile", profileString);
    }
  } catch (error) {
    console.error("사용자 프로필 저장 실패:", error);
    throw error;
  }
}

// 사용자 프로필 로드
export async function getUserProfile(): Promise<MinimalUserProfile | null> {
  try {
    const profileString = await SecureStore.getItemAsync("userProfile");
    if (!profileString) return null;

    return JSON.parse(profileString) as MinimalUserProfile;
  } catch (error) {
    console.error("사용자 프로필 로드 실패:", error);
    return null;
  }
}

// 사용자 프로필 삭제
export async function clearUserProfile(): Promise<void> {
  try {
    await SecureStore.deleteItemAsync("userProfile");
  } catch (error) {
    console.error("사용자 프로필 삭제 실패:", error);
    throw error;
  }
}
```

### **2. 로그인 시 안전한 저장 함수 사용**

기존의 직접 JSON.stringify() 사용을 새로운 유틸리티 함수로 교체합니다.

```typescript
// app/(auth)/login.tsx
import { saveUserProfile } from "@/utils/userProfile";

// 기존 코드
await SecureStore.setItemAsync("userProfile", JSON.stringify(userData));

// 수정된 코드
await saveUserProfile(userData);
```

### **3. 자동 로그인 시 토큰 유효성 검증**

앱 재시작 시 토큰의 유효성을 서버에서 직접 검증하도록 개선합니다.

```typescript
// hooks/useAuth.ts
loadUserProfile: async () => {
  try {
    const token = await SecureStore.getItemAsync("accessToken");
    if (!token) {
      return;
    }

    // 서버에서 사용자 정보를 직접 조회해서 토큰 유효성 검증
    const userResponse = await apiClient.get("/users/me", {
      headers: { Authorization: `Bearer ${token}` },
    });

    if (userResponse.data?.data?.user) {
      const userData = userResponse.data.data.user;
      await saveUserProfile(userData); // SecureStore에도 저장
      set({
        userProfile: userData as UserProfile,
        isAuthenticated: true,
        token,
      });
    } else {
      // 사용자 데이터가 없으면 토큰이 유효하지 않음
      await SecureStore.deleteItemAsync("accessToken");
      await clearUserProfile();
      set({
        isAuthenticated: false,
        token: null,
        userProfile: null,
      });
    }
  } catch (error) {
    console.error("사용자 프로필 로드 실패:", error);
    // 에러 발생 시 토큰 삭제 (무효한 토큰일 가능성)
    try {
      await SecureStore.deleteItemAsync("accessToken");
      await clearUserProfile();
      set({
        isAuthenticated: false,
        token: null,
        userProfile: null,
      });
    } catch (cleanupError) {
      console.error("토큰 정리 실패:", cleanupError);
    }
  }
},
```

### **4. 데이터 삭제 시 일관성 있는 정리**

로그아웃이나 회원탈퇴 시 새로운 유틸리티 함수를 사용합니다.

```typescript
// app/(auth)/withdrawal.tsx, app/(tabs)/myProfile.tsx
import { clearUserProfile } from "@/utils/userProfile";

// 기존 코드
const keysToRemove = ["accessToken", "refreshToken", "user", "userProfile"];
await Promise.all(keysToRemove.map((key) => SecureStore.deleteItemAsync(key)));

// 수정된 코드
const keysToRemove = ["accessToken", "refreshToken", "user"];
await Promise.all([
  ...keysToRemove.map((key) => SecureStore.deleteItemAsync(key)),
  clearUserProfile(),
]);
```

<br>

## 결론

SecureStore의 2048바이트 제한은 보안을 위한 것이므로, 이를 우회하기보다는 **필요한 데이터만 선별하여 저장**하는 것이 올바른 해결 방법입니다. 위의 방법을 통해 안전하고 효율적인 데이터 저장 시스템을 구축할 수 있습니다.

### 핵심 포인트

1. **데이터 크기 제한**: 2000바이트 이하로 유지
2. **필수 필드만 저장**: id, name, email, role, authProvider
3. **자동 크기 체크**: 저장 전 크기 확인 및 경고
4. **토큰 유효성 검증**: 서버에서 직접 검증
5. **일관성 있는 정리**: 로그아웃 시 완전한 데이터 삭제

이 방법을 통해 SecureStore 경고 없이 안전한 사용자 데이터 관리가 가능합니다.
