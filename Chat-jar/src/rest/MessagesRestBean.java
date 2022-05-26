package rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import chatmanager.ChatManagerRemote;
import messagemanager.AgentMessage;
import messagemanager.MessageManagerRemote;
import models.Message;
import models.User;

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
		User user = chatManager.getByUsername(message.getReceiver().getUsername());
		if(user.getHost().getAlias().equals(getNodeAlias() + ":8080")) {
			AgentMessage amsg = new AgentMessage();
			amsg.userArgs.put("command", "MESSAGE");
			amsg.userArgs.put("receiver", message.getReceiver().getUsername());
			amsg.userArgs.put("sender", message.getSender().getUsername());
			amsg.userArgs.put("subject", message.getSubject());
			amsg.userArgs.put("content", message.getContent());
			messageManager.post(amsg);
		}
		else {
			System.out.println("Sending message to node: " + user.getHost().getAlias());
			ResteasyClient resteasyClient = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = resteasyClient.target("http://" + user.getHost().getAlias() + "/Chat-war/api/messages");
			MessagesRest rest = rtarget.proxy(MessagesRest.class);
			rest.messageUser(message);
			resteasyClient.close();
		}
	}

	@Override
	public void getUserMessages(String username) {
		AgentMessage amsg = new AgentMessage();
		amsg.userArgs.put("command", "GET_MESSAGES");
		amsg.userArgs.put("receiver", username);
		messageManager.post(amsg);
	}

	private String getNodeAlias() {		
		return System.getProperty("jboss.node.name");
	}
}
