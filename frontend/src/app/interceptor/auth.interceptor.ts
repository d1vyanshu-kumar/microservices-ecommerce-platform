import {HttpInterceptorFn} from "@angular/common/http";
import {inject} from "@angular/core";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {switchMap, take} from "rxjs";

/**
 * Functional HTTP interceptor that attaches the OAuth2 Bearer token
 * to outgoing requests. Uses take(1) to avoid keeping the
 * token stream open and switchMap to attach the Authorization header.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const oidcSecurityService = inject(OidcSecurityService);

  return oidcSecurityService.getAccessToken().pipe(
    take(1),
    switchMap(token => {
      if (token) {
        const authenticatedRequest = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        });
        return next(authenticatedRequest);
      }
      return next(req);
    })
  );
};
