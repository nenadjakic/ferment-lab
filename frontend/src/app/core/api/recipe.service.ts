import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './api.config';
import { Recipe, RecipeAddRequest, RecipeDetail } from './models';

@Injectable({ providedIn: 'root' })
export class RecipeService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/recipe`;

  findAll(fermentTypeId?: string, featuredOnly = false): Observable<Recipe[]> {
    let params = new HttpParams().set('featuredOnly', featuredOnly);
    if (fermentTypeId) {
      params = params.set('fermentTypeId', fermentTypeId);
    }

    return this.http.get<Recipe[]>(this.url, { params });
  }

  findById(id: string): Observable<RecipeDetail> {
    return this.http.get<RecipeDetail>(`${this.url}/${id}`);
  }

  add(request: RecipeAddRequest): Observable<RecipeDetail> {
    return this.http.post<RecipeDetail>(this.url, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
