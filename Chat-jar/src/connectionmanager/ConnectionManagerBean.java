package connectionmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.ws.rs.Path;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import chatmanager.ChatManagerRemote;
import messagemanager.AgentMessage;
import messagemanager.MessageManagerRemote;
import models.User;
import util.FileUtils;
import ws.WSChat;

@Singleton
@Startup
@Remote(ConnectionManager.class)
@Path("/connection")
public class ConnectionManagerBean implements ConnectionManager {

	private String nodeAddress;
	private String nodeAlias;
	private List<String> connections = new ArrayList<>();
	private String masterAlias = null;
	
	@EJB
	private ChatManagerRemote chatManager;
	
	@EJB
	private WSChat ws;
	
	@EJB
	private MessageManagerRemote messageManager;
	
	@PostConstruct
	private void init() {
		nodeAddress = getNodeAddress();
		nodeAlias = getNodeAlias() + ":8080";
		masterAlias = getMasterAlias();
		System.out.println("MASTER ADDR: " + masterAlias + ", node name: " + nodeAlias + ", node address: " + nodeAddress);
		if (masterAlias != null && !masterAlias.equals("")) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + masterAlias + "/Chat-war/api/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			connections = rest.registerNode(nodeAlias);
			connections.remove(nodeAlias);
			connections.add(masterAlias);
			client.close();
			System.out.println("Number of connected nodes: " + connections.size());
		}
		else {
			System.out.println("Master node started");
		}

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
	
	private String getMasterAlias() {
		try {
			File f = FileUtils.getFile(ConnectionManager.class, "", "connections.properties");
			FileInputStream fileInput = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			return properties.getProperty("master");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public List<String> registerNode(String nodeAlias) {
		System.out.println("New node registered: " + nodeAlias);
		for (String c : connections) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + c + "/Chat-war/api/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.addNode(nodeAlias);
			client.close();
		}
		connections.add(nodeAlias);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				ResteasyClient resteasyClient = new ResteasyClientBuilder().build();
				ResteasyWebTarget rtarget = resteasyClient.target("http://"  + nodeAlias + "/Chat-war/api/connection");
				ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
				rest.setLoggedInRemote(chatManager.loggedInUsers());
				rest.setRegisteredRemote(chatManager.registeredUsers());
				resteasyClient.close();
			}
		}).start();
		return connections;
	}

	@Override
	public void addNode(String nodeAlias) {
		System.out.println("New node added: " + nodeAlias);
		connections.add(nodeAlias);
	}

	@Override
	public void deleteNode(String nodeAlias) {
		System.out.println("Node removed: " + nodeAlias);
		connections.remove(nodeAlias);
	}

	@Override
	public String pingNode() {
		System.out.println("Node pinged");
		return "OK";
	}
	
	@PreDestroy
	private void shutDown() {
		notifyAllDelete(nodeAlias);
		notifyLogOutOnShutDown(nodeAlias);
	}
	
	private void notifyLogOutOnShutDown(String alias) {
		System.out.println("Logging out users on node: " + alias);
		List<User> users = chatManager.loggedInUsers();
		List<User> ret = new ArrayList<>();
		for(User u: users) {
			if(!u.getHost().getAlias().equals(alias)) {
				ret.add(u);
			}
		}
		chatManager.setLoggedInUsers(ret);
		notifyAllLoggedIn();
	}
	
	private void notifyAllDelete(String alias) {
		for(String c : connections) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + c + "/Chat-war/api/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.deleteNode(alias);
			client.close();
		}
	}

	@Schedule(hour = "*", minute="*/1", persistent=false)
	private void heartbeat() {
		System.out.println("Heartbeat protocol started");
		for(String c : connections) {
			System.out.println("Pinging node: " + c);
			new Thread(new Runnable() {
				@Override
				public void run() {
					if(!nodeResponded(c)) {
						System.out.println("Node: " + c + " not responding");
						connections.remove(c);
						notifyAllDelete(c);
						notifyLogOutOnShutDown(c);
					}
				}
			}).start();
		}
	}
	
	private boolean nodeResponded(String alias) {
		for(int i=0; i<2; i++) {
			try {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rtarget = client.target("http://" + alias + "/Chat-war/api/connection");
				ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
				String response = rest.pingNode();
				client.close();
				if(response.equals("OK")) {
					return true;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void notifyAllLoggedIn() {
		for (String c: connections) {
			System.out.println("Sending logged users to node: " + c);
			ResteasyClient resteasyClient = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = resteasyClient.target("http://" + c + "/Chat-war/api/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.setLoggedInRemote(chatManager.loggedInUsers());
			resteasyClient.close();
		}		
	}

	@Override
	public void setLoggedInRemote(List<User> users) {
		System.out.println("Number of logged users: " + users.size());
		chatManager.setLoggedInUsers(users);
		for(User u : chatManager.loggedInUsers()) {
			if(!u.getHost().getAlias().equals(getNodeAlias() + ":8080")) {
				continue;
			}
			AgentMessage message = new AgentMessage();
			message.userArgs.put("receiver", u.getUsername());
			message.userArgs.put("command", "GET_LOGGEDIN");
			
			messageManager.post(message);
		}
	}

	@Override
	public void notifyAllRegistered() {
		for (String c: connections) {
			System.out.println("Sending registered users to node: " + c);
			ResteasyClient resteasyClient = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = resteasyClient.target("http://" + c + "/Chat-war/api/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.setRegisteredRemote(chatManager.registeredUsers());
			resteasyClient.close();
		}	
	}

	@Override
	public void setRegisteredRemote(List<User> users) {
		System.out.println("Number of registered users: " + users.size());
		chatManager.setRegisteredUsers(users);
		for(User u : chatManager.loggedInUsers()) {
			if(!u.getHost().getAlias().equals(getNodeAlias() + ":8080")) {
				continue;
			}
			AgentMessage message = new AgentMessage();
			message.userArgs.put("receiver", u.getUsername());
			message.userArgs.put("command", "GET_REGISTERED");
			
			messageManager.post(message);
		}
	}

}
