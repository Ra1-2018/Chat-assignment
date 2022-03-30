import { Component, OnInit } from '@angular/core';
import { User } from '../model/user';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.css']
})
export class SignInComponent implements OnInit {

  model = new User('', '');

  constructor() { }

  ngOnInit(): void {
  }

  onSubmit() {
    console.log(this.model);
  }
}
