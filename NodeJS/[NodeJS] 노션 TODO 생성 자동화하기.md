> 회사를 다니게 되면서 매일 아침마다 TODO 쓰게 되었는데 하나하나 일일이 생성을 하니 너무 귀찮아서 매일 아침 8시 30분(주말 제외)에 자동으로 생성하게 만들었다. 그런데 소문을 들어보니 노션에도 똑같은 기능이 있다고...

## 프로젝트 설명

### 프로젝트 기능

1. **노션 TODO 자동 생성(평일 아침 8시 30분(주말제외))**

   - 형식: 📅 mm월 dd일 TODO 제목, 4개 섹션, "TODO" 태그
   - 섹션: 🚀 진행전 작업/📝 진행중 작업/✅ 완료된 작업/📚 학습 노트
   - 이전 날짜의 진행전, 진행중 미완료 항목 자동 이전

2. **슬랙 알림**
   - TODO 생성 알림
   - 이전 날짜의 진행전, 진행중 미완료 항목 목록 표시

### 프로젝트 파일 구조

```
NOTION-TODO/
├── dist/
├── node_modules/
├── src/
│   ├── config/
│   │   └── index.ts
│   ├── errors/
│   │   └── index.ts
│   ├── services/
│   │   ├── notion.service.ts
│   │   └── slack.service.ts
│   ├── types/
│   │   ├── notion.types.ts
│   │   └── slack.types.ts
│   └── utils/
│       ├── blocks.ts
│       └── date.ts
├── index.ts
├── .env
├── .gitignore
├── package-lock.json
├── package.json
├── README.md
└── tsconfig.json
```

<br>

## 프로젝트 초기 환경 설정

### 프로젝트 생성

프로젝트 디렉토리를 생성하고 npm 프로젝트를 초기화한다.

```shell
# 프로젝트 디렉토리 생성 및 이동
mkdir notion-todo
cd notion-todo

# npm 프로젝트 초기화
npm init -y
```

### 프로젝트에 필요한 패키지 설치

Notion API, Slack API, 환경변수 관리, 스케줄링을 위한 패키지와 TypeScript 개발에 필요한 패키지들을 설치한다.

```shell
# 메인 패키지 설치
npm install @notionhq/client @slack/web-api dotenv node-cron

# 개발 의존성 패키지 설치
npm install --save-dev typescript @types/node @types/node-cron ts-node nodemon
```

### Typescript 설정

TypeScript 설정 파일을 생성하여 프로젝트에 맞는 컴파일 옵션을 설정한다.

```shell
npx tsc --init
```

### tsconfig.json 설정

TypeScript 컴파일러 옵션을 프로젝트에 맞게 구성한다.

```json
{
  "compilerOptions": {
    "target": "ES6",
    "module": "commonjs",
    "outDir": "./dist",
    "rootDir": "./src",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules"]
}
```

### package.json 설정

개발 및 배포에 필요한 npm 스크립트를 설정한다.

```json
"scripts": {
    "start": "ts-node src/index.ts",
    "dev": "nodemon --exec ts-node src/index.ts",
    "build": "tsc"
  },
```

### .gitignore 설정

Git 버전 관리에서 제외할 파일과 디렉토리를 지정한다.

```plaintext
/dist
/node_modules
.env
```

### .env 설정

Notion API와 Slack API 사용에 필요한 환경 변수들을 설정한다.

```shell
NOTION_API_KEY=YOUR_NOTION_API_KEY
NOTION_DATABASE_ID=YOUR_NOTION_DATABASE_ID
SLACK_TOKEN=YOUR_SLACK_TOKEN
SLACK_CHANNEL=YOUR_SLACK_CHANNEL
```

<br>

## 프로젝트 설정

### 설정 관리 및 에러 처리

우선 .env로 선언해두었던 환경 변수와 에러 처리를 설정한다.

```typescript
// config/index.ts
import dotenv from "dotenv";

dotenv.config();

export const CONFIG = {
  NOTION: {
    API_KEY: process.env.NOTION_API_KEY!,
    DATABASE_ID: process.env.NOTION_DATABASE_ID!,
  },
  SLACK: {
    TOKEN: process.env.SLACK_TOKEN!,
    CHANNEL: process.env.SLACK_CHANNEL || "#todo-notifications",
  },
};
```

```typescript
// errors/index.ts
export class AppError extends Error {
  constructor(message: string, public statusCode: number) {
    super(message);
    this.name = "AppError";
  }
}

export function handleError(error: unknown): void {
  if (error instanceof AppError) {
    console.error(`❌ Error ${error.statusCode} 
                     메시지: ${error.message}`);
  } else if (error instanceof Error) {
    console.error(`❌ Error ${error.name} 
                     메시지: ${error.message}`);
  } else {
    console.error("❌ 알 수 없는 에러가 발생했습니다.");
  }
}
```

