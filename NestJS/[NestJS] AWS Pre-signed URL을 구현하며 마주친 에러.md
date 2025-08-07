## 1. CORS 관련 에러

AWS S3에 파일을 업로드할 때 CORS 오류가 발생함.
**원인**: AWS S3 버킷의 CORS 설정에서 클라이언트 도메인이나 PUT 메서드가 허용되지 않은 경우 발생함.

### 에러 해결 방안

1. **NestJS 서버에서 CORS 옵션 설정**

```tsx
// src/main.ts
const corsOptions: CorsOptions = {
  origin: ["http://localhost:3000", "http://localhost:8081/*"],
  credentials: true,
  methods: "GET,HEAD,PUT,PATCH,POST,DELETE",
  allowedHeaders: ["Content-Type", "Authorization"],
  optionsSuccessStatus: 200,
};
```

2. **AWS S3 버킷의 CORS 정책 설정**

```tsx
// AWS S3 버킷 CORS
[
  {
    AllowedHeaders: ["*"],
    AllowedMethods: ["PUT", "GET", "POST", "DELETE"],
    AllowedOrigins: ["*"],
    ExposeHeaders: [],
  },
];
```

<br>

## 2. SignatureDoesNotMatch 에러

AWS S3에 파일을 업로드할 때 서명 불일치 문제가 발생함.
**원인:** 서버에서 Pre-signed URL을 생성할 때 클라이언트에서 업로드 시 ContentType이 다른 값으로 전송됨 → 서버와 클라이언트 간 헤더 불일치로 인해 서명 검증 실패

### 에러 해결 방안

1. **서버에서 Pre-signed URL 생성 시 ContentType 명시**

```tsx
const command = new PutObjectCommand({
  Bucket: this.bucket,
  Key: key,
  ContentType: contentType,
});
```

2. **클라이언트에서도 동일한 Content-Type으로 업로드**

```tsx
const contentType = getMimeType(fileExtension);

await fetch(presignedUrl, {
  method: "PUT",
  body: blob,
  headers: {
    "Content-Type": contentType,
  },
});

// MIME 타입 유틸 함수
const getMimeType = (extension: string): string => {
  const mimeTypes: { [key: string]: string } = {
    jpg: "image/jpeg",
    jpeg: "image/jpeg",
    png: "image/png",
    gif: "image/gif",
    heic: "image/heic",
    heif: "image/heif",
  };

  return mimeTypes[extension.toLowerCase()] || "image/jpeg";
};
```
