package gg.alexandre.extended.commands;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import gg.alexandre.extended.interact.VolumeInteractionRunner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Locale;

public class RunTriggerVolumeCommand extends AbstractPlayerCommand {

    private static final String DEFAULT_EVENT = "command";

    @Nonnull
    private final RequiredArg<String> volumeIdArg;
    @Nonnull
    private final DefaultArg<String> eventArg;

    public RunTriggerVolumeCommand() {
        super("runtriggervolume", "Run trigger volume effects");
        requirePermission("alexandre.triggervolumesextended.command.runtriggervolume");

        volumeIdArg = withRequiredArg("id", "Trigger volume id", ArgTypes.STRING);
        volumeIdArg.suggest(RunTriggerVolumeCommand::suggestVolumeIds);

        eventArg = withDefaultArg("event", "Trigger event", ArgTypes.STRING, DEFAULT_EVENT, DEFAULT_EVENT);
        eventArg.suggest(RunTriggerVolumeCommand::suggestEvents);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> playerRef, @Nonnull PlayerRef player, @Nonnull World world) {
        TriggerEventType eventType = parseEvent(context, eventArg.get(context));
        if (eventType == null) {
            return;
        }

        TriggerVolumeManager manager = store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());

        String volumeId = volumeIdArg.get(context);
        VolumeEntry volume = manager.getVolume(volumeId);
        if (volume == null) {
            context.sendMessage(Message.raw("Trigger volume '" + volumeId + "' does not exist."));
            return;
        }

        if (!volume.isEnabled() || volume.isPendingDestroy()) {
            context.sendMessage(Message.raw("Trigger volume '" + volumeId + "' is not active."));
            return;
        }

        VolumeInteractionRunner.fire(
                eventType,
                playerRef,
                player.getUuid(),
                volume,
                manager,
                store,
                System.nanoTime(),
                true
        );

        context.sendMessage(Message.raw(
                "Ran " + eventType.name().toLowerCase(Locale.ROOT) + " effects for trigger volume '" + volumeId + "'."
        ));
    }

    @Nullable
    private static TriggerEventType parseEvent(@Nonnull CommandContext context, @Nonnull String rawEvent) {
        try {
            return TriggerEventType.valueOf(rawEvent.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            context.sendMessage(Message.raw(
                    "Unknown trigger event '" + rawEvent + "'. Use one of: " + String.join(", ", eventNames()) + "."
            ));
            return null;
        }
    }

    @Nonnull
    private static String[] eventNames() {
        List<TriggerEventType> values = TriggerEventType.values();
        String[] names = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            names[i] = values.get(i).name().toLowerCase(Locale.ROOT);
        }
        return names;
    }

    private static void suggestVolumeIds(@Nonnull CommandSender sender, @Nonnull String input, int index,
                                         @Nonnull SuggestionResult result) {
        if (!(sender instanceof PlayerRef player) || !player.isValid()) {
            return;
        }

        Ref<EntityStore> ref = player.getReference();
        if (ref == null || !ref.isValid()) {
            return;
        }

        TriggerVolumeManager manager = ref.getStore().getResource(TriggerVolumesPlugin.get().getManagerResourceType());

        String prefix = input.toLowerCase(Locale.ROOT);
        for (VolumeEntry volume : manager.getVolumes()) {
            String id = volume.getId();
            if (id.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                result.suggest(id);
            }
        }
    }

    private static void suggestEvents(@Nonnull CommandSender sender, @Nonnull String input, int index,
                                      @Nonnull SuggestionResult result) {
        String prefix = input.toLowerCase(Locale.ROOT);
        for (TriggerEventType eventType : TriggerEventType.values()) {
            String event = eventType.name().toLowerCase(Locale.ROOT);
            if (event.startsWith(prefix)) {
                result.suggest(event);
            }
        }
    }

}
