import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TrackingPage } from './tracking';

describe('TrackingPage', () => {
  let component: TrackingPage;
  let fixture: ComponentFixture<TrackingPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrackingPage],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TrackingPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('isStepDone should work correctly', () => {
    expect(component.isStepDone('ON_THE_WAY', 'PENDING')).toBe(true);
    expect(component.isStepDone('ASSIGNED', 'ARRIVED')).toBe(false);
    expect(component.isStepDone('COMPLETED', 'COMPLETED')).toBe(true);
  });

  it('isStepActive should work correctly', () => {
    expect(component.isStepActive('ON_THE_WAY', 'ON_THE_WAY')).toBe(true);
    expect(component.isStepActive('ASSIGNED', 'PENDING')).toBe(false);
  });
});
