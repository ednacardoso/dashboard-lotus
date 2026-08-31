import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-professional-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './professional-dashboard.component.html',
  styleUrl: './professional-dashboard.component.scss'
})
export class ProfessionalDashboardComponent implements OnInit {
  userEmail = signal<string>('');
  userName = signal<string>('');

  constructor(
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.http.get<{ email: string; name: string }>('/api/auth/me').subscribe({
      next: (user) => {
        this.userEmail.set(user.email);
        this.userName.set(user.name);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
