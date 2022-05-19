import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { User } from '../model/user';

const baseUrl = 'http://localhost:8080/Chat-war/api/users/';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  isSignedIn = false;
  loggedUsers: User[] = [];
  registeredUsers: User[] = [];
  user: User = new User('', '');

  constructor(private http: HttpClient, private toastr: ToastrService) { }

  signIn(user: User) {
    return this.http.post(baseUrl + 'login', user).subscribe({
      next: (user) => {
        this.user = user as User;
        this.isSignedIn = true;
      },
      error: () => (this.toastr.error("Invalid username/password"))
    });
  }
  
  register(user:  User) {
    return this.http.post(baseUrl + 'register', user).subscribe({
      next: () => (this.toastr.success("Successfully registered")),
      error: () => (this.toastr.error("Username taken"))
    });
  }

  signOut() {
    return this.http.delete(baseUrl + 'loggedIn/' + this.user.username).subscribe({
      next: () => {
        this.isSignedIn = false;
        this.user = new User('', '');
      }
    });
  }

  getLoggedUsers() {
    return this.http.get(baseUrl + 'loggedIn').subscribe({
      next: (users) => (this.loggedUsers = users as User[])
    });
  }

  getRegisteredUsers() {
    return this.http.get(baseUrl + 'registered').subscribe({
      next: (users) => (this.registeredUsers = users as User[])
    });
  }
}
