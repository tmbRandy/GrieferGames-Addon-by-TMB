package tmb.randy.tmbgriefergames.v1_8_9.mixins;

import net.labymod.api.Laby;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tmb.randy.tmbgriefergames.core.events.FishEvent;

@Mixin(SoundManager.class)
public class MixinSoundManager {

    @Inject(method = "playSound", at = @At("HEAD"))
    private void onPlaySound(ISound sound, CallbackInfo ci) {
        if (sound != null && sound.getSoundLocation() != null) {
            if (sound.getSoundLocation().getResourcePath().contains("random.splash")) {
                Laby.fireEvent(new FishEvent(sound.getXPosF(), sound.getYPosF(), sound.getZPosF()));
            }
        }
    }
}
