import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AdminUser,
  AdminAppointment,
  Room,
  RoomOccupancy,
  RoomRental,
  CreateUserRequest,
  CreateProfessionalRequest,
  UpdateProfessionalRequest,
  UpdateClientRequest,
  CreateUserResponse,
  RoomRequest,
  RoomRentalRequest
} from './admin.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/admin';

  getProfessionals(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.apiUrl}/professionals`);
  }

  createProfessional(request: CreateProfessionalRequest): Observable<CreateUserResponse> {
    return this.http.post<CreateUserResponse>(`${this.apiUrl}/professionals`, request);
  }

  updateProfessional(id: number, request: UpdateProfessionalRequest): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.apiUrl}/professionals/${id}`, request);
  }

  deleteProfessional(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/professionals/${id}`);
  }

  getClients(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.apiUrl}/clients`);
  }

  createClient(request: CreateUserRequest): Observable<CreateUserResponse> {
    return this.http.post<CreateUserResponse>(`${this.apiUrl}/clients`, request);
  }

  updateClient(id: number, request: UpdateClientRequest): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.apiUrl}/clients/${id}`, request);
  }

  deleteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/clients/${id}`);
  }

  getAppointments(): Observable<AdminAppointment[]> {
    return this.http.get<AdminAppointment[]>(`${this.apiUrl}/appointments`);
  }

  getRooms(): Observable<Room[]> {
    return this.http.get<Room[]>('/api/admin/rooms');
  }

  createRoom(request: RoomRequest): Observable<Room> {
    return this.http.post<Room>('/api/admin/rooms', request);
  }

  updateRoom(id: number, request: RoomRequest): Observable<Room> {
    return this.http.put<Room>(`/api/admin/rooms/${id}`, request);
  }

  deleteRoom(id: number): Observable<void> {
    return this.http.delete<void>(`/api/admin/rooms/${id}`);
  }

  toggleRoomActive(id: number): Observable<Room> {
    return this.http.patch<Room>(`/api/admin/rooms/${id}/toggle`, {});
  }

  getVacantRooms(yearMonth: string): Observable<Room[]> {
    return this.http.get<Room[]>(`/api/admin/rooms/vacant`, {
      params: { yearMonth }
    });
  }

  getOccupiedRooms(yearMonth: string): Observable<RoomOccupancy[]> {
    return this.http.get<RoomOccupancy[]>(`/api/admin/rooms/occupied`, {
      params: { yearMonth }
    });
  }

  getRoomRentals(yearMonth: string): Observable<RoomRental[]> {
    return this.http.get<RoomRental[]>(`/api/admin/rooms/rentals`, {
      params: { yearMonth }
    });
  }

  rentRoom(request: RoomRentalRequest): Observable<RoomRental> {
    return this.http.post<RoomRental>('/api/admin/rooms/rentals', request);
  }

  removeRental(id: number): Observable<void> {
    return this.http.patch<void>(`/api/admin/rooms/rentals/${id}/remove`, {});
  }
}