### 타입 정의 및 유틸리티 함수

Notion과 Slack 인터페이스 및 타입을 정의하여 타입 안정성을 확보한다.

```typescript
// types/notion.types.ts
// Notion Todo 섹션 인터페이스
export interface TodoSection {
  pendingTodos: string[];
  inProgressTodos: string[];
}

// Notion 블록 구조 인터페이스
export interface NotionBlock {
  type: string;
  heading_2?: {
    rich_text: Array<{ plain_text: string }>;
  };
  to_do?: {
    rich_text: Array<{ plain_text: string }>;
    checked: boolean;
  };
}

// Notion Todo 섹션 타입 Enum
export enum TodoSectionType {
  PENDING = "pending",
  IN_PROGRESS = "inProgress",
  NONE = "none",
}

// types/slack.types.ts
import { TodoSection } from "./notion.types";

// Slack 메시지 구조 인터페이스
export interface SlackBlock {
  type: "section";
  text: {
    type: "mrkdwn";
    text: string;
  };
}

// Slack 알림 내용 구조 인터페이스
export interface SlackNotificationContent {
  todayMessage: string;
  beforeDayMessage: string;
  todos: TodoSection;
}
```

## 핵심 기능 구현

### Notion TODO 생성 서비스

매일 아침 새로운 TODO 페이지를 생성하며, 이전 날짜의 미완료 항목을 자동으로 이전한다.

```typescript
// services/notion.service.ts
import { Client } from "@notionhq/client";
import { CONFIG } from "../config/index";
import { AppError } from "../errors";
import { createHeading, createTodo, createParagraph } from "../utils/blocks";
import { getDateStr, getISODateStr, getLastWorkday } from "../utils/date";
import {
  NotionBlock,
  TodoSection,
  TodoSectionType,
} from "../types/notion.types";
import {
  CreatePageResponse,
  BlockObjectRequest,
} from "@notionhq/client/build/src/api-endpoints";

export class NotionService {
  private client: Client;

  constructor() {
    this.client = new Client({ auth: CONFIG.NOTION.API_KEY });
  }

  // 이전 날짜의 미완료된 TODO 항목들을 가져오는 메서드
  async getYesterdayUncompletedTodos(): Promise<TodoSection> {
    try {
      const lastWorkday = getLastWorkday();
      const dateStr = getDateStr(lastWorkday);
      const isoDate = getISODateStr(lastWorkday);

      // Notion 데이터베이스에서 특정 날짜에 해당하는 TODO 페이지를 조회
      const response = await this.client.databases.query({
        database_id: CONFIG.NOTION.DATABASE_ID,
        filter: {
          and: [
            {
              property: "이름",
              title: {
                contains: dateStr,
              },
            },
            {
              property: "날짜",
              date: {
                equals: isoDate,
              },
            },
          ],
        },
      });

      // 해당 날짜의 페이지가 없는 경우 빈 배열 반환
      if (!response.results.length) {
        return {
          pendingTodos: [],
          inProgressTodos: [],
        };
      }

      // 해당 페이지의 블록들을 조회 (TODO 항목들이 블록으로 저장됨)
      const blocks = await this.client.blocks.children.list({
        block_id: response.results[0].id,
      });

      return this.extractTodos(blocks.results as NotionBlock[]);
    } catch (error) {
      throw new AppError("할 일 목록을 불러오는 중 오류가 발생했습니다.", 500);
    }
  }

  // 금일 TODO 페이지를 생성하는 메서드
  async createDailyTodo(): Promise<CreatePageResponse> {
    try {
      const today = new Date();
      const { pendingTodos, inProgressTodos } =
        await this.getYesterdayUncompletedTodos();

      const children = this.buildPageBlocks({ pendingTodos, inProgressTodos });

      return this.client.pages.create({
        parent: { database_id: CONFIG.NOTION.DATABASE_ID },
        icon: { type: "emoji", emoji: "📅" },
        properties: this.buildPageProperties(today),
        children,
      });
    } catch (error) {
      throw new AppError("TODO 생성 실패", 500);
    }
  }

  // ... 기타 private 메서드는 생략
}
```

### Slack 알림 서비스

TODO 생성 결과와 미완료 항목을 Slack 채널에 알림으로 전송하여 업무 현황을 공유한다.

