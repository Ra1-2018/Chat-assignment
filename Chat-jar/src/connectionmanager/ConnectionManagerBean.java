package connectionmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
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
	private String masterAlias;
	
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
			FileInputStream fileInput;
			fileInput = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			return properties.getProperty("master") + ":8080";
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public List<String> registerNode(String nodeAlias) {
		for (String c : connections) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + c + "/Chat-war/api/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.addNode(nodeAlias);
		}
		connections.add(nodeAlias);
		return connections;
	}

	@Override
	public void addNode(String nodeAlias) {
		connections.add(nodeAlias);
	}

	@Override
	public void deleteNode(String alias) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String pingNode() {
		// TODO Auto-generated method stub
		return null;
	}

}
