package gg.alexandre.extended;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class CommandEffect extends TriggerEffect {

    private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("(?<!\\S)@p(?!\\S)");

    @Nonnull
    public static final BuilderCodec<CommandEffect> CODEC = BuilderCodec.builder(
                    CommandEffect.class, CommandEffect::new, BASE_CODEC
            )
            .append(
                    new KeyedCodec<>("ExecuteAsPlayer", Codec.BOOLEAN, false),
                    (e, v) -> e.executeAsPlayer = v,
                    (e) -> e.executeAsPlayer
            ).add()
            .append(
                    new KeyedCodec<>("Command", Codec.STRING, false),
                    (e, v) -> e.command = v,
                    (e) -> e.command
            ).add()
            .build();

    private boolean executeAsPlayer = false;
    private String command = "";

    public void execute(@Nonnull TriggerContext context) {
        String commandLine = command.strip();
        if (commandLine.isBlank()) {
            return;
        }

        Ref<EntityStore> entityRef = context.getEntityRef();
        Store<EntityStore> store = context.getStore();

        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef != null) {
            commandLine = commandLine.startsWith("/") ? commandLine.substring(1) : commandLine;
            commandLine = PLAYER_NAME_PATTERN.matcher(commandLine).replaceAll(playerRef.getUsername());

            CommandManager.get().handleCommand(
                    executeAsPlayer ? playerRef : ConsoleSender.INSTANCE,
                    commandLine
            );
        }
    }

}
