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
  selectedIssue?: string;
  additionalNotes?: string;
  latitude: number;
  longitude: number;
  address: string;
}

export interface ServiceRequestResponse {
  id: string;
  userId: string;
  vehicleType: string;
  vehicleNumber: string;
  problemDescription: string;
  selectedIssue?: string;
  additionalNotes?: string;
  latitude: number;
  longitude: number;
  address: string;
  status: RequestStatus;
  assignedMechanicId: string | null;
  /** ISO-8601 string from the API */
  createdAt: string;
}
