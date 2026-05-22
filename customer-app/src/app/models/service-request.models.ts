export type RequestStatus =
  | 'PENDING'
  | 'ACCEPTED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'TOW_REQUIRED';

export interface CreateServiceRequestPayload {
  vehicleType: string;
  vehicleNumber: string;
  problemDescription: string;
  latitude: number;
  longitude: number;
}

export interface ServiceRequestResponse {
  id: string;
  userId: string;
  vehicleType: string;
  vehicleNumber: string;
  problemDescription: string;
  latitude: number;
  longitude: number;
  status: RequestStatus;
  assignedMechanicId: string | null;
  /** ISO-8601 string from the API */
  createdAt: string;
}
