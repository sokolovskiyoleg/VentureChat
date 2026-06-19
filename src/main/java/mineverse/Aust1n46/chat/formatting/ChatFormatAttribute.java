package mineverse.Aust1n46.chat.formatting;

import java.util.List;

import mineverse.Aust1n46.chat.ClickAction;

public class ChatFormatAttribute {
	private final String name;
	private final List<String> hoverText;
	private final ClickAction clickAction;
	private final String clickText;

	public ChatFormatAttribute(String name, List<String> hoverText, ClickAction clickAction, String clickText) {
		this.name = name;
		this.hoverText = hoverText;
		this.clickAction = clickAction;
		this.clickText = clickText;
	}

	public String getName() {
		return name;
	}

	public List<String> getHoverText() {
		return hoverText;
	}

	public ClickAction getClickAction() {
		return clickAction;
	}

	public String getClickText() {
		return clickText;
	}
}
