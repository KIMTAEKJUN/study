현재 프로젝트를 진행하면서 이미지를 업로드하는 기능을 개발했습니다. 처음에는 서버에 직접 이미지를 업로드하는 방식을 고려했으나, 하지만 서버 용량 한계와 백업 등을 고려할 때 AWS S3를 활용하는 것이 더 적합하다고 판단했습니다.

현재 프로젝트에서는

1. **Pre-signed URL을 활용한 이미지 업로드 기능**
2. **이미지 타입별 분류 및 저장, 관리**
3. **예약 및 리뷰와 연동**

이 글에서는 구현 과정에 대한 정리하고자 합니다.

## 1. AWS S3 설정 방법

### **1-1. AWS S3 버킷 생성 및 설정**

먼저 AWS 콘솔에서 S3 버킷을 생성합니다. 이후에는 업로드 및 접근 권한을 위해 버킷 정책 설정과 IAM 사용자 권한을 추가해줘야 합니다.

또한 프론트엔드에서 S3로 직접 이미지를 업로드하려면, CORS 설정을 통해 클라이언트 도메인 및 요청 메서드를 허용해야 정상적으로 동작합니다.

```typescript
// AWS S3 버킷 권한
{
    "Version": "2012-10-17",
    "Id": "id",
    "Statement": [
        {
            "Sid": "sid",
            "Effect": "Allow",
            "Principal": "*",
            "Action": [
                "s3:DeleteObject",
                "s3:GetObject",
                "s3:PutObject"
            ],
            "Resource": "arn:aws:s3:::your-bucket-name/*"
        }
    ]
}
```

```typescript
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

```typescript
// AWS IAM 사용자 권한
{
	"Version": "2012-10-17",
	"Statement": [
		{
			"Sid": "sidName",
			"Effect": "Allow",
			"Action": [
				"s3:PutObject",
				"s3:GetObject",
				"s3:DeleteObject",
				"s3:ListBucket"
			],
			"Resource": [
				"arn:aws:s3:::your-bucket-name",
				"arn:aws:s3:::your-bucket-name/*"
			]
		}
	]
}
```

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

### **1-2. AWS SDK 설치 및 설정**

```bash
npm install @aws-sdk/client-s3 @aws-sdk/s3-request-presigner uuid
npm install -D @types/uuid
```

### **1-3. 환경 변수 설정**

.env 파일에 AWS 관련 설정을 추가합니다. 액세스 키(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)는 IAM 사용자 생성 후 발급된 키를 사용해야 합니다.

```
AWS_REGION=ap-northeast-2
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_S3_BUCKET=your-bucket-name
```

### **1-4. ConfigModule 설정**

```tsx
// src/config/config.ts
export default () => ({
  aws: {
    region: process.env.AWS_REGION,
    accessKeyId: process.env.AWS_ACCESS_KEY_ID,
    secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
    bucket: process.env.AWS_S3_BUCKET,
  },
});
```

### 1-5. 이미지 엔티티 정의

```tsx
// src/common/entities/image.entity.ts
@Entity()
export class Image extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  imageUrl: string;

  @Column({
    type: "enum",
    enum: ImageType,
  })
  imageType: ImageType;

  @Column({ nullable: true, default: 0 })
  order: number;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;

  @ManyToOne(() => User)
  @JoinColumn({ name: "uploaderId" })
  uploader: User;

  @ManyToOne(() => Reservation, (reservation) => reservation.images, {
    onDelete: "CASCADE",
  })
  @JoinColumn({ name: "reservationId" })
  reservation: Reservation;

  @ManyToOne(() => Review, (review) => review.images, {
    onDelete: "CASCADE",
  })
  @JoinColumn({ name: "reviewId" })
  review: Review;
}
```

<br>

## **2. Pre-signed URL을 활용한 이미지 업로드 방식**

### **2-1. Pre-signed URL의 개념**

Presigned URL은 특정 시간 동안만 유효한 임시 URL로, 클라이언트가 AWS 인증 정보 없이도 S3에 직접 파일을 업로드할 수 있게 해줍니다.

**Pre-signed URL 방식의 장점은 아래와 같습니다.**

1. **서버 부하 감소**: 파일이 서버를 거치지 않고 클라이언트에서 S3로 직접 전송됩니다.
2. **보안 강화**: AWS 인증 정보가 클라이언트에 노출되지 않습니다.
3. **빠른 파일 전송**: 대용량 파일도 효율적으로 업로드할 수 있습니다.

### **2-2. 업로드 흐름 정리**

1. 프론트에서 이미지 선택 (필요 시 리사이징)
2. 백엔드에 Pre-signed URL 요청
3. 프론트에서 S3에 직접 업로드
4. 업로드된 URL을 백엔드에 전달하여 DB에 저장

### 2-3. AWS S3 서비스 구현

Pre-signed URL 생성 로직을 구현합니다.

```tsx
// src/aws/aws-s3.service.ts
@Injectable()
export class AwsS3Service {
  private s3Client: S3Client;
  private bucket: string;

