package fr.zuruh.afkrecord.mixin;

import fr.zuruh.afkrecord.AFKRecord;
import fr.zuruh.afkrecord.Manager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin extends ReentrantThreadExecutor<Runnable>
        implements WindowEventHandler {

    @Shadow
    public Screen currentScreen;

    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    public ClientPlayerEntity player;

    public MinecraftClientMixin(String string) {
        super(string);
    }

    // disable if running when player is dead or doesn't exist (in game menu, etc)
    @Inject(at = @At("HEAD"), method = "tick()V")
    private void tapeModifyTick(CallbackInfo info) {

        if (Manager.INSTANCE.isRunningIgnorePause() && (player == null || !player.isAlive()))
            Manager.INSTANCE.disable();

    }

    // cancel automatic game pause while running
    @Inject(at = @At("HEAD"), method = "openPauseMenu(Z)V", cancellable = true)
    private void tapeModifyOpenPauseMenu(CallbackInfo info) {

        if (Manager.INSTANCE.isRunning()) info.cancel();

    }

    // pause/unpause tape on open/close
    @Inject(at = @At("HEAD"), method = "setScreen(Lnet/minecraft/client/gui/screen/Screen;)V")
    private void tapeModifyOpenScreen(Screen screen, CallbackInfo info) {

        if (screen == null && currentScreen != null) {
            Manager.INSTANCE.unpause();
        } else if (currentScreen == null && screen != null) {
            Manager.INSTANCE.pause();
        }

    }

    // press keys if enabled
    @Inject(at = @At("HEAD"), method = "handleInputEvents()V")
    private void tapeModifyHandleInputEvents(CallbackInfo info) {

        if (AFKRecord.keyTape.wasPressed()) {
            Set<KeyBinding> pressedKeybinds = new HashSet<>();
            for (KeyBinding keyBinding : options.allKeys) {
                if (keyBinding.isPressed()) {
                    if (keyBinding != AFKRecord.keyTape)
                        pressedKeybinds.add(keyBinding);
                }
            }
            if (!pressedKeybinds.isEmpty())
                Manager.INSTANCE.enable(pressedKeybinds);
        }

        if (Manager.INSTANCE.isRunning()) {
            if (Manager.INSTANCE.wasPaused) {
                Manager.INSTANCE.enabledKeys.forEach(key -> KeyBinding.onKeyPressed(((KeyBindingAccessor) key).getKeyCode()));
                Manager.INSTANCE.wasPaused = false;
            } else {
                Manager.INSTANCE.enabledKeys.forEach(key -> key.setPressed(true));
            }
        }

    }


}
