import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';

export interface LocationState {
  latitude: number;
  longitude: number;
  address: string;
  source: 'GPS' | 'Map';
}

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private readonly http = inject(HttpClient);

  /**
   * Retrieves the user's current GPS location.
   * Returns a promise with lat/lng, or throws an error string if denied/failed.
   */
  getCurrentPosition(): Promise<{ lat: number; lng: number }> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject('Geolocation is not supported by your browser.');
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (pos) => {
          resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        },
        (error) => {
          switch (error.code) {
            case error.PERMISSION_DENIED:
              reject('Location permission was denied. Please allow location access or select on map.');
              break;
            case error.POSITION_UNAVAILABLE:
              reject('Location information is unavailable.');
              break;
            case error.TIMEOUT:
              reject('The request to get user location timed out.');
              break;
            default:
              reject('An unknown error occurred while detecting location.');
              break;
          }
        },
        { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
      );
    });
  }

  /**
   * Performs reverse geocoding using OpenStreetMap Nominatim.
   */
  reverseGeocode(lat: number, lng: number): Observable<string> {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`;
    return this.http.get<any>(url).pipe(
      map(res => {
        if (res && res.display_name) {
          // Format the address cleanly. Often it returns a very long string.
          // Let's take the first two meaningful parts (e.g. Suburb, City) if possible
          const parts = res.display_name.split(',').map((p: string) => p.trim());
          if (parts.length > 2) {
            return `${parts[0]}, ${parts[1]}`; // Keep it clean
          }
          return res.display_name;
        }
        return 'Unknown Address';
      }),
      catchError(() => of('Address not found'))
    );
  }
}
