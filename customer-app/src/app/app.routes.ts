import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';
import { Dashboard } from './pages/dashboard/dashboard';
import { Login } from './pages/login/login';
import { MyRequests } from './pages/my-requests/my-requests';
import { Register } from './pages/register/register';
import { RequestServicePage } from './pages/request-service/request-service';
import { LandingPage } from './pages/landing/landing';
import { ProfilePage } from './pages/profile/profile';
import { ForgotPasswordPage } from './pages/forgot-password/forgot-password';
import { ResetPasswordPage } from './pages/reset-password/reset-password';

export const routes: Routes = [
  { path: '', component: LandingPage, pathMatch: 'full', canActivate: [guestGuard] },
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: Register, canActivate: [guestGuard] },
  { path: 'forgot-password', component: ForgotPasswordPage, canActivate: [guestGuard] },
  { path: 'reset-password', component: ResetPasswordPage, canActivate: [guestGuard] },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'request-service', component: RequestServicePage, canActivate: [authGuard] },
  { path: 'my-requests', component: MyRequests, canActivate: [authGuard] },
  { path: 'track/:requestId', loadComponent: () => import('./pages/tracking/tracking').then((m) => m.TrackingPage), canActivate: [authGuard] },
  { path: 'profile', component: ProfilePage, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];