> [[React] Chart.js 를 통해 차트 그리기](https://velog.io/@kimtaekjun/React-Chart.js-%EB%A5%BC-%ED%86%B5%ED%95%B4-%EC%B0%A8%ED%8A%B8-%EA%B7%B8%EB%A6%AC%EA%B8%B0) 게시물에 써져있듯이 같이 회사 과제 전형을 진행하며, 실시간으로 주식 시세 데이터를 패칭하기 위해 Axios와 React-Query를 사용하게 되었습니다.

## Axios와 React-Query 설치하기
먼저 Axios와 React-Query를 설치하는 명령어입니다.
```shell
$ npm install @tanstack/react-query @tanstack/react-query-devtools axios
혹은
$ yarn add @tanstack/react-query @tanstack/react-query-devtools axios
```

<br>

## API 설정
### 기본 API 설정
먼저 Axios를 설치한 후, 기본 API 클라이언트를 설정합니다.
```tsx
// base.tsx
import axios, { AxiosError } from "axios";

const BASE_URL = "API_BASE_URL";

export const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

export const handleApiError = (error: any): never => {
  if (error instanceof AxiosError) {
    throw new Error(
      `API 에러: ${error.response?.data.message || error.message}`
    );
  }
  throw error;
};
```

### 주식 데이터 API 설정
기본 API 클라이언트를 설정한 후, 주식 데이터를 가져오는 구체적인 API를 설정합니다.
```tsx
// stock.tsx
// 일별 주식 데이터 조회
export const fetchDailyStockData = async (
  days: number
): Promise<StockData[]> => {
  try {
    const response = await api.get<StockData[]>(`/daily`, {
      params: { days },
    });
    return response.data;
  } catch (error) {
    return handleApiError(error);
  }
};

// 주별 주식 데이터 조회
export const fetchWeeklyStockData = async (
  weeks: number
): Promise<StockData[]> => {
  try {
    const response = await api.get<StockData[]>(`/weekly`, {
      params: { weeks },
    });
    return response.data;
  } catch (error) {
    return handleApiError(error);
  }
};

// 월별 주식 데이터 조회
export const fetchMonthlyStockData = async (
  months: number
): Promise<StockData[]> => {
  try {
    const response = await api.get<StockData[]>(`/monthly`, {
      params: { months },
    });
    return response.data;
  } catch (error) {
    return handleApiError(error);
  }
};

// 최신 주식 데이터 조회 (일별 데이터의 첫 번째 항목 사용)
export const fetchLatestStockData = async (): Promise<Stock> => {
  try {
    const data = await fetchDailyStockData(1);
    if (data.length === 0) {
      throw new Error("No stock data available");
    }
    const latestData = data[0];

    // StockData를 Stock 형식으로 변환
    return {
      stockCode: "005930", // 삼성전자 종목 코드
      stockName: "삼성전자",
      currentPrice: latestData.closePrice,
      priceChange: latestData.closePrice - latestData.openPrice,
      changeRate:
        ((latestData.closePrice - latestData.openPrice) /
          latestData.openPrice) *
        100,
    };
  } catch (error) {
    return handleApiError(error);
  }
};

```

<br>

## React-Query 사용하기
### QueryClient 생성 및 Provider 설정
index.tsx 에서 QueryClient와 QueryClientProvider를 설정합니다.
```tsx
// index.tsx
import React from "react";
import { createRoot } from "react-dom/client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import "./index.css";

// 전역 상태 관리를 위한 QueryClient 생성 (캐싱, 훅 등)
const queryClient = new QueryClient();

const container = document.getElementById("root");
const root = createRoot(container!);

root.render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>
);
```

### 앱 구조 설정
App.tsx에서 라우팅과 전체 레이아웃을 설정합니다.
```tsx
// App.tsx
import React from "react";
import { Route, Routes } from "react-router-dom";
import Header from "./components/UI/Header";
import Footer from "./components/UI/Footer";
import HomePage from "./pages/HomePage";
import DailyStockPage from "./pages/stock/DailyStockPage";
import WeeklyStockPage from "./pages/stock/WeeklyStockPage";
import MonthlyStockPage from "./pages/stock/MonthlyStockPage";

const App: React.FC = () => {
  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      <Header />
      <main className="flex-grow container mx-auto px-4 py-8">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/daily" element={<DailyStockPage />} />
          <Route path="/weekly" element={<WeeklyStockPage />} />
          <Route path="/monthly" element={<MonthlyStockPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
};

export default App;

```

## React-Query 훅 사용하기
### 일별 주식 데이터 조회
```tsx
// DailyStockPage.tsx
import React from "react";
import { useQuery } from "@tanstack/react-query";
import DailyStockChart from "./_components/DailyStockChart";
import { fetchDailyStockData } from "../../_api/stock";
import { StockData } from "../../types/stock";
import LoadingSpinner from "../../components/etc/LoadingSpinner";
import ErrorMessage from "../../components/etc/ErrorMessage";

const DailyStockPage: React.FC = () => {
  const { data, isLoading, isError } = useQuery<StockData[], Error>({
    queryKey: ["dailyStockData"],
    queryFn: () => fetchDailyStockData(30),
  });

  if (isLoading) return <LoadingSpinner />;
  if (isError)
    return <ErrorMessage message="데이터 조회 중 오류가 발생했습니다." />;
  if (!data || data.length === 0)
    return <ErrorMessage message="데이터가 없습니다." />;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl text-center font-bold mb-6 text-gray-800">
        일별 삼성전자 실시간 주식 시세 정보
      </h1>
      <div className="bg-white rounded-lg shadow-md p-6">
        <DailyStockChart data={data} />
      </div>
    </div>
  );
};

// MonthlyStockPage, WeeklyStockPage 등등
```

### 최신 주식 데이터 조회
```tsx
// useStockData.ts
import { useQuery } from "@tanstack/react-query";
import { fetchLatestStockData } from "../_api/stock";
import { Stock } from "../types/stock";

export const useStockData = () => {
  return useQuery<Stock, Error>({
    queryKey: ["latestStockData"],
    queryFn: fetchLatestStockData,
    refetchInterval: 60000, // 1분마다 데이터 갱신
  });
};
```