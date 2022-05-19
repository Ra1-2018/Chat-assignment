import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Message } from '../model/message';
import { User } from '../model/user';

const baseUrl = 'http://localhost:8080/Chat-war/api/messages/';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  messages: Message[] = []

  constructor(private http: HttpClient) { }

  messageUser(message: Message) {
    return this.http.post(baseUrl + 'user', message);
  }

  messageAll(message: Message) {
    return this.http.post(baseUrl + 'all', message);
  }

  getMessages(user: User) {
    return this.http.get(baseUrl + user.username);
  }
}
