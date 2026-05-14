package gg.alexandre.extended;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class TriggerExtendedPlugin extends JavaPlugin {

    public TriggerExtendedPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        TriggerVolumesPlugin.get().registerEffectType("Command", CommandEffect.class, CommandEffect.CODEC);
    }

}
