package gg.alexandre.extended.effects;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class DestroyOtherVolumeEffect extends TriggerEffect {

    @Nonnull
    public static final BuilderCodec<DestroyOtherVolumeEffect> CODEC = BuilderCodec.builder(
                    DestroyOtherVolumeEffect.class, DestroyOtherVolumeEffect::new, BASE_CODEC
            )
            .append(
                    new KeyedCodec<>("VolumeID", Codec.STRING, false),
                    (e, v) -> e.id = v,
                    (e) -> e.id
            ).add()
            .build();

    private String id = "";

    @Override
    public void execute(@Nonnull TriggerContext context) {
        if (id.isBlank()) {
            return;
        }

        Store<EntityStore> store = context.getStore();

        TriggerVolumeManager manager = store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());

        VolumeEntry volume = manager.getVolume(id);
        if (volume != null) {
            manager.unregister(volume.getId());
            manager.notifyViewersRemove(volume.getId());
        }
    }

}
