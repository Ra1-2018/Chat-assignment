package agents;

import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import chatmanager.ChatManagerRemote;
import messagestorage.MessageStorageRemote;
import models.User;
import ws.WSChat;
/**
 * Sledece nedelje cemo prebaciti poruke koje krajnji korisnik treba da vidi da se 
 * salju preko Web Socketa na front-end (klijentski deo aplikacije)
 * @author Aleksandra
 *
 */
@Stateful
@Remote(Agent.class)
public class UserAgent implements Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String agentId;
	@EJB
	private CachedAgentsRemote cachedAgents;
	@EJB
	private WSChat ws;
	@EJB
	private ChatManagerRemote chatManager;
	@EJB
	private MessageStorageRemote messageStorage;

	@PostConstruct
	public void postConstruct() {
		System.out.println("Created User Agent!");
	}
	
	@Override
	public void handleMessage(Message message) {
		TextMessage tmsg = (TextMessage) message;

		String receiver;
		try {
			receiver = (String) tmsg.getObjectProperty("receiver");
			if (agentId.equals(receiver)) {
				String option = "";
				String response = "";
				try {
					option = (String) tmsg.getObjectProperty("command");
					switch (option) {
					case "GET_LOGGEDIN":
						response = "LOGGEDIN!";
						List<User> loggedUsers = chatManager.loggedInUsers();
						for (User u : loggedUsers) {
							response += u.toString() + "|";
						}

						break;
					case "GET_REGISTERED":
						response = "REGISTERED!";
						List<User> registeredUsers = chatManager.registeredUsers();
						for (User u : registeredUsers) {
							response += u.toString() + "|";
						}

						break;
					case "MESSAGE":
						response = "MESSAGE!";
						String sender = (String) tmsg.getObjectProperty("sender");
						String content = (String) tmsg.getObjectProperty("content");
						String subject = (String) tmsg.getObjectProperty("subject");
						models.Message msg = new models.Message(new User(receiver, ""), new User(sender, ""), LocalDateTime.now(), subject, content);
						messageStorage.addMessage(msg);
						response += msg.toString();
						break;
					case "GET_MESSAGES":
						response = "MESSAGES!";
						for(models.Message m : messageStorage.getAll()) {
							response += m.toString() + "|";
						}
						break;
					default:
						response = "ERROR!Option: " + option + " does not exist.";
						break;
					}
					System.out.println(response);
					ws.onMessage(agentId, response);
					
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String init(String agentId) {
		this.agentId = agentId;
		cachedAgents.addRunningAgent(agentId, this);
		return agentId;
	}

	@Override
	public String getAgentId() {
		return agentId;
	}
}
