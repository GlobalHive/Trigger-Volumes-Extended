package gg.alexandre.extended.display;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeDisplayEntry;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ShapeColorDisplayResource implements Resource<EntityStore> {

    private static ResourceType<EntityStore, ShapeColorDisplayResource> resourceType;

    @Nonnull
    private final Map<String, DisplayState> entriesByDisplayId = new HashMap<>();

    public static void setResourceType(@Nonnull ResourceType<EntityStore, ShapeColorDisplayResource> type) {
        resourceType = type;
    }

    @Nonnull
    public static ResourceType<EntityStore, ShapeColorDisplayResource> getResourceType() {
        if (resourceType == null) {
            throw new IllegalStateException("ShapeColorDisplayResource was not registered yet");
        }
        return resourceType;
    }

    public void put(@Nonnull String displayId, @Nonnull TriggerVolumeDisplayEntry entry, boolean systemManaged) {
        put(displayId, entry, systemManaged, 0L);
    }

    public void put(@Nonnull String displayId, @Nonnull TriggerVolumeDisplayEntry entry, boolean systemManaged,
                    long overrideUntilNanos) {
        DisplayState existing = entriesByDisplayId.get(displayId);
        long nowNanos = System.nanoTime();
        if (systemManaged && existing != null && !existing.systemManaged && existing.overrideUntilNanos > nowNanos) {
            return;
        }

        entriesByDisplayId.put(
                displayId,
                new DisplayState(new TriggerVolumeDisplayEntry(entry), systemManaged, overrideUntilNanos)
        );
    }

    public void remove(@Nonnull String displayId) {
        entriesByDisplayId.remove(displayId);
    }

    @Nonnull
    public Set<Map.Entry<String, DisplayState>> entries() {
        return entriesByDisplayId.entrySet();
    }

    public void removeMissingSystemManagedDisplays(@Nonnull Set<String> liveDisplayIds,
                                                   @Nonnull Consumer<String> remover) {
        Iterator<Map.Entry<String, DisplayState>> iterator = entriesByDisplayId.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DisplayState> entry = iterator.next();
            if (!entry.getValue().systemManaged || liveDisplayIds.contains(entry.getKey())) {
                continue;
            }
            remover.accept(entry.getKey());
            iterator.remove();
        }
    }

    public void removeInvalidDisplays(@Nonnull Set<String> validDisplayIds, @Nonnull Consumer<String> remover) {
        Iterator<Map.Entry<String, DisplayState>> iterator = entriesByDisplayId.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DisplayState> entry = iterator.next();
            if (validDisplayIds.contains(entry.getKey())) {
                continue;
            }
            remover.accept(entry.getKey());
            iterator.remove();
        }
    }

    @Override
    public Resource<EntityStore> clone() {
        return new ShapeColorDisplayResource();
    }

    public record DisplayState(@Nonnull TriggerVolumeDisplayEntry entry, boolean systemManaged,
                               long overrideUntilNanos) {

    }

}
