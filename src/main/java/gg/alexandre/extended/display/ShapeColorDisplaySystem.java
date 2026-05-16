package gg.alexandre.extended.display;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.protocol.packets.player.AddOrUpdateTriggerVolumeDisplay;
import com.hypixel.hytale.protocol.packets.player.RemoveTriggerVolumeDisplay;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeDisplayEntry;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import gg.alexandre.extended.effects.ShapeColorEffect;

import javax.annotation.Nonnull;
import java.util.*;

public class ShapeColorDisplaySystem extends TickingSystem<EntityStore> {

    public static final String DISPLAY_ID_PREFIX = "ShapeColor:";
    private static final long EXECUTE_OVERRIDE_NANOS = 500_000_000L;

    @Override
    public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
        TriggerVolumeManager manager = store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
        ShapeColorDisplayResource resource = store.getResource(ShapeColorDisplayResource.getResourceType());

        List<PlayerRef> players = new ArrayList<>(store.getExternalData().getWorld().getPlayerRefs());

        Set<String> validDisplayIds = new HashSet<>();
        Set<String> liveTickDisplayIds = new HashSet<>();
        for (VolumeEntry volume : manager.getVolumes()) {
            String displayId = displayId(volume);
            if (!volume.isEnabled() || volume.isPendingDestroy()) {
                continue;
            }

            validDisplayIds.add(displayId);

            ShapeColorEffect tickEffect = findTickEffect(volume);
            if (tickEffect == null) {
                continue;
            }

            TriggerVolumeDisplayEntry entry = createEntry(manager, volume, tickEffect);
            liveTickDisplayIds.add(displayId);
            resource.put(displayId, entry, true);
        }

        resource.removeMissingSystemManagedDisplays(liveTickDisplayIds, displayId -> sendRemove(players, displayId));
        resource.removeInvalidDisplays(validDisplayIds, displayId -> sendRemove(players, displayId));

        for (Map.Entry<String, ShapeColorDisplayResource.DisplayState> display : resource.entries()) {
            String volumeId = volumeId(display.getKey());
            VolumeEntry volume = manager.getVolume(volumeId);
            if (volume == null || !volume.isEnabled() || volume.isPendingDestroy()) {
                continue;
            }

            sendAddOrUpdate(players, display.getKey(), display.getValue().entry());
        }
    }

    private static ShapeColorEffect findTickEffect(@Nonnull VolumeEntry volume) {
        for (var effect : volume.getEffects()) {
            if (effect instanceof ShapeColorEffect shapeColor
                && !shapeColor.isClear()
                && shapeColor.getEventType() == TriggerEventType.TICK) {
                return shapeColor;
            }
        }
        return null;
    }

    public static void apply(@Nonnull Store<EntityStore> store, @Nonnull TriggerVolumeManager manager,
                             @Nonnull VolumeEntry volume, @Nonnull ShapeColorEffect effect) {
        ShapeColorDisplayResource resource = store.getResource(ShapeColorDisplayResource.getResourceType());

        String displayId = displayId(volume);
        TriggerVolumeDisplayEntry entry = createEntry(manager, volume, effect);
        resource.put(displayId, entry, false, System.nanoTime() + EXECUTE_OVERRIDE_NANOS);
        sendAddOrUpdate(new ArrayList<>(store.getExternalData().getWorld().getPlayerRefs()), displayId, entry);
    }

    public static void clear(@Nonnull Store<EntityStore> store, @Nonnull VolumeEntry volume) {
        ShapeColorDisplayResource resource = store.getResource(ShapeColorDisplayResource.getResourceType());

        String displayId = displayId(volume);
        resource.remove(displayId);
        sendRemove(new ArrayList<>(store.getExternalData().getWorld().getPlayerRefs()), displayId);
    }

    @Nonnull
    private static TriggerVolumeDisplayEntry createEntry(@Nonnull TriggerVolumeManager manager,
                                                         @Nonnull VolumeEntry volume,
                                                         @Nonnull ShapeColorEffect effect) {
        int red = (int) (effect.getColor().x * 255) << 16;
        int green = (int) (effect.getColor().y * 255) << 8;
        int blue = (int) (effect.getColor().z * 255);
        int color = red | green | blue;

        TriggerVolumeDisplayEntry entry = new TriggerVolumeDisplayEntry(manager.buildDisplayEntry(volume));
        entry.color = effect.getColor();
        entry.opacity = effect.getOpacity();
        entry.name = null;
        entry.groupId = Integer.toString(color);
        entry.groupColor = color;
        return entry;
    }

    @Nonnull
    public static String displayId(@Nonnull VolumeEntry volume) {
        return DISPLAY_ID_PREFIX + volume.getId();
    }

    @Nonnull
    private static String volumeId(@Nonnull String displayId) {
        return displayId.startsWith(DISPLAY_ID_PREFIX)
                ? displayId.substring(DISPLAY_ID_PREFIX.length())
                : displayId;
    }

    private static void sendAddOrUpdate(@Nonnull List<PlayerRef> players, @Nonnull String displayId,
                                        @Nonnull TriggerVolumeDisplayEntry entry) {
        AddOrUpdateTriggerVolumeDisplay packet = new AddOrUpdateTriggerVolumeDisplay(displayId, entry);
        for (PlayerRef player : players) {
            player.getPacketHandler().write(packet);
        }
    }

    private static void sendRemove(@Nonnull List<PlayerRef> players, @Nonnull String displayId) {
        RemoveTriggerVolumeDisplay packet = new RemoveTriggerVolumeDisplay(displayId);
        for (PlayerRef player : players) {
            player.getPacketHandler().write(packet);
        }
    }

}
