import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  ViewEncapsulation,
  afterNextRender,
  effect,
  inject,
  input,
  viewChild
} from '@angular/core';

import { ECharts, EChartsOption, init } from './echarts.setup';

/**
 * Thin ECharts wrapper.
 *
 * The chart instance is created once the host element is in the DOM, kept in sync with [option]
 * through an effect, and resized by a ResizeObserver so it behaves inside CSS grid layouts.
 */
@Component({
  selector: 'app-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  template: `<div #host class="w-full" [style.height.px]="height()" role="img" [attr.aria-label]="ariaLabel()"></div>`
})
export class ChartComponent {
  readonly option = input.required<EChartsOption>();
  readonly height = input(300);

  /** Text alternative for the chart, required for screen reader users. */
  readonly ariaLabel = input('Chart');

  private readonly host = viewChild.required<ElementRef<HTMLDivElement>>('host');
  private readonly destroyRef = inject(DestroyRef);

  private chart?: ECharts;
  private observer?: ResizeObserver;

  constructor() {
    afterNextRender(() => {
      const element = this.host().nativeElement;

      this.chart = init(element, undefined, { renderer: 'canvas' });
      this.chart.setOption(this.option());

      this.observer = new ResizeObserver(() => this.chart?.resize());
      this.observer.observe(element);
    });

    // `notMerge` so removing a series (for example dropping a batch from the comparison) actually
    // removes its line instead of leaving a stale one behind.
    effect(() => {
      const option = this.option();
      this.chart?.setOption(option, { notMerge: true });
    });

    this.destroyRef.onDestroy(() => {
      this.observer?.disconnect();
      this.chart?.dispose();
    });
  }
}
