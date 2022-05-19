package rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

import agentmanager.AgentManagerRemote;
import chatmanager.ChatManagerRemote;
import messagemanager.AgentMessage;
import messagemanager.MessageManagerRemote;
import models.Message;
import models.User;
import util.JNDILookup;

@Stateless
@Path("/messages")
public class MessagesRestBean implements MessagesRest {

	@EJB
	private MessageManagerRemote messageManager;
	@EJB
	private ChatManagerRemote chatManager;
	
	@Override
	public void messageAll(Message message) {
		for(User user : chatManager.loggedInUsers()) {
			AgentMessage amsg = new AgentMessage();
			amsg.userArgs.put("command", "MESSAGE");
			amsg.userArgs.put("receiver", user.getUsername());
			amsg.userArgs.put("sender", message.getSender().getUsername());
			amsg.userArgs.put("subject", message.getSubject());
			amsg.userArgs.put("content", message.getContent());
			messageManager.post(amsg);
		}
	}

	@Override
	public void messageUser(Message message) {
		AgentMessage amsg = new AgentMessage();
		amsg.userArgs.put("command", "MESSAGE");
		amsg.userArgs.put("receiver", message.getReceiver().getUsername());
		amsg.userArgs.put("sender", message.getSender().getUsername());
		amsg.userArgs.put("subject", message.getSubject());
		amsg.userArgs.put("content", message.getContent());
		messageManager.post(amsg);
	}

	@Override
	public void getUserMessages(String username) {
		AgentMessage amsg = new AgentMessage();
		amsg.userArgs.put("command", "GET_MESSAGES");
		amsg.userArgs.put("receiver", username);
		messageManager.post(amsg);
	}

}
