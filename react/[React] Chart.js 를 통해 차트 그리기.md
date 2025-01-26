> 최근에 회사 과제 전형을 진행하며, 처음으로 리액트를 사용해보았습니다. 
해당 과제는 실시간으로 삼성전자 주식 시세를 확인할 수 있는 웹을 만드는 과제였는데 차트를 보여주기 위해 Chart.js 라이브러리를 사용하게 되었습니다.

## Chart.js 설치하기
먼저 Chart.js 라이브러리를 설치하는 명령어입니다.
```shell
$ npm install --save chart.js react-chartjs-2
혹은
$ yarn add chart.js react-chartjs-2
```

<br>

## Chart.js 시작하기
### 차트 컴포넌트 생성
리액트에서 컴포넌트를 통해 차트를 그릴 수 있으며, [Chart.js Samples](https://www.chartjs.org/docs/latest/samples/information.html) 에 들어가면 Bar, Line 등 차트 종류 별로 컴포넌트를 아래와 코드에서 볼 수 있듯이 import 해서 사용할 수 있다.

```tsx
// Import Line Components
import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

interface DailyStockChartProps {
  data: StockData[];
}

const DailyStockChart: React.FC<DailyStockChartProps> = ({ data }) => {
  const chartData = {
    labels: data.map((item) => item.date),
    datasets: [
      {
        label: "종가",
        data: data.map((item) => item.closePrice),
        borderColor: "rgb(255, 99, 132)",
        backgroundColor: "rgba(255, 99, 132, 0.5)",
        borderWidth: 3,
        yAxisID: "y",
      },
      {
        label: "시가",
        data: data.map((item) => item.openPrice),
        borderColor: "rgb(75, 192, 192)",
        backgroundColor: "rgba(75, 192, 192, 0.5)",
        borderWidth: 3,
        yAxisID: "y",
      },
      {
        label: "거래량",
        data: data.map((item) => item.volume),
        borderColor: "rgb(53, 162, 235)",
        backgroundColor: "rgba(53, 162, 235, 0.5)",
        borderWidth: 3,
        yAxisID: "y1",
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: {
        position: "top" as const,
      },
      title: {
        display: true,
        text: "일별 주가, 시가 및 거래량",
      },
    },
    scales: {
      y: {
        type: "linear" as const,
        display: true,
        position: "left" as const,
      },
      y1: {
        type: "linear" as const,
        display: true,
        position: "right" as const,
        grid: {
          drawOnChartArea: false,
        },
      },
    },
  };

  return <Line data={chartData} options={options} />;
};
```

### 차트 컴포넌트 사용
이제 DailyStockChart 컴포넌트를 다른 컴포넌트에서 사용할 수 있습니다. 예를 들어, DailyStockPage.tsx 파일에 다음과 같이 추가할 수 있습니다.

```tsx
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
```

<br>

## 차트 데이터 설정하기
### 차트 데이터 구성
Chart.js를 사용하기 위한 차트 데이터의 구성과 옵션 설정이며, 위의 DailyStockChart 컴포넌트를 자세히 살펴보겠습니다.

```tsx
const chartData = {
  labels: data.map((item) => item.date),
  datasets: [
    {
      label: "종가",
      data: data.map((item) => item.closePrice),
      borderColor: "rgb(255, 99, 132)",
      backgroundColor: "rgba(255, 99, 132, 0.5)",
      borderWidth: 3,
      yAxisID: "y",
    },
    // ... 다른 데이터셋
  ],
};
```
- **labels**: x축에 표시될 레이블이며, 여기서는 날짜를 사용합니다.
- **datasets**: 실제 차트에 그려질 데이터셋들의 배열
  - **label**: 데이터셋의 이름
  - **data**: 실제 데이터 값
  - **borderColor, backgroundColor**: 선과 영역의 색상
  - **borderWidth**: 선의 두께
  - **yAxisID**: 이 데이터셋이 사용할 y축
  
### 차트 옵션 설정
```tsx
const options = {
  responsive: true,
  plugins: {
    legend: {
      position: "top" as const,
    },
    title: {
      display: true,
      text: "일별 주가, 시가 및 거래량",
    },
  },
  scales: {
    y: {
      type: "linear" as const,
      display: true,
      position: "left" as const,
    },
    y1: {
      type: "linear" as const,
      display: true,
      position: "right" as const,
      grid: {
        drawOnChartArea: false,
      },
    },
  },
};
```
- **responsive**: 차트가 컨테이너의 크기에 반응하여 크기를 조절
- **plugins**: 차트에 추가적인 기능 제공
  - **legend**: 차트의 범례 설정
  - **title**: 차트의 제목 설정
- **scales**: 차트의 축 설정
  - **y, y1**: 두 개의 y축을 설정