  constructor(
    private configService: ConfigService,
    @InjectRepository(Image)
    private readonly imageRepository: Repository<Image>,
    @InjectRepository(Review)
    private readonly reviewRepository: Repository<Review>,
    @InjectRepository(Reservation)
    private readonly reservationRepository: Repository<Reservation>
  ) {
    this.s3Client = new S3Client({
      region: this.configService.get<string>("aws.region") || "",
      credentials: {
        accessKeyId: this.configService.get<string>("aws.accessKeyId") || "",
        secretAccessKey:
          this.configService.get<string>("aws.secretAccessKey") || "",
      },
    });
    this.bucket = this.configService.get<string>("aws.bucket") || "";
  }

  // 이미지 업로드
  async generatePresignedUrl(
    imageType: ImageType,
    fileExtension: string,
    contentType: string,
    user: User,
    reservationId?: number,
    reviewId?: number
  ): Promise<{ presignedUrl: string; imageUrl: string; imageId: number }> {
    // 허용된 파일 형식 정의
    const allowedMimeTypes = {
      jpg: "image/jpeg",
      jpeg: "image/jpeg",
      png: "image/png",
      gif: "image/gif",
      heic: "image/heic",
      heif: "image/heif",
    };

    // 파일 확장자 소문자로 변환
    const normalizedExtension = fileExtension.toLowerCase();

    // 1. 파일 확장자 검증
    if (!Object.keys(allowedMimeTypes).includes(normalizedExtension)) {
      throw new BadRequestException(AppError.AWS.INVALID_FILE_EXTENSION);
    }

    // 2. 콘텐츠 타입 검증 (확장자에 맞는 콘텐츠 타입인지 확인)
    const expectedContentType = allowedMimeTypes[normalizedExtension];
    if (contentType !== expectedContentType) {
      throw new BadRequestException(AppError.AWS.INVALID_CONTENT_TYPE);
    }

    // 파일 이름 생성 (UUID + 확장자)
    const fileName = `${uuidv4()}.${fileExtension}`;

    // 이미지 타입에 따른 폴더 경로 설정
    const folderPath = this.getFolderPath(imageType);
    const key = `${folderPath}/${fileName}`;

    // S3에 업로드할 객체 명령 생성
    const command = new PutObjectCommand({
      Bucket: this.bucket,
      Key: key,
      ContentType: contentType,
    });

    // presigned URL 생성 (유효 시간 5분)
    const presignedUrl = await getSignedUrl(this.s3Client, command, {
      expiresIn: 300,
    });

    // 이미지 URL 생성
    const imageUrl = `https://${
      this.bucket
    }.s3.${this.configService.get<string>("aws.region")}.amazonaws.com/${key}`;

    // 최대 순서 계산 (기본값 0)
    let maxOrder = 0;

    // 이미지 순서 계산 (생략)...

    // 이미지 생성
    const image = this.imageRepository.create({
      imageUrl,
      imageType,
      order: maxOrder + 1,
      uploader: user,
    });

    // 이미지 타입에 따른 연관 엔티티 설정 (생략)...

    // 이미지 저장
    const savedImage = await this.imageRepository.save(image);

    return {
      presignedUrl,
      imageUrl,
      imageId: savedImage.id,
    };
  }
}
```

<br>

## 3. 코드를 파헤쳐 보자!

### **3-1. 파일 확장자 및 MIME 타입 검증**

보안과 안정성을 위해 업로드할 수 있는 파일 형식을 제한하고, 파일 확장자와 콘텐츠 타입을 검증합니다.

```tsx
// 허용된 파일 형식 정의
const allowedMimeTypes = {
  jpg: "image/jpeg",
  jpeg: "image/jpeg",
  png: "image/png",
  gif: "image/gif",
  heic: "image/heic",
  heif: "image/heif",
};

// 파일 확장자 소문자로 변환
const normalizedExtension = fileExtension.toLowerCase();

// 파일 확장자 유효성 검증
if (!Object.keys(allowedMimeTypes).includes(normalizedExtension)) {
  throw new BadRequestException("지원하지 않는 파일 형식입니다.");
}

