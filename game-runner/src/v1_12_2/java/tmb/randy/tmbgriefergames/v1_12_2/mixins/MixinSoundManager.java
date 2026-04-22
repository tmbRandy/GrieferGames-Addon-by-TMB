package tmb.randy.tmbgriefergames.v1_12_2.mixins;

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
        if(sound.getSoundLocation().getPath().contains("entity.bobber.splash")) {
            Laby.fireEvent(new FishEvent(sound.getXPosF(), sound.getYPosF(), sound.getZPosF()));
        }
    }
}
