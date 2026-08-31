import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';

export interface AuthResponse {
  token: string;
  email: string;
  name: string;
  role: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

const TOKEN_KEY = 'auth_token';
const ROLE_KEY = 'auth_role';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = '/api/auth';

  private token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private userRole = signal<string | null>(localStorage.getItem(ROLE_KEY));
  isAuthenticated = computed(() => !!this.token());

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => this.setSession(response)),
      catchError((error) => throwError(() => this.extractError(error)))
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
      tap((response) => this.setSession(response)),
      catchError((error) => throwError(() => this.extractError(error)))
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    this.token.set(null);
    this.userRole.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.token();
  }

  getRole(): string | null {
    return this.userRole();
  }

  hasRole(role: string): boolean {
    return this.userRole() === role;
  }

  getDashboardRoute(): string {
    const role = this.userRole();
    switch (role) {
      case 'ADMIN':
        return '/dashboard';
      case 'CLIENT':
        return '/cliente';
      case 'PROFESSIONAL':
        return '/profissional';
      default:
        return '/login';
    }
  }

  private setSession(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(ROLE_KEY, response.role);
    this.token.set(response.token);
    this.userRole.set(response.role);
  }

  private extractError(error: any): string {
    if (error?.error?.message) return error.error.message;
    if (error?.error) return error.error;
    return error?.message || 'Erro inesperado. Tente novamente.';
  }
}
