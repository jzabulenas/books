import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { guestGuard } from './core/guest.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./login/login.component').then((m) => m.LoginComponent),
    canActivate: [guestGuard],
  },
  // Activates when `return '/index.html';` gets called in `proxy.conf.js`
  {
    path: 'login/ott',
    loadComponent: () => import('./login/login-ott.component').then((m) => m.LoginOttComponent),
  },
  {
    path: 'app',
    loadComponent: () => import('./shell/shell.component').then((m) => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'books',
        loadComponent: () =>
          import('./book/page/books-page.component').then((m) => m.BooksPageComponent),
      },
      {
        path: 'recommendations',
        loadComponent: () =>
          import('./recommendation/recommendations-page.component').then(
            (m) => m.RecommendationsPageComponent,
          ),
      },
      {
        path: 'saved',
        loadComponent: () =>
          import('./saved/saved-books-page.component').then((m) => m.SavedBooksPageComponent),
      },
      { path: '', redirectTo: 'books', pathMatch: 'full' },
      { path: '**', redirectTo: 'books' },
    ],
  },
  { path: '', redirectTo: '/app/books', pathMatch: 'full' },
];
