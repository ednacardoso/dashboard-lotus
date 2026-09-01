import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth/auth.service';
import { AppointmentService } from '../appointment/appointment.service';
import { Appointment, Availability, AvailabilityRequest, Notification } from '../appointment/appointment.model';
import { CreateUserResponse } from '../admin/admin.model';

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

  showClientForm = signal<boolean>(false);
  showClientPassword = signal<boolean>(false);
  showGeneratedPassword = signal<boolean>(false);
  createdUser = signal<CreateUserResponse | null>(null);

  clientForm: FormGroup;
  availabilityForm: FormGroup;

  private readonly strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()\-_+=<>?]).{8,}$/;

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

    this.clientForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      definePassword: [false],
      password: ['', [Validators.required, Validators.pattern(this.strongPasswordRegex)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });

    this.updatePasswordValidators(this.clientForm, false);
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

  private passwordMatchValidator(form: FormGroup): null | { passwordMismatch: true } {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    if (form.get('definePassword')?.value && password !== confirmPassword) {
      return { passwordMismatch: true };
    }
    return null;
  }

  private updatePasswordValidators(form: FormGroup, definePassword: boolean): void {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');

    if (definePassword) {
      password?.setValidators([Validators.required, Validators.pattern(this.strongPasswordRegex)]);
      confirmPassword?.setValidators([Validators.required]);
    } else {
      password?.clearValidators();
      confirmPassword?.clearValidators();
    }

    password?.updateValueAndValidity();
    confirmPassword?.updateValueAndValidity();
  }

  onDefineClientPasswordChange(): void {
    const definePassword = this.clientForm.get('definePassword')?.value;
    this.updatePasswordValidators(this.clientForm, definePassword);
  }

  createClient(): void {
    if (this.clientForm.invalid) {
      return;
    }

    const definePassword = this.clientForm.value.definePassword;
    const request = {
      name: this.clientForm.value.name,
      email: this.clientForm.value.email,
      ...(definePassword ? { password: this.clientForm.value.password } : {})
    };

    this.appointmentService.createClient(request).subscribe({
      next: (response) => {
        this.createdUser.set(response);
        this.successMessage.set('Cliente cadastrado com sucesso.');
        this.clientForm.reset({ definePassword: false });
        this.updatePasswordValidators(this.clientForm, false);
      },
      error: (err) => this.handleError(err)
    });
  }

  copyPassword(): void {
    const password = this.createdUser()?.generatedPassword;
    if (password) {
      navigator.clipboard.writeText(password);
    }
  }

  closeCreatedUserModal(): void {
    this.createdUser.set(null);
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

  private handleError(err: any): void {
    this.errorMessage.set(err.error?.message || err.message || 'Erro ao processar solicitação.');
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  logout(): void {
    this.authService.logout();
  }
}
