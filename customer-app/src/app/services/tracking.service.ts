import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import type { TrackingResponse } from '../models/tracking.models';

/**
 * Angular service for the tracking API.
 * Currently uses HTTP polling; architecture supports future WebSocket swap
 * by simply changing the Observable source inside getTracking().
 */
@Injectable({ providedIn: 'root' })
export class TrackingService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = 'http://localhost:8081';

  /**
   * Fetch current tracking state for a service request.
   * Called on an interval by the tracking page.
   */
  getTracking(requestId: string): Observable<TrackingResponse> {
    return this.http.get<TrackingResponse>(`${this.apiBase}/api/tracking/${requestId}`);
  }
}
