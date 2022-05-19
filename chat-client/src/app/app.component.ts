import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'chat-client';
}

/*function initSocket(userService: UserService, router: Router, toastr: ToastrService) {
  let connection: WebSocket|null = new WebSocket("ws://localhost:8080/Chat-war/ws/chat");
  connection.onopen = function() {
    console.log("Socket is open");
  }

  connection.onclose = function () {
    userService.signOut();
    connection = null;
  }

  connection.onmessage = function (msg) {
    const data = msg.data.split("!");
    if(data[0] === "LOGGEDIN") {
        let users:User[] = [];
        data[1].split("|").forEach((user: string) => {
            if (user) {
                let userData = user.split(",");
                users.push(new User(userData[0], userData[1]));
            }
        });
        userService.loggedUsers = users;
    }
    else if(data[0] === "REGISTERED") {
      let users:User[] = [];
      data[1].split("|").forEach((user: string) => {
          if (user) {
              let userData = user.split(",");
              users.push(new User(userData[0], userData[1]));
          }
      });
      userService.registeredUsers = users;
    }
    else if(data[0] == "LOG_IN" && data[1].includes("Yes")) {
      userService.isSignedIn = true;
      let username = data[2];
      sessionStorage.setItem("user", username);
      router.navigate(['signed-in-users']);
      initUserSocket(username, toastr);
    }
    else if(data[0] == "REGISTER" && data[1].includes("Yes")) {
      toastr.success(data[1]);
    }
    else {
      toastr.info(data[1]);
    }
  }
}

function initUserSocket(username: string, toastr: ToastrService) {
  let connection: WebSocket|null = new WebSocket("ws://localhost:8080/Chat-war/ws/" + username);
  connection.onopen = function() {
    console.log("User socket is open");
  }

  connection.onclose = function () {
    connection = null;
  }

  connection.onmessage = function (msg) {
    const data = msg.data.split("!");
    toastr.info(data[1]);
  }
}*/