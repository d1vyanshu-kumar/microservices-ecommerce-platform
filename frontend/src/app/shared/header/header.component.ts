import { Component, inject, OnInit } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  private readonly oidcSecurityService = inject(OidcSecurityService);
  isAuthenticated = false;
  username = '';
  mobileMenuOpen = false;

  ngOnInit(): void {
    this.oidcSecurityService.isAuthenticated$.subscribe(
      ({ isAuthenticated }) => { this.isAuthenticated = isAuthenticated; }
    );
    this.oidcSecurityService.userData$.subscribe(
      ({ userData }) => {
        this.username = userData?.preferred_username ?? '';
      }
    );
  }

  login(): void { this.oidcSecurityService.authorize(); }

  logout(): void {
    this.oidcSecurityService.logoff().subscribe(r => console.log('logged out', r));
  }

  toggleMobileMenu(): void { this.mobileMenuOpen = !this.mobileMenuOpen; }
}
