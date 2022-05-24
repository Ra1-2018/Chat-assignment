import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { UserService } from "../service/user.service";

@Injectable()
export class Interceptor implements HttpInterceptor {
  constructor(private userService: UserService) { }
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let username = this.userService.user.username;
    if (username.length > 0) {
      request = request.clone({
        setHeaders: {
          Authorization: `${username}` 
        }
      });
    }
    return next.handle(request);
  }
}