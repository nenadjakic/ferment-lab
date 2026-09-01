import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.config';
import { BatchComparison, DashboardSummary, MetricSeries, MetricType, Milestone } from './models';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/analytics`;

  dashboard(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.url}/dashboard`);
  }

  milestones(): Observable<Milestone[]> {
    return this.http.get<Milestone[]>(`${this.url}/milestone`);
  }

  series(batchId: string): Observable<MetricSeries[]> {
    return this.http.get<MetricSeries[]>(`${this.url}/batch/${batchId}/series`);
  }

  /**
   * Overlays two to six batches. Repeating the `batchId` parameter preserves the order the user
   * picked, which keeps chart colours stable.
   */
  compare(batchIds: string[], metrics: MetricType[]): Observable<BatchComparison> {
    let params = new HttpParams();
    batchIds.forEach((id) => (params = params.append('batchId', id)));
    metrics.forEach((metric) => (params = params.append('metric', metric)));

    return this.http.get<BatchComparison>(`${this.url}/comparison`, { params });
  }
}
