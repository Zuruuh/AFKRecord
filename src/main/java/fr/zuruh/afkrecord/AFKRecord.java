package fr.zuruh.afkrecord;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AFKRecord implements ClientModInitializer {

	public static KeyBinding keyTape;

	public static final Logger LOGGER = LoggerFactory.getLogger("afkrecord");

	@Override
	public void onInitializeClient() {
		LOGGER.debug("Client initialized!");

		 AFKRecord.keyTape = new KeyBinding(
				 "afkrecord",
				 GLFW.GLFW_KEY_K,
				 "key.categories.misc"
		 );

		 KeyBindingHelper.registerKeyBinding(AFKRecord.keyTape);
	}
}
