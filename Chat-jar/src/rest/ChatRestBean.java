package rest;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import chatmanager.ChatManagerRemote;
import messagemanager.MessageManagerRemote;
import models.User;

@Stateless
@LocalBean
@Path("/users")
public class ChatRestBean implements ChatRest, ChatRestLocal {

	@EJB
	private MessageManagerRemote messageManager;
	
	@EJB
	private ChatManagerRemote chatManager;
	
	@Override
	public Response register(User user) {
		/*AgentMessage message = new AgentMessage();
		message.userArgs.put("receiver", "chat");
		message.userArgs.put("command", "REGISTER");
		message.userArgs.put("username", user.getUsername());
		message.userArgs.put("password", user.getPassword());
		
		messageManager.post(message);*/
		if(!chatManager.register(user)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		return Response.status(Response.Status.CREATED).entity(user).build();
	}

	@Override
	public Response login(User user) {
		/*AgentMessage message = new AgentMessage();
		message.userArgs.put("receiver", "chat");
		message.userArgs.put("command", "LOG_IN");
		message.userArgs.put("username", user.getUsername());
		message.userArgs.put("password", user.getPassword());
		
		messageManager.post(message);*/
		if(!chatManager.login(user)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		return Response.status(Response.Status.OK).entity(user).build();
	}

	@Override
	public Response getloggedInUsers() {
		/*AgentMessage message = new AgentMessage();
		message.userArgs.put("receiver", "chat");
		message.userArgs.put("command", "GET_LOGGEDIN");
		
		messageManager.post(message);*/
		return Response.status(Response.Status.OK).entity(chatManager.loggedInUsers()).build();
	}

	@Override
	public Response logout(String username) {
		/*AgentMessage message = new AgentMessage();
		message.userArgs.put("receiver", "chat");
		message.userArgs.put("command", "LOG_OUT");
		message.userArgs.put("username", username);
		messageManager.post(message);*/
		if(!chatManager.logout(username)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response getRegisteredUsers() {
		/*AgentMessage message = new AgentMessage();
		message.userArgs.put("receiver", "chat");
		message.userArgs.put("command", "GET_REGISTERED");
		
		messageManager.post(message);*/
		return Response.status(Response.Status.OK).entity(chatManager.loggedInUsers()).build();
	}

}
