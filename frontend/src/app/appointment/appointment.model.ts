export interface Professional {
  id: number;
  name: string;
  specialty: string;
}

export interface Availability {
  id: number;
  date: string;
  startTime: string;
  endTime: string;
}

export interface Appointment {
  id: number;
  professionalId: number;
  professionalName: string;
  professionalSpecialty: string;
  date: string;
  startTime: string;
  endTime: string;
  status: string;
}

export interface AppointmentRequest {
  availabilityId: number;
}
