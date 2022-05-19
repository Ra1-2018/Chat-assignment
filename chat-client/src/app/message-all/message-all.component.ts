import { Component, OnInit } from '@angular/core';
import { Message } from '../model/message';
import { User } from '../model/user';
import { MessageService } from '../service/message.service';
import { UserService } from '../service/user.service';

@Component({
  selector: 'app-message-all',
  templateUrl: './message-all.component.html',
  styleUrls: ['./message-all.component.css']
})
export class MessageAllComponent implements OnInit {

  message = new Message(null, null, null, '', '');

  constructor(private messageService: MessageService, private userService: UserService) { }

  ngOnInit(): void {
  }

  onSubmit() {
    console.log(this.message);
    this.message.sender = this.userService.user;
    this.messageService.messageAll(this.message).subscribe();
  }
}
