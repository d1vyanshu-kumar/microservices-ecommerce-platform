import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Product } from '../../model/product';
import { ProductService } from '../../services/product/product.service';
import { OidcSecurityService } from 'angular-auth-oidc-client';

interface Preset {
  name: string;
  skuCode: string;
  description: string;
  price: number;
  icon: string;
}

@Component({
  selector: 'app-add-product',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './add-product.component.html',
  styleUrl: './add-product.component.css'
})
export class AddProductComponent implements OnInit {
  addProductForm: FormGroup;
  private readonly productService = inject(ProductService);
  private readonly oidcSecurityService = inject(OidcSecurityService);
  private readonly router = inject(Router);

  isAuthenticated = false;
  productCreated = false;
  createdProductName = '';
  errorMessage = '';
  isSubmitting = false;

  presets: Preset[] = [
    { name: 'iPhone 15 Pro', skuCode: 'iphone_15', description: 'Apple iPhone 15 Pro with A17 Pro chip, titanium design, and 48MP camera system.', price: 1299.99, icon: '📱' },
    { name: 'Google Pixel 8', skuCode: 'pixel_8', description: 'Google Pixel 8 with Tensor G3 chip, AI-powered camera, and 7 years of updates.', price: 699.99, icon: '📷' },
    { name: 'Samsung Galaxy S24', skuCode: 'galaxy_24', description: 'Samsung Galaxy S24 Ultra with S Pen, 200MP camera, and Galaxy AI features.', price: 1199.99, icon: '🌌' },
    { name: 'OnePlus 12', skuCode: 'oneplus_12', description: 'OnePlus 12 with Snapdragon 8 Gen 3, Hasselblad camera, and 100W fast charging.', price: 799.99, icon: '⚡' },
  ];

  constructor(private fb: FormBuilder) {
    this.addProductForm = this.fb.group({
      skuCode: ['', [Validators.required]],
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      price: [0, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;
    });
  }

  login(): void {
    this.oidcSecurityService.authorize();
  }

  fillPreset(preset: Preset): void {
    this.addProductForm.patchValue({
      skuCode: preset.skuCode,
      name: preset.name,
      description: preset.description,
      price: preset.price
    });
    this.addProductForm.markAllAsTouched();
    this.productCreated = false;
    this.errorMessage = '';
  }

  onSubmit(): void {
    if (!this.isAuthenticated) {
      this.errorMessage = 'You must be signed in to create products.';
      return;
    }

    if (this.addProductForm.valid) {
      this.isSubmitting = true;
      this.errorMessage = '';
      const name = this.addProductForm.get('name')?.value;
      const product: Product = {
        skuCode: this.addProductForm.get('skuCode')?.value,
        name: name,
        description: this.addProductForm.get('description')?.value,
        price: this.addProductForm.get('price')?.value
      };

      this.productService.createProduct(product).subscribe({
        next: () => {
          this.productCreated = true;
          this.createdProductName = name;
          this.isSubmitting = false;
          this.addProductForm.reset();
        },
        error: (err) => {
          this.isSubmitting = false;
          if (err?.status === 401) {
            this.errorMessage = 'Authentication expired or invalid. Please sign in again.';
          } else {
            this.errorMessage = err?.error?.message || 'Failed to create product. Ensure API Gateway is running.';
          }
        }
      });
    }
  }

  goBack(): void { this.router.navigateByUrl('/'); }

  get skuCode() { return this.addProductForm.get('skuCode'); }
  get name() { return this.addProductForm.get('name'); }
  get description() { return this.addProductForm.get('description'); }
  get price() { return this.addProductForm.get('price'); }
}