```typescript
// services/slack.service.ts
import { WebClient } from "@slack/web-api";
import { CONFIG } from "../config";
import { AppError } from "../errors";
import { SlackNotificationContent, SlackBlock } from "../types/slack.types";

export class SlackService {
  private client: WebClient;

  constructor() {
    this.client = new WebClient(CONFIG.SLACK.TOKEN);
  }

  // TODO 생성 알림을 해당 채널로 전송
  async sendNotification({
    todayMessage,
    beforeDayMessage,
    todos: { pendingTodos, inProgressTodos },
  }: SlackNotificationContent): Promise<void> {
    try {
      const blocks = this.buildBlocks({
        todayMessage,
        beforeDayMessage,
        pendingTodos,
        inProgressTodos,
      });

      await this.client.chat.postMessage({
        channel: CONFIG.SLACK.CHANNEL,
        text: todayMessage,
        blocks,
      });
    } catch (error) {
      throw new AppError("슬랙 알림 전송 실패", 503);
    }
  }

  // 에러 발생 시 알림 전송
  async sendErrorNotification(errorMessage: string): Promise<void> {
    await this.sendNotification({
      todayMessage: `🚨 ${errorMessage}`,
      beforeDayMessage: "🧑🏻‍💻 서비스를 확인해주세요.",
      todos: {
        pendingTodos: [],
        inProgressTodos: [],
      },
    });
  }

  // ... 기타 private 메서드는 생략
}
```

### 애플리케이션 실행 관리

cron을 사용하여 평일 아침 8시 30분에 자동으로 TODO를 생성하고 알림을 전송하는 스케줄러를 구현한다.

```typescript
// index.ts
import cron from "node-cron";
import { NotionService } from "./services/notion.service";
import { SlackService } from "./services/slack.service";
import { handleError } from "./errors";
import { getDateStr, getLastWorkday, isWeekend } from "./utils/date";

class TodoApplication {
  private readonly notionService: NotionService;
  private readonly slackService: SlackService;

  constructor() {
    this.notionService = new NotionService();
    this.slackService = new SlackService();
    this.setupErrorHandlers();
  }

  // 금일의 TODO 생성 및 알림 전송
  private async runDailyTodo(): Promise<void> {
    // 주말에는 실행하지 않음
    if (isWeekend()) {
      console.log("주말에는 실행하지 않습니다.");
      return;
    }

    try {
      const lastWorkday = getLastWorkday();
      const todayDateStr = getDateStr();
      const lastWorkdayStr = getDateStr(lastWorkday);

      // 1. 이전 날짜의 미완료 TODO 항목들을 가져옴
      const { pendingTodos, inProgressTodos } =
        await this.notionService.getYesterdayUncompletedTodos();

      // 2. 금일의 TODO 페이지를 생성
      await this.notionService.createDailyTodo();

      // 3. Slack으로 알림 전송
      await this.slackService.sendNotification({
        todayMessage: `📅 *금일 [${todayDateStr}]의 TODO가 생성되었습니다.*`,
        beforeDayMessage: `🛵 *전날 [${lastWorkdayStr}]의 미완료 항목이 이전되었습니다.*`,
        todos: {
          pendingTodos,
          inProgressTodos,
        },
      });

      console.log("✅ 모든 작업이 완료되었습니다.");
    } catch (error) {
      console.error("오류 발생:", error);
      handleError(error);
      throw error;
    }
  }

  public start(): void {
    // 평일 아침 8시 30분에 실행(월요일 ~ 금요일)
    cron.schedule("30 8 * * 1-5", async () => {
      try {
        await this.runDailyTodo();
      } catch (error) {
        console.error("CRON 작업 중 오류가 발생했습니다.", error);
      }
    });

    console.log("🚀 Notion TODO Application이 시작되었습니다.");
  }

  // ... 에러 핸들링 관련 메서드는 생략
}

const app = new TodoApplication();
app.start();
```

<br>

## 실행 방법

### pm2를 통한 무중단 배포

```shell
# TypeScript 코드를 JavaScript로 컴파일
npm run build

# PM2 글로벌 설치
npm install -g pm2

# 컴파일된 JavaScript 파일을 PM2로 실행 ('notion-todo'라는 이름으로)
pm2 start dist/index.js --name "notion-todo"

# 서버 재시작 시에도 PM2가 자동으로 시작되도록 설정
sudo pm2 startup

# 현재 실행 중인 프로세스 목록을 저장
sudo pm2 save

# 실시간으로 로그를 확인
pm2 logs
```

### pm2 재시작

```shell
# 프로세스 중지
sudo pm2 stop 0

# 모든 프로세스 삭제
sudo pm2 delete all

# 프로세스 다시 시작
pm2 start dist/index.js --name "notion-todo"
```

<br>

## 실행 결과

![](https://velog.velcdn.com/images/kimtaekjun/post/04bc1d12-98be-4042-930a-5a0b3e059f5f/image.jpg)
