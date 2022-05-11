import { User } from "./user";

export class Message {
    constructor(
        public receiver: User|null,
        public sender: User|null,
        public dateCreated: Date|null,
        public subject: string,
        public content: string
    ) {}
}