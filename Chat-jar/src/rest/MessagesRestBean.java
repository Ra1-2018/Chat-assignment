package rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

import agentmanager.AgentManagerRemote;
import messagemanager.AgentMessage;
import messagemanager.MessageManagerRemote;
import models.Message;
import util.JNDILookup;

@Stateless
@Path("/messages")
public class MessagesRestBean implements MessagesRest {

	@EJB
	private MessageManagerRemote messageManager;
	
	@Override
	public void messageAll(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageUser(Message message) {
		AgentMessage amsg = new AgentMessage();
		amsg.userArgs.put("receiver", message.getReceiver().getUsername());
		amsg.userArgs.put("sender", message.getSender().getUsername());
		amsg.userArgs.put("subject", message.getSubject());
		amsg.userArgs.put("content", message.getContent());
		messageManager.post(amsg);
	}

	@Override
	public void getUserMessages(String username) {
		// TODO Auto-generated method stub
		
	}

}
