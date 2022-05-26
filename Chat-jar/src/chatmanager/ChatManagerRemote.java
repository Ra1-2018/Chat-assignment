package chatmanager;

import java.util.List;

import javax.ejb.Remote;

import models.User;

@Remote
public interface ChatManagerRemote {

	public boolean login(User user);

	public boolean register(User user);

	public List<User> loggedInUsers();
	
	public boolean logout(String username);
	
	public List<User> registeredUsers();
	
	public void setLoggedInUsers(List<User> users);
	
}
