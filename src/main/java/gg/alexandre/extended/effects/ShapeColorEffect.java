package gg.alexandre.extended.effects;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import gg.alexandre.extended.display.ShapeColorDisplaySystem;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.awt.*;

public class ShapeColorEffect extends TriggerEffect {

    @Nonnull
    public static final BuilderCodec<ShapeColorEffect> CODEC = BuilderCodec.builder(
                    ShapeColorEffect.class, ShapeColorEffect::new, BASE_CODEC
            )
            .append(
                    new KeyedCodec<>("Color", Codec.STRING, false),
                    (e, v) -> e.color = v,
                    (e) -> e.color
            ).add()
            .append(
                    new KeyedCodec<>("Opacity", Codec.FLOAT, false),
                    (e, v) -> e.opacity = Math.max(0, Math.min(1, v)),
                    (e) -> e.opacity
            ).add()
            .append(
                    new KeyedCodec<>("Clear", Codec.BOOLEAN, false),
                    (e, v) -> e.clear = Boolean.TRUE.equals(v),
                    (e) -> e.clear ? true : null
            ).add()
            .build();

    @Nonnull
    private String color = "#FFFFFF";
    private float opacity = 0.3f;
    private boolean clear = false;

    public ShapeColorEffect() {
        setEventType(TriggerEventType.TICK);
    }

    @Override
    public void execute(@Nonnull TriggerContext context) {
        if (clear) {
            ShapeColorDisplaySystem.clear(context.getStore(), context.getVolume());
            return;
        }

        TriggerVolumeManager manager = context.getStore().getResource(TriggerVolumesPlugin.get().getManagerResourceType());
        ShapeColorDisplaySystem.apply(context.getStore(), manager, context.getVolume(), this);
    }

    @Nonnull
    public Vector3f getColor() {
        return parseColor(color);
    }

    public float getOpacity() {
        return Math.max(0.001f, Math.min(1.0f, opacity));
    }

    public boolean isClear() {
        return clear;
    }

    @Nonnull
    private static Vector3f parseColor(String raw) {
        Color color;
        try {
            color = Color.decode(raw);
        } catch (Exception e) {
            color = Color.WHITE;
        }

        return new Vector3f(
                color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                (color.getBlue() / 255.0f) * 0.6f
        );
    }

}
