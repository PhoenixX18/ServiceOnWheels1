import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TrackingService } from './tracking.service';

describe('TrackingService', () => {
  let service: TrackingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TrackingService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(TrackingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get tracking data', () => {
    const dummyResponse = {
      requestId: 'test-req',
      trackingStatus: 'ASSIGNED',
      vehicleType: 'Car',
      vehicleNumber: 'KA01',
      problemDescription: 'Test',
      eta: '5 min',
      distanceRemaining: '2 km',
      etaSeconds: 300,
      distanceMeters: 2000,
      userLat: 12.9,
      userLng: 77.5,
      mechanicLat: 12.91,
      mechanicLng: 77.51,
      mechanicName: 'Ravi',
      mechanicPhone: '987',
      mechanicVehicle: 'Bike',
      mechanicRating: 4.8
    } as any;

    service.getTracking('test-req').subscribe(res => {
      expect(res).toEqual(dummyResponse);
    });

    const req = httpMock.expectOne('http://localhost:8081/api/tracking/test-req');
    expect(req.request.method).toBe('GET');
    req.flush(dummyResponse);
  });
});
