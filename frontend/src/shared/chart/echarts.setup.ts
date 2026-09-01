import { BarChart, LineChart, PieChart, TreeChart } from 'echarts/charts';
import {
  DatasetComponent,
  GridComponent,
  LegendComponent,
  MarkAreaComponent,
  MarkLineComponent,
  TooltipComponent
} from 'echarts/components';
import { init, use } from 'echarts/core';
import type { ECharts, EChartsCoreOption } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';

/**
 * Registers only the ECharts pieces the application actually draws, which keeps the bundle far
 * smaller than importing the full library.
 */
use([
  LineChart,
  BarChart,
  PieChart,
  TreeChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  MarkLineComponent,
  MarkAreaComponent,
  DatasetComponent,
  CanvasRenderer
]);

export { init };
export type { ECharts };

export type EChartsOption = EChartsCoreOption;

/**
 * Shared dark theme fragments, so every chart in the app lines up visually.
 */
export const CHART_TEXT_COLOR = '#a8a29e';
export const CHART_LINE_COLOR = '#292524';

export const CHART_PALETTE = ['#f59e0b', '#38bdf8', '#a78bfa', '#34d399', '#fb7185', '#facc15'];
