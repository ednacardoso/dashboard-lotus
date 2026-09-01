import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Appointment, AppointmentRequest, Availability, AvailabilityRequest, Notification, Professional } from './appointment.model';
import { CreateUserRequest, CreateUserResponse } from '../admin/admin.model';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api';

  getProfessionals(): Observable<Professional[]> {
    return this.http.get<Professional[]>(`${this.apiUrl}/professionals`);
  }

  getAvailabilities(professionalId: number): Observable<Availability[]> {
    return this.http.get<Availability[]>(`${this.apiUrl}/availabilities`, {
      params: { professionalId: professionalId.toString() }
    });
  }

  getProfessionalAvailabilities(): Observable<Availability[]> {
    return this.http.get<Availability[]>(`${this.apiUrl}/professional/availabilities`);
  }

  createAvailability(request: AvailabilityRequest): Observable<Availability> {
    return this.http.post<Availability>(`${this.apiUrl}/professional/availabilities`, request);
  }

  updateAvailability(id: number, request: AvailabilityRequest): Observable<Availability> {
    return this.http.put<Availability>(`${this.apiUrl}/professional/availabilities/${id}`, request);
  }

  deleteAvailability(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/professional/availabilities/${id}`);
  }

  getMyAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.apiUrl}/appointments/my`);
  }

  getProfessionalAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.apiUrl}/professional/appointments`);
  }

  createAppointment(request: AppointmentRequest): Observable<Appointment> {
    return this.http.post<Appointment>(`${this.apiUrl}/appointments`, request);
  }

  updateAppointment(id: number, request: AppointmentRequest): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.apiUrl}/appointments/${id}`, request);
  }

  cancelAppointment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/appointments/${id}`);
  }

  getNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/professional/notifications`);
  }

  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/professional/notifications/unread`);
  }

  getUnreadNotificationCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/professional/notifications/unread/count`);
  }

  markNotificationAsRead(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/professional/notifications/${id}/read`, {});
  }

  createClient(request: CreateUserRequest): Observable<CreateUserResponse> {
    return this.http.post<CreateUserResponse>(`${this.apiUrl}/professional/clients`, request);
  }
}
