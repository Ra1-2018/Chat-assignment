package chatmanager;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import connectionmanager.ConnectionManager;
import models.Host;
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
		connectionManager.notifyAllRegistered();
		return true;
	}

	@Override
	public boolean login(User user) {
		boolean exists = registered.stream().anyMatch(u->u.getUsername().equals(u.getUsername()) && u.getPassword().equals(u.getPassword()));
		if(!exists)
			return false;
		user.setHost(getLocalHost());
		System.out.println("New user logged in on host: " + user.getHost());
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
	
	@Override
	public void setRegisteredUsers(List<User> users) {
		registered = users;
	}

	private Host getLocalHost() {
		return new Host(getNodeAlias() + ":8080", getNodeAddress());
	}
	
	private String getNodeAddress() {		
		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			ObjectName http = new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http");
			return (String) mBeanServer.getAttribute(http, "boundAddress");			
		} catch (MalformedObjectNameException | InstanceNotFoundException | AttributeNotFoundException | ReflectionException | MBeanException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String getNodeAlias() {		
		return System.getProperty("jboss.node.name");
	}
}
