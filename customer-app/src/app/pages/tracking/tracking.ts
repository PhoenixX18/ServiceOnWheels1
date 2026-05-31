import { isPlatformBrowser, TitleCasePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import {
  AfterViewInit,
  Component,
  inject,
  OnDestroy,
  PLATFORM_ID,
  signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription, interval, switchMap } from 'rxjs';
import { TrackingService } from '../../services/tracking.service';
import type { TrackingResponse, TrackingStatus } from '../../models/tracking.models';

@Component({
  selector: 'app-tracking',
  imports: [TitleCasePipe, RouterLink],
  templateUrl: './tracking.html',
  styleUrl: './tracking.css',
})
export class TrackingPage implements AfterViewInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly trackingApi = inject(TrackingService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly http = inject(HttpClient);

  readonly trackingData = signal<TrackingResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly loading = signal(true);

  private L: any;
  private map: any;
  private userMarker: any;
  private mechMarker: any;
  private routeLayer: any;
  private pollSub?: Subscription;
  private routeFetched = false;

  async ngAfterViewInit() {
    if (isPlatformBrowser(this.platformId)) {
      try {
        this.L = await import('leaflet');
        // Wait for data before initializing map so the DOM element exists
        this.startPolling();
      } catch (err) {
        console.error('Leaflet load error', err);
        this.errorMessage.set('Failed to load map library.');
        this.loading.set(false);
      }
    }
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
    this.map?.remove();
  }

  // ── Map Setup ──────────────────────────────────────────

  private initMap() {
    if (!document.getElementById('tracking-map')) return;
    
    this.map = this.L.map('tracking-map', {
      zoomControl: false,
      attributionControl: false,
      zoomSnap: 0.1,
      zoomDelta: 0.5,
      wheelPxPerZoomLevel: 120
    }).setView([20.5937, 78.9629], 5);

    this.L.control.zoom({ position: 'bottomright' }).addTo(this.map);
    this.L.control.attribution({ position: 'bottomleft', prefix: false }).addTo(this.map);

    this.L.tileLayer(
      'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',
      {
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a> &copy; <a href="https://carto.com/">CARTO</a>',
        subdomains: 'abcd',
        maxZoom: 19,
      }
    ).addTo(this.map);
  }

  // ── Polling ────────────────────────────────────────────

  private startPolling() {
    const requestId = this.route.snapshot.paramMap.get('requestId');
    if (!requestId) {
      this.errorMessage.set('Invalid request ID.');
      this.loading.set(false);
      return;
    }

    // Initial fetch
    this.fetchData(requestId);

    // Poll every 5 seconds
    this.pollSub = interval(5000)
      .pipe(switchMap(() => this.trackingApi.getTracking(requestId)))
      .subscribe({
        next: (data) => {
          this.trackingData.set(data);
          this.loading.set(false);
          setTimeout(() => {
            if (!this.map) this.initMap();
            this.updateMap(data);
          }, 0);
        },
        error: (err) => {
          console.error('Polling error', err);
        },
      });
  }

  private fetchData(requestId: string) {
    this.trackingApi.getTracking(requestId).subscribe({
      next: (data) => {
        this.trackingData.set(data);
        this.loading.set(false);
        setTimeout(() => {
          if (!this.map) this.initMap();
          this.updateMap(data);
        }, 0);
      },
      error: () => {
        this.errorMessage.set('Could not load tracking data.');
        this.loading.set(false);
      },
    });
  }

  // ── Map Updates ────────────────────────────────────────

  private updateMap(data: TrackingResponse) {
    if (!this.map || !this.L) return;

    const { userLat, userLng, mechanicLat, mechanicLng } = data;

    // User marker (created once)
    if (!this.userMarker && userLat && userLng) {
      const userIcon = this.L.divIcon({
        className: 'custom-user-marker',
        html: `<div class="user-pin">
                 <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 010-5 2.5 2.5 0 010 5z"/></svg>
               </div>`,
        iconSize: [32, 32],
        iconAnchor: [16, 32],
      });
      this.userMarker = this.L.marker([userLat, userLng], { icon: userIcon }).addTo(this.map);
    }

    // Mechanic marker (created once, then updated)
    if (mechanicLat && mechanicLng) {
      if (!this.mechMarker) {
        const mechIcon = this.L.divIcon({
          className: 'custom-mech-marker',
          html: `<div class="mech-pulse-wrapper">
                   <div class="mech-pulse"></div>
                   <div class="mech-pin">
                     <svg viewBox="0 0 24 24" fill="currentColor"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/></svg>
                   </div>
                 </div>`,
          iconSize: [40, 40],
          iconAnchor: [20, 20],
        });
        this.mechMarker = this.L.marker([mechanicLat, mechanicLng], { icon: mechIcon }).addTo(this.map);
      } else {
        this.mechMarker.setLatLng([mechanicLat, mechanicLng]);
      }

      // Fetch OSRM route once
      if (!this.routeFetched && userLat && userLng) {
        this.routeFetched = true;
        this.fetchRoute(userLat, userLng, mechanicLat, mechanicLng);
      }

      // Fit bounds
      if (this.userMarker && this.mechMarker) {
        const bounds = this.L.latLngBounds([
          this.userMarker.getLatLng(),
          this.mechMarker.getLatLng(),
        ]);
        this.map.fitBounds(bounds, { padding: [60, 60], maxZoom: 15 });
      }
    }
  }

  private fetchRoute(uLat: number, uLng: number, mLat: number, mLng: number) {
    const url = `https://router.project-osrm.org/route/v1/driving/${mLng},${mLat};${uLng},${uLat}?overview=full&geometries=geojson`;
    this.http.get<any>(url).subscribe({
      next: (res) => {
        if (res.routes?.length > 0) {
          if (this.routeLayer) this.map.removeLayer(this.routeLayer);
          this.routeLayer = this.L.geoJSON(res.routes[0].geometry, {
            style: { color: '#8B1E1E', weight: 4, opacity: 0.7, dashArray: '8, 6' },
          }).addTo(this.map);
          this.map.fitBounds(this.routeLayer.getBounds(), { padding: [60, 60] });
        }
      },
      error: () => {
        // Fallback: straight line
        this.routeLayer = this.L
          .polyline([[mLat, mLng], [uLat, uLng]], {
            color: '#8B1E1E', weight: 4, opacity: 0.6, dashArray: '8, 6',
          })
          .addTo(this.map);
      },
    });
  }

  // ── Template Helpers ───────────────────────────────────

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'badge-pending';
      case 'ASSIGNED': return 'badge-accepted';
      case 'ON_THE_WAY': 
      case 'ARRIVED':
      case 'IN_SERVICE': return 'badge-progress';
      case 'COMPLETED': return 'badge-completed';
      default: return 'badge-neutral';
    }
  }

  formatStatus(status: string): string {
    return status.replace(/_/g, ' ');
  }

  isStepDone(currentStatus: TrackingStatus, step: TrackingStatus): boolean {
    const order: TrackingStatus[] = ['PENDING', 'ASSIGNED', 'ON_THE_WAY', 'ARRIVED', 'IN_SERVICE', 'COMPLETED'];
    return order.indexOf(currentStatus) >= order.indexOf(step);
  }

  isStepActive(currentStatus: TrackingStatus, step: TrackingStatus): boolean {
    return currentStatus === step;
  }
}
