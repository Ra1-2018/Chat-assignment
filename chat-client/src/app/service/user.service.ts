import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  isSignedIn = false;

  constructor() { }

  signIn() {
    this.isSignedIn = true;
  }
  
  signOut() {
    this.isSignedIn = false;
  }
}
