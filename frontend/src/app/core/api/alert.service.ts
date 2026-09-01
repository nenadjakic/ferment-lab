import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.config';
import { Alert } from './models';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/alert`;

  findAll(includeAcknowledged = false): Observable<Alert[]> {
    return this.http.get<Alert[]>(this.url, {
      params: new HttpParams().set('includeAcknowledged', includeAcknowledged)
    });
  }

  acknowledge(id: string): Observable<Alert> {
    return this.http.post<Alert>(`${this.url}/${id}/acknowledge`, null);
  }

  acknowledgeAll(): Observable<{ acknowledged: number }> {
    return this.http.post<{ acknowledged: number }>(`${this.url}/acknowledge-all`, null);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
