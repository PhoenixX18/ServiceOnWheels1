import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, inject, OnDestroy, PLATFORM_ID, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ServiceRequestService } from '../../services/service-request.service';
import { LocationService } from '../../services/location.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-request-service',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './request-service.html',
  styleUrl: './request-service.css',
})
export class RequestServicePage implements AfterViewInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ServiceRequestService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly locationService = inject(LocationService);

  readonly errorMessage = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly locating = signal(false);
  readonly locationSet = signal(false);
  readonly detectedAddress = signal<string>('No location selected');

  private map: any;
  private marker: any;
  private L: any;

  readonly form = this.fb.nonNullable.group({
    vehicleType: ['', [Validators.required, Validators.maxLength(80)]],
    vehicleNumber: ['', [Validators.required, Validators.maxLength(32)]],
    selectedIssue: ['', [Validators.required]],
    customIssue: ['', [Validators.maxLength(4000)]],
    additionalNotes: ['', [Validators.maxLength(4000)]],
    latitude: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(-90),
      Validators.max(90),
    ]),
    longitude: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(-180),
      Validators.max(180),
    ]),
    address: ['', [Validators.required, Validators.maxLength(255)]],
  });

  constructor() {
    this.form.controls.selectedIssue.valueChanges.subscribe((val) => {
      const customCtrl = this.form.controls.customIssue;
      if (val === 'Other') {
        customCtrl.setValidators([Validators.required, Validators.maxLength(4000)]);
      } else {
        customCtrl.clearValidators();
        customCtrl.setValidators([Validators.maxLength(4000)]);
      }
      customCtrl.updateValueAndValidity();
    });
  }

  async ngAfterViewInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.L = await import('leaflet');
      this.initMap();
    }
  }

  ngOnDestroy() {
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap() {
    // Default map view centered to a reasonable default (e.g. India)
    this.map = this.L.map('map', {
      zoomSnap: 0.1,
      zoomDelta: 0.5,
      wheelPxPerZoomLevel: 120
    }).setView([20.5937, 78.9629], 5);
    
    this.L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a> &copy; <a href="https://carto.com/">CARTO</a>'
    }).addTo(this.map);

    // Ensure Leaflet recalculates map size properly
    setTimeout(() => {
      if (this.map) {
        this.map.invalidateSize();
      }
    }, 250);

    this.map.on('click', (e: any) => {
      this.updateLocation(e.latlng.lat, e.latlng.lng);
    });
  }

  private updateLocation(lat: number, lng: number) {
    if (!this.L) return;

    if (this.marker) {
      this.marker.setLatLng([lat, lng]);
    } else {
      const customIcon = this.L.divIcon({
        className: 'custom-map-marker',
        html: `<div style="color: #ef4444; filter: drop-shadow(0 4px 6px rgba(0,0,0,0.5));">
                 <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor" stroke="white" stroke-width="1.5">
                   <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 010-5 2.5 2.5 0 010 5z"/>
                 </svg>
               </div>`,
        iconSize: [40, 40],
        iconAnchor: [20, 40],
      });
      this.marker = this.L.marker([lat, lng], { icon: customIcon, draggable: true }).addTo(this.map);
      this.marker.on('dragend', (e: any) => {
        const pos = e.target.getLatLng();
        this.updateLocation(pos.lat, pos.lng);
      });
    }
    
    this.map.setView([lat, lng], 15);
    this.form.patchValue({
      latitude: Number(lat.toFixed(6)),
      longitude: Number(lng.toFixed(6))
    });
    this.locationSet.set(true);
    this.fetchAddress(lat, lng);
  }

  private fetchAddress(lat: number, lng: number) {
    this.detectedAddress.set('Locating address...');
    this.locationService.reverseGeocode(lat, lng).subscribe({
      next: (address) => {
        this.detectedAddress.set(address);
        this.form.patchValue({ address });
      },
      error: () => {
        this.detectedAddress.set('Address not found');
        this.toast.error('Geocoding Failed', 'Could not fetch address for this location.');
      }
    });
  }

  useMyLocation(): void {
    this.errorMessage.set(null);
    this.locating.set(true);
    
    this.locationService.getCurrentPosition()
      .then(pos => {
        this.locating.set(false);
        this.updateLocation(pos.lat, pos.lng);
        this.toast.success('Location set', 'Your current location has been detected.');
      })
      .catch(errorMsg => {
        this.locating.set(false);
        this.toast.warning('Location failed', errorMsg);
      });
  }

  onSubmit(): void {
    this.errorMessage.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.warning('Incomplete form', 'Please fill in all required fields and select a location.');
      return;
    }

    const raw = this.form.getRawValue();
    const latitude = Number(raw.latitude);
    const longitude = Number(raw.longitude);
    if (Number.isNaN(latitude) || Number.isNaN(longitude)) {
      this.toast.error('Invalid coordinates', 'Latitude and longitude must be valid numbers.');
      return;
    }

    this.submitting.set(true);

    const problemDesc = raw.selectedIssue === 'Other' ? raw.customIssue.trim() : raw.selectedIssue;

    this.api
      .createRequest({
        vehicleType: raw.vehicleType.trim(),
        vehicleNumber: raw.vehicleNumber.trim(),
        problemDescription: problemDesc,
        selectedIssue: raw.selectedIssue,
        additionalNotes: raw.additionalNotes.trim(),
        latitude,
        longitude,
        address: raw.address.trim() || 'Unknown location',
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.toast.success('Request submitted!', 'Help is on the way. We\'ll dispatch a mechanic shortly.');
          void this.router.navigateByUrl('/my-requests');
        },
        error: (err: unknown) => {
          this.submitting.set(false);
          const msg = extractHttpMessage(err);
          this.errorMessage.set(msg);
          this.toast.error('Submission failed', msg);
        },
      });
  }
}

function extractHttpMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as { message?: string; validationErrors?: Record<string, string> } | undefined;
    if (body?.validationErrors && typeof body.validationErrors === 'object') {
      const first = Object.values(body.validationErrors)[0];
      if (first) return first;
    }
    return body?.message ?? err.message ?? 'Request failed';
  }
  return 'Something went wrong';
}
