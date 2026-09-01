import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth/auth.service';
import { AdminService } from '../admin/admin.service';
import { AdminUser, AdminAppointment, Room, RoomRental, RoomOccupancy, CreateUserResponse } from '../admin/admin.model';

type AdminSection = 'overview' | 'professionals' | 'clients' | 'appointments' | 'rooms';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  userEmail = signal<string>('');
  userName = signal<string>('');

  currentSection = signal<AdminSection>('overview');

  professionals = signal<AdminUser[]>([]);
  clients = signal<AdminUser[]>([]);
  appointments = signal<AdminAppointment[]>([]);
  rooms = signal<Room[]>([]);
  roomRentals = signal<RoomRental[]>([]);
  occupiedRooms = signal<RoomOccupancy[]>([]);
  vacantRooms = signal<Room[]>([]);

  selectedYearMonth = signal<string>(this.currentYearMonth());

  loading = signal<boolean>(false);
  errorMessage = signal<string>('');
  successMessage = signal<string>('');

  createdUser = signal<CreateUserResponse | null>(null);

  professionalForm: FormGroup;
  clientForm: FormGroup;
  roomForm: FormGroup;
  rentalForm: FormGroup;

  showProfessionalPassword = signal<boolean>(false);
  showClientPassword = signal<boolean>(false);
  showGeneratedPassword = signal<boolean>(false);

  private readonly strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()\-_+=<>?]).{8,}$/;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.professionalForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      specialty: ['', Validators.required],
      definePassword: [false],
      password: ['', [Validators.required, Validators.pattern(this.strongPasswordRegex)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });

    this.clientForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      definePassword: [false],
      password: ['', [Validators.required, Validators.pattern(this.strongPasswordRegex)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });

    this.roomForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      capacity: [null],
      monthlyPrice: [0, [Validators.required, Validators.min(0)]]
    });

    this.rentalForm = this.fb.group({
      professionalId: [null, Validators.required],
      roomId: [null, Validators.required],
      yearMonth: [this.currentYearMonth(), Validators.required]
    });

    this.updatePasswordValidators(this.professionalForm, false);
    this.updatePasswordValidators(this.clientForm, false);
  }

  ngOnInit(): void {
    this.http.get<{ email: string; name: string }>('/api/auth/me').subscribe({
      next: (user) => {
        this.userEmail.set(user.email);
        this.userName.set(user.name);
      }
    });

    this.loadOverview();
  }

  private currentYearMonth(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
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

  setSection(section: AdminSection): void {
    this.currentSection.set(section);
    this.clearMessages();
    this.createdUser.set(null);

    switch (section) {
      case 'overview':
        this.loadOverview();
        break;
      case 'professionals':
        this.loadProfessionals();
        break;
      case 'clients':
        this.loadClients();
        break;
      case 'appointments':
        this.loadAppointments();
        break;
      case 'rooms':
        this.loadRooms();
        this.loadRoomData();
        break;
    }
  }

  private loadOverview(): void {
    this.loading.set(true);
    this.adminService.getProfessionals().subscribe({
      next: (data) => this.professionals.set(data),
      error: (err) => this.handleError(err)
    });
    this.adminService.getClients().subscribe({
      next: (data) => this.clients.set(data),
      error: (err) => this.handleError(err)
    });
    this.adminService.getAppointments().subscribe({
      next: (data) => this.appointments.set(data),
      error: (err) => this.handleError(err)
    });
    this.adminService.getRooms().subscribe({
      next: (data) => {
        this.rooms.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.handleError(err);
        this.loading.set(false);
      }
    });
  }

  private loadProfessionals(): void {
    this.loading.set(true);
    this.adminService.getProfessionals().subscribe({
      next: (data) => {
        this.professionals.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.handleError(err);
        this.loading.set(false);
      }
    });
  }

  private loadClients(): void {
    this.loading.set(true);
    this.adminService.getClients().subscribe({
      next: (data) => {
        this.clients.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.handleError(err);
        this.loading.set(false);
      }
    });
  }

  private loadAppointments(): void {
    this.loading.set(true);
    this.adminService.getAppointments().subscribe({
      next: (data) => {
        this.appointments.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.handleError(err);
        this.loading.set(false);
      }
    });
  }

  private loadRooms(): void {
    this.loading.set(true);
    this.adminService.getRooms().subscribe({
      next: (data) => {
        this.rooms.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.handleError(err);
        this.loading.set(false);
      }
    });
  }

  loadRoomData(): void {
    const yearMonth = this.selectedYearMonth();
    this.adminService.getRoomRentals(yearMonth).subscribe({
      next: (data) => this.roomRentals.set(data)
    });
    this.adminService.getOccupiedRooms(yearMonth).subscribe({
      next: (data) => this.occupiedRooms.set(data)
    });
    this.adminService.getVacantRooms(yearMonth).subscribe({
      next: (data) => this.vacantRooms.set(data)
    });
  }

  onYearMonthChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.selectedYearMonth.set(target.value);
    this.loadRoomData();
  }

  onDefineProfessionalPasswordChange(): void {
    const definePassword = this.professionalForm.get('definePassword')?.value;
    this.updatePasswordValidators(this.professionalForm, definePassword);
  }

  onDefineClientPasswordChange(): void {
    const definePassword = this.clientForm.get('definePassword')?.value;
    this.updatePasswordValidators(this.clientForm, definePassword);
  }

  createProfessional(): void {
    if (this.professionalForm.invalid) {
      return;
    }

    const definePassword = this.professionalForm.value.definePassword;
    const request = {
      name: this.professionalForm.value.name,
      email: this.professionalForm.value.email,
      specialty: this.professionalForm.value.specialty,
      ...(definePassword ? { password: this.professionalForm.value.password } : {})
    };

    this.adminService.createProfessional(request).subscribe({
      next: (response) => {
        this.createdUser.set(response);
        this.successMessage.set('Profissional cadastrado com sucesso.');
        this.professionalForm.reset({ definePassword: false });
        this.updatePasswordValidators(this.professionalForm, false);
        this.loadProfessionals();
      },
      error: (err) => this.handleError(err)
    });
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

    this.adminService.createClient(request).subscribe({
      next: (response) => {
        this.createdUser.set(response);
        this.successMessage.set('Cliente cadastrado com sucesso.');
        this.clientForm.reset({ definePassword: false });
        this.updatePasswordValidators(this.clientForm, false);
        this.loadClients();
      },
      error: (err) => this.handleError(err)
    });
  }

  createRoom(): void {
    if (this.roomForm.invalid) {
      return;
    }

    this.adminService.createRoom(this.roomForm.value).subscribe({
      next: () => {
        this.successMessage.set('Sala cadastrada com sucesso.');
        this.roomForm.reset({ monthlyPrice: 0 });
        this.loadRooms();
      },
      error: (err) => this.handleError(err)
    });
  }

  toggleRoom(id: number): void {
    this.adminService.toggleRoomActive(id).subscribe({
      next: () => this.loadRooms(),
      error: (err) => this.handleError(err)
    });
  }

  rentRoom(): void {
    if (this.rentalForm.invalid) {
      return;
    }

    this.adminService.rentRoom(this.rentalForm.value).subscribe({
      next: () => {
        this.successMessage.set('Sala alocada com sucesso.');
        this.loadRoomData();
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
