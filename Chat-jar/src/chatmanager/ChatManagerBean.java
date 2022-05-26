package chatmanager;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

import connectionmanager.ConnectionManager;
import models.User;

// TODO Implement the rest of Client-Server functionalities 
/**
 * Session Bean implementation class ChatBean
 */
@Singleton
@LocalBean
public class ChatManagerBean implements ChatManagerRemote, ChatManagerLocal {

	private List<User> registered = new ArrayList<User>();
	private List<User> loggedIn = new ArrayList<User>();
	
	@EJB
	private ConnectionManager connectionManager;
	
	/**
	 * Default constructor.
	 */
	public ChatManagerBean() {
	}

	@Override
	public boolean register(User user) {
		boolean exists = registered.stream().anyMatch(u->u.getUsername().equals(user.getUsername()));
		if(exists)
			return false;
		registered.add(user);
		return true;
	}

	@Override
	public boolean login(User user) {
		boolean exists = registered.stream().anyMatch(u->u.getUsername().equals(u.getUsername()) && u.getPassword().equals(u.getPassword()));
		if(!exists)
			return false;
		loggedIn.add(user);
		connectionManager.notifyAllLoggedIn();
		return true;
	}

	@Override
	public List<User> loggedInUsers() {
		return loggedIn;
	}

	@Override
	public boolean logout(String username) {
		for(User user : loggedIn) {
			if(user.getUsername().equals(username)) {
				loggedIn.remove(user);
				connectionManager.notifyAllLoggedIn();
				return true;
			}
		}
		return false;
	}

	@Override
	public List<User> registeredUsers() {
		return registered;
	}

	@Override
	public void setLoggedInUsers(List<User> users) {
		loggedIn = users;
	}

}
