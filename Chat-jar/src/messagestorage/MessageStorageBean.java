package messagestorage;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateful;

import models.Message;

@Stateful
@LocalBean
public class MessageStorageBean implements MessageStorageRemote, MessageStorageLocal{

	private List<Message> messages = new ArrayList<Message>();
	
	public MessageStorageBean() { }
	
	@Override
	public List<Message> getAll() {
		return messages;
	}

	@Override
	public boolean addMessage(Message message) {
		messages.add(message);
		return true;
	}

}
