import { Component, inject, OnInit, OnDestroy, AfterViewInit, ElementRef, ViewChildren, QueryList } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil, take } from 'rxjs';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { ProductService } from '../../services/product/product.service';
import { OrderService } from '../../services/order/order.service';
import { Product } from '../../model/product';
import { Order } from '../../model/order';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';

interface Toast {
  id: number;
  type: 'success' | 'error' | 'info';
  title: string;
  message: string;
  visible: boolean;
}

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChildren('scrollEl') scrollEls!: QueryList<ElementRef>;

  private readonly productService = inject(ProductService);
  private readonly orderService = inject(OrderService);
  private readonly oidcSecurityService = inject(OidcSecurityService);
  private readonly router = inject(Router);
  private readonly destroy$ = new Subject<void>();

  products: Product[] = [];
  isAuthenticated = false;
  searchTerm = '';
  toasts: Toast[] = [];
  quantityMap: Record<string, number> = {};
  orderLoadingMap: Record<string, boolean> = {};
  private toastId = 0;
  private observer!: IntersectionObserver;

  get filteredProducts(): Product[] {
    if (!this.searchTerm.trim()) return this.products;
    const term = this.searchTerm.toLowerCase();
    return this.products.filter(p =>
      p.name.toLowerCase().includes(term) ||
      (p.skuCode || '').toLowerCase().includes(term) ||
      p.description.toLowerCase().includes(term)
    );
  }

  ngOnInit(): void {
    this.oidcSecurityService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ isAuthenticated }) => {
        this.isAuthenticated = isAuthenticated;
      });
    this.loadProducts();
  }

  ngAfterViewInit(): void {
    this.observer = new IntersectionObserver(
      (entries) => entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('revealed'); }),
      { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
    );
    setTimeout(() => this.observeElements(), 200);
    this.scrollEls.changes.subscribe(() => this.observeElements());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.observer?.disconnect();
  }

  private observeElements(): void {
    document.querySelectorAll('.scroll-reveal').forEach(el => this.observer.observe(el));
  }

  loadProducts(): void {
    this.productService.getProducts()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: products => { this.products = products; },
        error: () => this.showToast('error', 'Load Failed', 'Could not fetch products. Check API gateway.')
      });
  }

  login(): void {
    this.oidcSecurityService.authorize();
  }

  goToCreateProductPage(): void {
    if (!this.isAuthenticated) {
      this.login();
      return;
    }
    this.router.navigateByUrl('/add-product');
  }

  getQuantity(product: Product): number {
    return this.quantityMap[product.id || ''] || 1;
  }

  incrementQty(product: Product): void {
    const key = product.id || '';
    this.quantityMap[key] = (this.quantityMap[key] || 1) + 1;
  }

  decrementQty(product: Product): void {
    const key = product.id || '';
    const cur = this.quantityMap[key] || 1;
    if (cur > 1) this.quantityMap[key] = cur - 1;
  }

  orderProduct(product: Product): void {
    if (!this.isAuthenticated) {
      this.login();
      return;
    }

    const key = product.id || '';
    this.orderLoadingMap[key] = true;

    this.oidcSecurityService.userData$
      .pipe(take(1))
      .subscribe(result => {
        const qty = this.getQuantity(product);
        const order: Order = {
          skuCode: product.skuCode,
          price: product.price,
          quantity: qty,
          userDetails: {
            email: result.userData?.email || 'admin@example.com',
            firstName: result.userData?.given_name || result.userData?.firstName || 'Admin',
            lastName: result.userData?.family_name || result.userData?.lastName || 'User'
          }
        };

        this.orderService.orderProduct(order)
          .pipe(take(1))
          .subscribe({
            next: (res) => {
              this.orderLoadingMap[key] = false;
              this.quantityMap[key] = 1;
              this.showToast('success', 'Order Placed!',
                `${qty}x ${product.name} ordered successfully! Kafka event dispatched.`);
            },
            error: (err) => {
              this.orderLoadingMap[key] = false;
              let msg = 'Product may be out of stock or service unavailable.';
              if (err?.error?.detail) {
                msg = err.error.detail;
              } else if (err?.error?.message) {
                msg = err.error.message;
              } else if (typeof err?.error === 'string') {
                try {
                  const parsed = JSON.parse(err.error);
                  msg = parsed.detail || parsed.message || err.error;
                } catch {
                  msg = err.error;
                }
              } else if (err?.message) {
                msg = err.message;
              }
              this.showToast('error', 'Order Failed', msg);
            }
          });
      });
  }

  onCardMouseMove(event: MouseEvent, card: HTMLElement): void {
    const rect = card.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const rotateX = ((y - rect.height / 2) / rect.height) * -10;
    const rotateY = ((x - rect.width / 2) / rect.width) * 10;
    card.style.transform = `perspective(800px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.03, 1.03, 1.03)`;
    const shine = card.querySelector('.card-shine') as HTMLElement;
    if (shine) { shine.style.left = `${x - 100}px`; shine.style.top = `${y - 100}px`; shine.style.opacity = '1'; }
  }

  onCardMouseLeave(card: HTMLElement): void {
    card.style.transform = 'perspective(800px) rotateX(0) rotateY(0) scale3d(1, 1, 1)';
    const shine = card.querySelector('.card-shine') as HTMLElement;
    if (shine) shine.style.opacity = '0';
  }

  showToast(type: Toast['type'], title: string, message: string): void {
    const toast: Toast = { id: ++this.toastId, type, title, message, visible: true };
    this.toasts.push(toast);
    setTimeout(() => this.removeToast(toast.id), 5000);
  }

  removeToast(id: number): void {
    const toast = this.toasts.find(t => t.id === id);
    if (toast) toast.visible = false;
    setTimeout(() => { this.toasts = this.toasts.filter(t => t.id !== id); }, 350);
  }
}
