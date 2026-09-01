import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.config';
import {
  Batch,
  BatchAddRequest,
  BatchCompleteRequest,
  BatchDetail,
  BatchEditRequest,
  BatchEvent,
  BatchEventAddRequest,
  BatchStage,
  BatchStageAddRequest,
  BatchStatus,
  Measurement,
  MeasurementAddRequest,
  Page
} from './models';

@Injectable({ providedIn: 'root' })
export class BatchService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/batch`;

  search(filter: {
    search?: string;
    fermentTypeId?: string;
    status?: BatchStatus;
    page?: number;
    size?: number;
  }): Observable<Page<Batch>> {
    let params = new HttpParams()
      .set('page', filter.page ?? 0)
      .set('size', filter.size ?? 20);

    if (filter.search) {
      params = params.set('search', filter.search);
    }
    if (filter.fermentTypeId) {
      params = params.set('fermentTypeId', filter.fermentTypeId);
    }
    if (filter.status) {
      params = params.set('status', filter.status);
    }

    return this.http.get<Page<Batch>>(this.url, { params });
  }

  findOpen(): Observable<Batch[]> {
    return this.http.get<Batch[]>(`${this.url}/open`);
  }

  findById(id: string): Observable<BatchDetail> {
    return this.http.get<BatchDetail>(`${this.url}/${id}`);
  }

  add(request: BatchAddRequest): Observable<BatchDetail> {
    return this.http.post<BatchDetail>(this.url, request);
  }

  update(id: string, request: BatchEditRequest): Observable<BatchDetail> {
    return this.http.put<BatchDetail>(`${this.url}/${id}`, request);
  }

  changeStatus(id: string, status: BatchStatus): Observable<BatchDetail> {
    return this.http.patch<BatchDetail>(`${this.url}/${id}/status`, null, {
      params: new HttpParams().set('status', status)
    });
  }

  complete(id: string, request: BatchCompleteRequest): Observable<BatchDetail> {
    return this.http.post<BatchDetail>(`${this.url}/${id}/complete`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  addStage(id: string, request: BatchStageAddRequest): Observable<BatchStage> {
    return this.http.post<BatchStage>(`${this.url}/${id}/stage`, request);
  }

  startStage(id: string, stageId: string): Observable<BatchStage> {
    return this.http.post<BatchStage>(`${this.url}/${id}/stage/${stageId}/start`, null);
  }

  completeStage(id: string, stageId: string): Observable<BatchStage> {
    return this.http.post<BatchStage>(`${this.url}/${id}/stage/${stageId}/complete`, null);
  }

  addEvent(id: string, request: BatchEventAddRequest): Observable<BatchEvent> {
    return this.http.post<BatchEvent>(`${this.url}/${id}/event`, request);
  }

  addMeasurement(id: string, request: MeasurementAddRequest): Observable<Measurement> {
    return this.http.post<Measurement>(`${this.url}/${id}/measurement`, request);
  }

  deleteMeasurement(id: string, measurementId: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}/measurement/${measurementId}`);
  }
}
