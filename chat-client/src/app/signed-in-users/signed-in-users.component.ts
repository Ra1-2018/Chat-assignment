import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { User } from '../model/user';
import { UserService } from '../service/user.service';

@Component({
  selector: 'app-signed-in-users',
  templateUrl: './signed-in-users.component.html',
  styleUrls: ['./signed-in-users.component.css']
})
export class SignedInUsersComponent implements OnInit {
  
  constructor(public userService: UserService,) { }

  ngOnInit(): void {
    this.getLoggedUsers();
  }

  getLoggedUsers() {
    this.userService.getLoggedUsers();
  }

  sortData(sort: Sort) {
    const data = this.userService.loggedUsers.slice();
    if (!sort.active || sort.direction === '') {
      this.userService.loggedUsers = data;
      return;
    }

    this.userService.loggedUsers = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case 'username': return compare(a.username, b.username, isAsc);
        default: return 0;
      }
    });
  }
}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}

