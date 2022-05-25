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

	@Override
	public List<String> getNodes() {
		return connections;
	}
	
	@PreDestroy
	private void shutDown() {
		notifyAllDelete(nodeAlias);
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
}