// 콘텐츠 타입 검증
const expectedContentType = allowedMimeTypes[normalizedExtension];
if (contentType !== expectedContentType) {
  throw new BadRequestException("유효하지 않은 콘텐츠 타입입니다.");
}
```

### **3-2. UUID를 활용한 고유 파일명 생성**

파일명 충돌과 보안 이슈를 방지하기 위해 UUID를 사용하여 고유한 파일명을 생성합니다.

```tsx
import { v4 as uuidv4 } from "uuid";

// 고유한 파일명 생성
const fileName = `${uuidv4()}.${fileExtension}`;
```

<br>

## 4. 이미지 타입별 관리

### 4-1. 이미지 타입 정의

프로젝트에서 여러 도메인에 이미지의 용도에 따라 타입을 구분하여 관리하기 위해 구현했습니다.

```tsx
// src/common/enums/image.enums.ts
export enum ImageType {
  REVIEW = "리뷰",
  RESERVATION = "예약",
  WORK_PROCESS = "작업 과정",
}
```

### 4-2. 타입별 폴더 구조

이거 또한 위와 같이 AWS S3 버킷 내에서 이미지 타입별로 폴더를 나누어 관리하기 위해 구현했습니다.

```tsx
// 이미지 타입에 따른 폴더 경로 설정
private getFolderPath(imageType: ImageType): string {
  switch (imageType) {
    case ImageType.REVIEW:
      return 'images/reviews';
    case ImageType.RESERVATION:
      return 'images/reservations';
    case ImageType.WORK_PROCESS:
      return 'images/work_process';
    default:
      return 'images/others';
  }
}
```

<br>

## 5. 이미지 삭제 기능 구현

### 5-1. AWS S3 객체 삭제 구현

AWS S3에서 이미지 파일을 삭제하는 로직을 구현했습니다.

```tsx
// src/aws/aws-s3.service.ts
async deleteObject(key: string): Promise<void> {
  try {
    const objectKey = this.extractKeyFromUrl(key);

    const image = await this.imageRepository.findOne({
      where: { imageUrl: Like(`%${objectKey}%`) },
    });

    // 이미지 존재 여부 확인
    if (image) {
      const command = new DeleteObjectCommand({
        Bucket: this.bucket,
        Key: objectKey,
      });

      try {
        await this.s3Client.send(command);
        await this.imageRepository.remove(image);
      } catch (error) {
        throw new InternalServerErrorException(AppError.AWS.S3_DELETE_FAILED);
      }
    } else {
      throw new NotFoundException(AppError.AWS.IMAGE_NOT_FOUND);
    }
  } catch (error) {
    throw error;
  }
}
```

<br>

## 6. 실제 코드

### 6-1. 컨트롤러 구현

```tsx
// src/aws/aws-s3.controller.ts
@Controller("upload")
export class AwsS3Controller {
  constructor(private readonly awsS3Service: AwsS3Service) {}

  // 일반 사용자용 이미지 업로드
  @Post("presigned-url")
  @UseGuards(JwtAuthGuard)
  async generatePresignedUrl(@Body() body: PresignedUrlRequestDto, @Req() req) {
    const { imageType, fileExtension, contentType, reservationId, reviewId } =
      body;
    const user = req.user;

    if (
      user.role !== UserRole.ADMIN &&
      this.awsS3Service.isAdminOnlyImageType(imageType)
    ) {
      throw new ForbiddenException(AppError.AUTH.ADMIN_ONLY_IMAGE_TYPE);
    }

    const result = await this.awsS3Service.generatePresignedUrl(
      imageType,
      fileExtension,
      contentType,
      user,
      reservationId,
      reviewId
    );

    return {
      status: "success",
      data: result,
    };
  }

  // 관리자용 이미지 업로드
  @Post("admin/presigned-url")
  @UseGuards(JwtAuthGuard, AdminGuard)
  async generateAdminPresignedUrl(
    @Body() body: PresignedUrlRequestDto,
    @Req() req
  ) {
    const { imageType, fileExtension, contentType } = body;
    const user = req.user;

    const result = await this.awsS3Service.generatePresignedUrl(
      imageType,
      fileExtension,
      contentType,
      user
    );

    return {
      status: "success",
      data: result,
    };
  }

  // 관리자용 이미지(object) 삭제
  @Delete("admin/delete-object")
  @UseGuards(JwtAuthGuard, AdminGuard)
  async deleteObject(@Body() body: DeleteImageRequestDto) {
    await this.awsS3Service.deleteObject(body.key);

    return {
      status: "success",
      data: null,
    };
  }
}
```

### 6-2. DTO 정의

```tsx
// src/aws/dtos/aws-request.dto.ts
export class PresignedUrlRequestDto {
  @IsEnum(ImageType)
  @IsNotEmpty()
  imageType: ImageType;

