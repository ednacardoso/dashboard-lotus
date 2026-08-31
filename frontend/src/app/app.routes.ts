import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ClientDashboardComponent } from './client-dashboard/client-dashboard.component';
import { ProfessionalDashboardComponent } from './professional-dashboard/professional-dashboard.component';
import { authGuard } from './auth/auth.guard';
import { loginGuard } from './auth/login.guard';
import { roleGuard } from './auth/role.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [roleGuard('ADMIN')] },
  { path: 'cliente', component: ClientDashboardComponent, canActivate: [roleGuard('CLIENT')] },
  { path: 'profissional', component: ProfessionalDashboardComponent, canActivate: [roleGuard('PROFESSIONAL')] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
