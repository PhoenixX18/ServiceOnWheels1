/**
 * Tracking data models — mirrors the backend TrackingResponse DTO.
 */

export type TrackingStatus = 'PENDING' | 'ASSIGNED' | 'ON_THE_WAY' | 'ARRIVED' | 'COMPLETED';

export interface TrackingResponse {
  requestId: string;
  trackingStatus: TrackingStatus;

  // Request info
  vehicleType: string;
  vehicleNumber: string;
  problemDescription: string;
  selectedIssue?: string;
  additionalNotes?: string;
  address: string;

  // ETA & distance
  eta: string;
  distanceRemaining: string;
  etaSeconds: number;
  distanceMeters: number;

  // User location
  userLat: number;
  userLng: number;

  // Mechanic current (simulated) location
  mechanicLat: number;
  mechanicLng: number;

  // Mechanic details
  mechanicName: string;
  mechanicPhone: string;
  mechanicVehicle: string;
  mechanicRating: number;
}
