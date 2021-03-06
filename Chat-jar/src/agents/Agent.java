package agents;

import java.io.Serializable;

import javax.jms.Message;

public interface Agent extends Serializable {

	public String init(String agentId);
	public void handleMessage(Message message);
	public String getAgentId();
}
