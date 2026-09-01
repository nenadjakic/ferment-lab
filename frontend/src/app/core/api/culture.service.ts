import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.config';
import {
  Culture,
  CultureAddRequest,
  CultureFeeding,
  CultureFeedingRequest,
  CultureSplitRequest,
  CultureTreeNode,
  CultureUpdateRequest
} from './models';

@Injectable({ providedIn: 'root' })
export class CultureService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/culture`;

  findAll(activeOnly = false): Observable<Culture[]> {
    return this.http.get<Culture[]>(this.url, {
      params: new HttpParams().set('activeOnly', activeOnly)
    });
  }

  findTree(): Observable<CultureTreeNode[]> {
    return this.http.get<CultureTreeNode[]>(`${this.url}/tree`);
  }

  findById(id: string): Observable<Culture> {
    return this.http.get<Culture>(`${this.url}/${id}`);
  }

  add(request: CultureAddRequest): Observable<Culture> {
    return this.http.post<Culture>(this.url, request);
  }

  update(id: string, request: CultureUpdateRequest): Observable<Culture> {
    return this.http.put<Culture>(`${this.url}/${id}`, request);
  }

  split(id: string, request: CultureSplitRequest): Observable<Culture> {
    return this.http.post<Culture>(`${this.url}/${id}/split`, request);
  }

  findFeedings(id: string): Observable<CultureFeeding[]> {
    return this.http.get<CultureFeeding[]>(`${this.url}/${id}/feeding`);
  }

  feed(id: string, request: CultureFeedingRequest): Observable<CultureFeeding> {
    return this.http.post<CultureFeeding>(`${this.url}/${id}/feeding`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
