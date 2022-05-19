import { Component, OnInit } from '@angular/core';
import { Message } from '../model/message';
import { User } from '../model/user';
import { MessageService } from '../service/message.service';
import { UserService } from '../service/user.service';

@Component({
  selector: 'app-send-message',
  templateUrl: './send-message.component.html',
  styleUrls: ['./send-message.component.css']
})
export class SendMessageComponent implements OnInit {

  message = new Message(null, null, null, '', '');

  constructor(public userService: UserService,
              private messageService: MessageService) { }

  ngOnInit(): void {
    this.getRegisteredUsers();
  }

  getRegisteredUsers() {
    //this.userService.getRegisteredUsers().subscribe();
  }

  onSubmit() {
    console.log(this.message);
    this.message.sender = new User(sessionStorage.getItem("user") as string, '');
    this.messageService.messageUser(this.message).subscribe();
  }
}
