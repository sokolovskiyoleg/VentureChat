package mineverse.Aust1n46.chat;

import net.kyori.adventure.text.Component;

//This class is used to create ChatMessage objects, which are used to store information about previous text components
//that were sent to the player.  This is a main component in making the message remover work.
public class ChatMessage {
	private Component component;
	private String message;
	private String coloredMessage;
	private int hash;

	public ChatMessage(Component component, String message, String coloredMessage, int hash) {
		this.component = component;
		this.message = message;
		this.coloredMessage = coloredMessage;
		this.hash = hash;
	}

	public Component getComponent() {
		return this.component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public String getMessage() {
		return this.message;
	}
	
	public String getColoredMessage() {
		return this.coloredMessage;
	}

	public int getHash() {
		return this.hash;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}
}