  @IsString()
  @IsNotEmpty()
  @Matches(/^(jpg|jpeg|png|gif|heic|heif)$/i)
  fileExtension: string;

  @IsString()
  @IsNotEmpty()
  @Matches(/^image\/(jpeg|png|gif|heic|heif)$/)
  contentType: string;

  @IsNumber()
  @IsOptional()
  reservationId?: number;

  @IsNumber()
  @IsOptional()
  reviewId?: number;
}

export class DeleteImageRequestDto {
  @IsString()
  @IsNotEmpty()
  key: string;
}

// src/aws/dtos/aws-response.dto.ts
export class AwsResponseDto {
  presignedUrl: string;
  imageUrl: string;
  imageId: number;
}
```

<br>

## 7. 클라이언트 구현 (React-Native)

### 7-1. AWS S3 이미지 업로드 유틸리티 구현

```tsx
// 이미지 선택 함수
export const pickImage = async (options = {}) => {
  const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();

  if (status !== "granted") {
    throw new Error("이미지를 선택하려면 갤러리 접근 권한이 필요합니다.");
  }

  // 이미지 선택
  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: "images",
    allowsEditing: true,
    aspect: [4, 3],
    quality: 0.8,
    ...options,
  });

  if (result.canceled || !result.assets || result.assets.length === 0) {
    return null;
  }

  return result.assets[0];
};

// S3에 이미지 업로드 함수
export const uploadImageToS3 = async (
  imageUri: string,
  imageType: ImageType,
  reservationId?: number,
  reviewId?: number
): Promise<{ imageId: string; imageUrl: string }> => {
  try {
    const token = await AsyncStorage.getItem("accessToken");

    // 1. 이미지 타입 확인
    const fileExtension = imageUri.split(".").pop()?.toLowerCase() || "jpg";
    const contentType = getMimeType(fileExtension);

    // 2. 백엔드에서 pre-signed URL 요청
    const response = await apiClient.post(
      "/upload/presigned-url",
      {
        imageType,
        fileExtension,
        contentType,
        reservationId,
        reviewId,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );

    // 3. 응답에서 presignedUrl과 imageId, imageUrl 추출
    const { presignedUrl, imageId, imageUrl } = response.data.data;

    // 4. pre-signed URL을 통해 S3에 직접 업로드
    const blob = await fetchImageFromUri(imageUri);

    await fetch(presignedUrl, {
      method: "PUT",
      body: blob,
      headers: {
        "Content-Type": contentType,
      },
    });

    // 5. 업로드된 이미지 URL 반환
    return { imageId, imageUrl };
  } catch (error) {
    console.error("S3 업로드 오류:", error);
    throw new Error("이미지 업로드에 실패했습니다.");
  }
};

// URI에서 Blob 객체 생성 함수
const fetchImageFromUri = async (uri: string) => {
  const response = await fetch(uri);
  const blob = await response.blob();
  return blob;
};

// 파일 확장자로 MIME 타입 반환
const getMimeType = (extension: string) => {
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

### 7-2. 이미지 선택 및 처리

```tsx
const [currentImageUri, setCurrentImageUri] = useState<string | null>(null);

const handleSelectImage = async () => {
  if (items.length >= MAX_IMAGE) {
    Alert.alert(
      "안내",
      `최대 ${MAX_IMAGE}개까지 이미지를 업로드할 수 있습니다.`
    );
    return;
  }

  try {
    const result = await pickImage({
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.5,
    });

    if (result) {
      if (result.fileSize && result.fileSize > MAX_IMAGE_SIZE) {
        Alert.alert("안내", "이미지 크기는 5MB 이하여야 합니다.");
        return;
      }

      setCurrentImageUri(result.uri);
    }
  } catch (error) {
    console.error("이미지 선택 오류:", error);
    Alert.alert("오류", "이미지를 선택하는 중 문제가 발생했습니다.");
  }
};

const uploadImage = async (uri: string) => {
  try {
    const imageData = await uploadImageToS3(uri, ImageType.RESERVATION);
    console.log("업로드된 이미지 데이터:", imageData);
    return imageData;
  } catch (error) {
    console.error("이미지 업로드 오류:", error);
    Alert.alert("오류", "이미지 업로드에 실패했습니다.");
    return null;
  }
};
```

<br>

## 8. 마무리

이번 글에서는 AWS S3와 Pre-signed URL을 활용한 이미지 업로드 구현 과정에 대해 정리해보았습니다.
이 글이 이미지 업로드를 구현하려는 분들께 도움이 되었길 바랍니다 🙌
끝까지 읽어주셔서 감사합니다 😊
