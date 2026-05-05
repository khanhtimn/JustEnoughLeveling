package dev.khanhtimn.jel.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OreHighlightRenderer {

	private static final Map<BlockPos, HighlightEntry> HIGHLIGHTS = new ConcurrentHashMap<>();

	public static void addHighlight(BlockPos pos, int color, long expireTick) {
		HIGHLIGHTS.put(pos.immutable(), new HighlightEntry(color, expireTick));
	}

	public static void clearHighlights() {
		HIGHLIGHTS.clear();
	}

	public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Camera camera, long gameTick) {
		if (HIGHLIGHTS.isEmpty()) return;

		Iterator<Map.Entry<BlockPos, HighlightEntry>> it = HIGHLIGHTS.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<BlockPos, HighlightEntry> entry = it.next();
			if (gameTick >= entry.getValue().expireTick) {
				it.remove();
				continue;
			}
			renderOutline(poseStack, bufferSource, camera, entry.getKey(), entry.getValue().color);
		}
	}

	private static void renderOutline(
			PoseStack poseStack, MultiBufferSource bufferSource,
			Camera camera, BlockPos pos, int color) {
		Vec3 camPos = camera.getPosition();
		double x = pos.getX() - camPos.x;
		double y = pos.getY() - camPos.y;
		double z = pos.getZ() - camPos.z;

		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;
		float a = 0.8f;

		poseStack.pushPose();
		poseStack.translate(x, y, z);

		Matrix4f matrix = poseStack.last().pose();
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.LINES);

		drawBox(consumer, matrix, 0, 0, 0, 1, 1, 1, r, g, b, a);

		poseStack.popPose();
	}

	private static void drawBox(
			VertexConsumer consumer, Matrix4f matrix,
			float x1, float y1, float z1, float x2, float y2, float z2,
			float r, float g, float b, float a) {
		line(consumer, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
		line(consumer, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
		line(consumer, matrix, x1, y1, z1, x1, y1, z2, r, g, b, a);
		line(consumer, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
		line(consumer, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
		line(consumer, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
		line(consumer, matrix, x1, y2, z1, x1, y2, z2, r, g, b, a);
		line(consumer, matrix, x1, y1, z2, x2, y1, z2, r, g, b, a);
		line(consumer, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a);
		line(consumer, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
		line(consumer, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
		line(consumer, matrix, x1, y2, z2, x2, y2, z2, r, g, b, a);
	}

	private static void line(
			VertexConsumer consumer, Matrix4f matrix,
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float r, float g, float b, float a) {
		float nx = x2 - x1;
		float ny = y2 - y1;
		float nz = z2 - z1;
		float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		if (len < 1e-6f) return;
		nx /= len;
		ny /= len;
		nz /= len;
		consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(nx, ny, nz);
		consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(nx, ny, nz);
	}

	private record HighlightEntry(int color, long expireTick) {
	}

	private OreHighlightRenderer() {
	}
}
