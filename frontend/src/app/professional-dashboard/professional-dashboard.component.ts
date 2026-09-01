import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth/auth.service';
import { AppointmentService } from '../appointment/appointment.service';
import { Appointment, Availability, AvailabilityRequest, Notification } from '../appointment/appointment.model';

@Component({
  selector: 'app-professional-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './professional-dashboard.component.html',
  styleUrl: './professional-dashboard.component.scss'
})
export class ProfessionalDashboardComponent implements OnInit {
  userEmail = signal<string>('');
  userName = signal<string>('');

  appointments = signal<Appointment[]>([]);
  availabilities = signal<Availability[]>([]);
  notifications = signal<Notification[]>([]);
  unreadCount = signal<number>(0);

  showNotifications = signal<boolean>(false);
  editingAvailability = signal<Availability | null>(null);
  errorMessage = signal<string>('');
  successMessage = signal<string>('');

  availabilityForm: FormGroup;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private appointmentService: AppointmentService,
    private fb: FormBuilder
  ) {
    this.availabilityForm = this.fb.group({
      date: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.http.get<{ email: string; name: string }>('/api/auth/me').subscribe({
      next: (user) => {
        this.userEmail.set(user.email);
        this.userName.set(user.name);
      }
    });

    this.loadData();
  }

  loadData(): void {
    this.appointmentService.getProfessionalAppointments().subscribe({
      next: (data) => this.appointments.set(data)
    });

    this.appointmentService.getProfessionalAvailabilities().subscribe({
      next: (data) => this.availabilities.set(data)
    });

    this.appointmentService.getUnreadNotifications().subscribe({
      next: (data) => {
        this.notifications.set(data);
        this.unreadCount.set(data.length);
      }
    });
  }

  todayAppointments(): Appointment[] {
    const today = new Date().toISOString().split('T')[0];
    return this.appointments().filter(a => a.date === today && a.status !== 'CANCELLED');
  }

  upcomingAppointments(): Appointment[] {
    const today = new Date().toISOString().split('T')[0];
    return this.appointments()
      .filter(a => a.date >= today && a.status !== 'CANCELLED')
      .slice(0, 5);
  }

  availableSlotsCount(): number {
    return this.availabilities().filter(a => !a.booked).length;
  }

  toggleNotifications(): void {
    this.showNotifications.update(value => !value);
  }

  markAsRead(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.appointmentService.markNotificationAsRead(notification.id).subscribe({
      next: () => {
        this.notifications.update(list => list.filter(n => n.id !== notification.id));
        this.unreadCount.update(count => Math.max(0, count - 1));
      }
    });
  }

  startCreate(): void {
    this.editingAvailability.set(null);
    this.availabilityForm.reset();
    this.clearMessages();
  }

  startEdit(availability: Availability): void {
    this.editingAvailability.set(availability);
    this.availabilityForm.patchValue({
      date: availability.date,
      startTime: availability.startTime,
      endTime: availability.endTime
    });
    this.clearMessages();
  }

  cancelEdit(): void {
    this.editingAvailability.set(null);
    this.availabilityForm.reset();
    this.clearMessages();
  }

  saveAvailability(): void {
    if (this.availabilityForm.invalid) {
      return;
    }

    const request: AvailabilityRequest = this.availabilityForm.value;
    const id = this.editingAvailability()?.id;

    const action = id
      ? this.appointmentService.updateAvailability(id, request)
      : this.appointmentService.createAvailability(request);

    action.subscribe({
      next: () => {
        this.successMessage.set(id ? 'Horário atualizado com sucesso.' : 'Horário criado com sucesso.');
        this.availabilityForm.reset();
        this.editingAvailability.set(null);
        this.loadData();
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || err.message || 'Erro ao salvar horário.');
      }
    });
  }

  deleteAvailability(id: number): void {
    if (!confirm('Deseja realmente excluir este horário?')) {
      return;
    }

    this.appointmentService.deleteAvailability(id).subscribe({
      next: () => {
        this.successMessage.set('Horário excluído com sucesso.');
        this.loadData();
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || err.message || 'Erro ao excluir horário.');
      }
    });
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  logout(): void {
    this.authService.logout();
  }
}
