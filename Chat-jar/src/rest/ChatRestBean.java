package rest;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import agentmanager.AgentManagerRemote;
import chatmanager.ChatManagerRemote;
import messagemanager.AgentMessage;
import messagemanager.MessageManagerRemote;
import models.User;
import util.JNDILookup;

@Stateless
@LocalBean
@Path("/users")
public class ChatRestBean implements ChatRest, ChatRestLocal {

	@EJB
	private MessageManagerRemote messageManager;
	
	@EJB
	private ChatManagerRemote chatManager;
	
	@EJB
	private AgentManagerRemote agentManager;
	
	@Override
	public Response register(User user) {
		if(!chatManager.register(user)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		for(User u : chatManager.loggedInUsers()) {
			AgentMessage message = new AgentMessage();
			message.userArgs.put("receiver", u.getUsername());
			message.userArgs.put("command", "GET_REGISTERED");
			
			messageManager.post(message);
		}
		return Response.status(Response.Status.CREATED).entity(user).build();
	}

	@Override
	public Response login(User user) {
		if(!chatManager.login(user)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		agentManager.startAgent(JNDILookup.UserAgentLookup, user.getUsername());
		for(User u : chatManager.loggedInUsers()) {
			AgentMessage message = new AgentMessage();
			message.userArgs.put("receiver", u.getUsername());
			message.userArgs.put("command", "GET_LOGGEDIN");
			
			messageManager.post(message);
		}
		return Response.status(Response.Status.OK).entity(user).build();
	}

	@Override
	public void getloggedInUsers(String username) {
		AgentMessage message = new AgentMessage();
		message.userArgs.put("receiver", username);
		message.userArgs.put("command", "GET_LOGGEDIN");
		
		messageManager.post(message);
	}

	@Override
	public Response logout(String username) {
		if(!chatManager.logout(username)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		agentManager.stopAgent(username);
		for(User u : chatManager.loggedInUsers()) {
			AgentMessage message = new AgentMessage();
			message.userArgs.put("receiver", u.getUsername());
			message.userArgs.put("command", "GET_LOGGEDIN");
			
			messageManager.post(message);
		}
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public void getRegisteredUsers(String username) {
		AgentMessage message = new AgentMessage();
		message.userArgs.put("receiver", username);
		message.userArgs.put("command", "GET_REGISTERED");
		
		messageManager.post(message);
	}
	
}
