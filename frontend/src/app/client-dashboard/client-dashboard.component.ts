import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { AppointmentService } from '../appointment/appointment.service';
import { Appointment, Availability, Professional } from '../appointment/appointment.model';

type ViewMode = 'appointments' | 'schedule';
type ModalMode = 'create' | 'edit';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.scss'
})
export class ClientDashboardComponent implements OnInit {
  userEmail = signal<string>('');
  userName = signal<string>('');
  activeView = signal<ViewMode>('appointments');

  professionals = signal<Professional[]>([]);
  availabilities = signal<Availability[]>([]);
  appointments = signal<Appointment[]>([]);
  selectedProfessional = signal<Professional | null>(null);
  selectedAvailability = signal<Availability | null>(null);

  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  showModal = signal<boolean>(false);
  modalMode = signal<ModalMode>('create');
  editingAppointment = signal<Appointment | null>(null);

  upcomingAppointments = computed(() =>
    this.appointments().filter(a => a.status !== 'CANCELLED')
  );

  pastAppointments = computed(() =>
    this.appointments().filter(a => a.status === 'CANCELLED')
  );

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private appointmentService: AppointmentService
  ) {}

  ngOnInit(): void {
    this.http.get<{ email: string; name: string }>('/api/auth/me').subscribe({
      next: (user) => {
        this.userEmail.set(user.email);
        this.userName.set(user.name);
      }
    });
    this.loadAppointments();
  }

  setView(view: ViewMode): void {
    this.activeView.set(view);
    this.clearMessages();

    if (view === 'schedule') {
      this.loadProfessionals();
    }
  }

  loadProfessionals(): void {
    this.isLoading.set(true);
    this.appointmentService.getProfessionals().subscribe({
      next: (professionals) => {
        this.professionals.set(professionals);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Erro ao carregar profissionais.');
        this.isLoading.set(false);
      }
    });
  }

  loadAvailabilities(professional: Professional): void {
    this.selectedProfessional.set(professional);
    this.selectedAvailability.set(null);
    this.isLoading.set(true);
    this.appointmentService.getAvailabilities(professional.id).subscribe({
      next: (availabilities) => {
        this.availabilities.set(availabilities);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Erro ao carregar horários disponíveis.');
        this.isLoading.set(false);
      }
    });
  }

  loadAppointments(): void {
    this.isLoading.set(true);
    this.appointmentService.getMyAppointments().subscribe({
      next: (appointments) => {
        this.appointments.set(appointments);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Erro ao carregar agendamentos.');
        this.isLoading.set(false);
      }
    });
  }

  selectAvailability(availability: Availability): void {
    this.selectedAvailability.set(availability);
  }

  openConfirmationModal(mode: ModalMode = 'create', appointment?: Appointment): void {
    if (!this.selectedAvailability()) return;

    this.modalMode.set(mode);
    this.editingAppointment.set(appointment || null);
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
    this.editingAppointment.set(null);
  }

  confirmAppointment(): void {
    const availability = this.selectedAvailability();
    if (!availability) return;

    this.isLoading.set(true);
    this.clearMessages();

    const request = { availabilityId: availability.id };

    if (this.modalMode() === 'edit' && this.editingAppointment()) {
      this.appointmentService.updateAppointment(this.editingAppointment()!.id, request).subscribe({
        next: () => {
          this.handleSuccess('Agendamento atualizado com sucesso!');
        },
        error: (err) => this.handleError(err)
      });
    } else {
      this.appointmentService.createAppointment(request).subscribe({
        next: () => {
          this.handleSuccess('Agendamento confirmado com sucesso!');
        },
        error: (err) => this.handleError(err)
      });
    }
  }

  startEdit(appointment: Appointment): void {
    this.editingAppointment.set(appointment);
    this.modalMode.set('edit');
    this.activeView.set('schedule');
    this.clearMessages();

    const loadAndSelect = () => {
      const professional = this.professionals().find(p => p.id === appointment.professionalId);
      if (!professional) {
        this.errorMessage.set('Profissional não encontrado.');
        this.isLoading.set(false);
        return;
      }

      this.selectedProfessional.set(professional);
      this.isLoading.set(true);
      this.appointmentService.getAvailabilities(professional.id).subscribe({
        next: (availabilities) => {
          this.availabilities.set(availabilities);
          this.isLoading.set(false);
        },
        error: () => {
          this.errorMessage.set('Erro ao carregar horários para edição.');
          this.isLoading.set(false);
        }
      });
    };

    if (this.professionals().length === 0) {
      this.isLoading.set(true);
      this.appointmentService.getProfessionals().subscribe({
        next: (professionals) => {
          this.professionals.set(professionals);
          loadAndSelect();
        },
        error: () => {
          this.errorMessage.set('Erro ao carregar profissionais.');
          this.isLoading.set(false);
        }
      });
    } else {
      loadAndSelect();
    }
  }

  cancelAppointment(appointment: Appointment): void {
    if (!confirm('Tem certeza que deseja cancelar este agendamento?')) return;

    this.isLoading.set(true);
    this.clearMessages();
    this.appointmentService.cancelAppointment(appointment.id).subscribe({
      next: () => {
        this.handleSuccess('Agendamento cancelado com sucesso!');
      },
      error: (err) => this.handleError(err)
    });
  }

  private handleSuccess(message: string): void {
    this.successMessage.set(message);
    this.errorMessage.set(null);
    this.showModal.set(false);
    this.selectedAvailability.set(null);
    this.editingAppointment.set(null);
    this.loadAppointments();
    this.setView('appointments');
  }

  private handleError(err: any): void {
    this.isLoading.set(false);
    this.successMessage.set(null);
    this.errorMessage.set(err?.error || err?.message || 'Erro ao processar agendamento.');
  }

  private clearMessages(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  formatDate(dateStr: string): string {
    const [year, month, day] = dateStr.split('-').map(Number);
    return new Date(year, month - 1, day).toLocaleDateString('pt-BR');
  }

  logout(): void {
    this.authService.logout();
  }
}
