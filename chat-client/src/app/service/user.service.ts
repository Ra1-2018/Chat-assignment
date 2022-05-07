import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { User } from '../model/user';

const baseUrl = 'http://localhost:8080/Chat-war/api/chat/';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  isSignedIn = false;
  loggedUsers: User[] = [];

  constructor(private http: HttpClient) { }

  signIn(user: User) {
    return this.http.post(baseUrl + 'login', user);
  }
  
  register(user:  User) {
    return this.http.post(baseUrl + 'register', user);
  }

  signOut() {
    this.isSignedIn = false;
    return this.http.delete(baseUrl + 'loggedIn/' + sessionStorage.getItem("user"));
  }

  getLoggedUsers() {
    return this.http.get(baseUrl + 'loggedIn');
  }
}
