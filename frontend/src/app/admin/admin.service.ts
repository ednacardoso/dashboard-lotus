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

  getClients(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.apiUrl}/clients`);
  }

  createClient(request: CreateUserRequest): Observable<CreateUserResponse> {
    return this.http.post<CreateUserResponse>(`${this.apiUrl}/clients`, request);
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
