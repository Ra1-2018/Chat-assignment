package messagestorage;

import java.util.List;

import javax.ejb.Remote;

import models.Message;

@Remote
public interface MessageStorageRemote {

	public List<Message> getAll();
	
	public boolean addMessage(Message message);
}
