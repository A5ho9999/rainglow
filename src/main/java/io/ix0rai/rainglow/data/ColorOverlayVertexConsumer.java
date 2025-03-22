package io.ix0rai.rainglow.data;

import net.minecraft.client.render.VertexConsumer;

public class ColorOverlayVertexConsumer implements VertexConsumer {
    private final VertexConsumer base;
    private final float r;
    private final float g;
    private final float b;

    public ColorOverlayVertexConsumer(VertexConsumer base, float r, float g, float b) {
        this.base = base;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        return base.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        // Adjust Alpha value to stop my eyes from blowing out
        // Change the float value to control transparency (For outer slime layer)
        int adjustedAlpha = (int)(alpha * 0.9f);

        return base.color(Math.min(255, (int)(red * r)), Math.min(255, (int)(green * g)), Math.min(255, (int)(blue * b)), adjustedAlpha);
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return base.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return base.overlay(u, v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return base.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return base.normal(x, y, z);
    }
}
