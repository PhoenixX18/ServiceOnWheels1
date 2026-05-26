import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LocationService } from './location.service';

describe('LocationService', () => {
  let service: LocationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LocationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(LocationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should reverse geocode properly', () => {
    const dummyResponse = {
      display_name: 'Koramangala, Bengaluru, Karnataka, 560034, India'
    };

    service.reverseGeocode(12.97, 77.59).subscribe(address => {
      expect(address).toBe('Koramangala, Bengaluru');
    });

    const req = httpMock.expectOne('https://nominatim.openstreetmap.org/reverse?format=json&lat=12.97&lon=77.59');
    expect(req.request.method).toBe('GET');
    req.flush(dummyResponse);
  });

  it('should handle geocoding errors', () => {
    service.reverseGeocode(0, 0).subscribe(address => {
      expect(address).toBe('Address not found');
    });

    const req = httpMock.expectOne('https://nominatim.openstreetmap.org/reverse?format=json&lat=0&lon=0');
    req.error(new ProgressEvent('network error'));
  });
});
