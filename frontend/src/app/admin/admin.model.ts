export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: string;
  specialty: string | null;
  createdAt: string;
}

export interface AdminAppointment {
  id: number;
  clientName: string;
  clientEmail: string;
  professionalName: string;
  professionalSpecialty: string;
  date: string;
  startTime: string;
  endTime: string;
  status: string;
  roomName: string | null;
}

export interface Room {
  id: number;
  name: string;
  description: string | null;
  capacity: number | null;
  monthlyPrice: number;
  active: boolean;
  createdAt: string;
}

export interface RoomOccupancy {
  roomId: number;
  roomName: string;
  professionalId: number;
  professionalName: string;
  professionalSpecialty: string;
  yearMonth: string;
}

export interface RoomRental {
  id: number;
  professionalId: number;
  professionalName: string;
  professionalSpecialty: string;
  roomId: number;
  roomName: string;
  yearMonth: string;
  active: boolean;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password?: string;
}

export interface CreateProfessionalRequest extends CreateUserRequest {
  specialty: string;
}

export interface CreateUserResponse {
  id: number;
  name: string;
  email: string;
  role: string;
  specialty: string | null;
  passwordGenerated: boolean;
  generatedPassword: string;
}

export interface RoomRequest {
  name: string;
  description: string;
  capacity: number;
  monthlyPrice: number;
}

export interface RoomRentalRequest {
  professionalId: number;
  roomId: number;
  yearMonth: string;
}
