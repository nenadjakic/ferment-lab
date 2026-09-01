import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';

import { API_BASE_URL } from './api.config';
import { FermentType } from './models';

/**
 * Ferment style reference data. The list never changes at runtime, so it is cached for the lifetime
 * of the application.
 */
@Injectable({ providedIn: 'root' })
export class FermentTypeService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/ferment-type`;

  private cached?: Observable<FermentType[]>;

  findAll(): Observable<FermentType[]> {
    this.cached ??= this.http.get<FermentType[]>(this.url).pipe(shareReplay({ bufferSize: 1, refCount: false }));
    return this.cached;
  }

  findById(id: string): Observable<FermentType> {
    return this.http.get<FermentType>(`${this.url}/${id}`);
  }
}
