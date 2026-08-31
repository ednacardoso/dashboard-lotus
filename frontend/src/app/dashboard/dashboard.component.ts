import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  userEmail = signal<string>('');
  userName = signal<string>('');

  constructor(
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.http.get<{ email: string }>('/api/auth/me').subscribe({
      next: (user) => {
        this.userEmail.set(user.email);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
