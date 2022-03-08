package com.simibubi.create.content.logistics.trains.entity;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class CarriageContraptionEntityRenderer extends ContraptionEntityRenderer<CarriageContraptionEntity> {

	public CarriageContraptionEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(CarriageContraptionEntity entity, Frustum clippingHelper, double cameraX,
		double cameraY, double cameraZ) {
		Carriage carriage = entity.getCarriage();
		if (carriage != null)
			for (CarriageBogey bogey : carriage.bogeys)
				if (bogey != null)
					bogey.leadingCouplingAnchor = bogey.trailingCouplingAnchor = null;
		if (!super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ))
			return false;
		return entity.validForRender;
	}

	@Override
	public void render(CarriageContraptionEntity entity, float yaw, float partialTicks, PoseStack ms,
		MultiBufferSource buffers, int overlay) {
		super.render(entity, yaw, partialTicks, ms, buffers, overlay);

		Carriage carriage = entity.getCarriage();
		if (carriage == null)
			return;

		Vec3 position = entity.getPosition(partialTicks);

		ms.pushPose();
		carriage.bogeys.forEachWithContext((bogey, first) -> {
			if (!first && !carriage.isOnTwoBogeys())
				return;

			ms.pushPose();
			float viewYRot = entity.getViewYRot(partialTicks);
			float viewXRot = entity.getViewXRot(partialTicks);
			int bogeySpacing = carriage.bogeySpacing;
			TransformStack.cast(ms)
				.rotateY(viewYRot + 90)
				.rotateX(-viewXRot)
				.rotateY(180)
				.translate(0, 0, first ? 0 : -bogeySpacing)
				.rotateY(-180)
				.rotateX(viewXRot)
				.rotateY(-viewYRot - 90)
				.rotateY(bogey.yaw.getValue(partialTicks))
				.rotateX(bogey.pitch.getValue(partialTicks))
				.translate(0, .5f, 0);

			bogey.type.render(null, bogey.wheelAngle.getValue(partialTicks), ms, partialTicks, buffers,
				getPackedLightCoords(entity, partialTicks), overlay);
			bogey.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, partialTicks, first);
			if (!carriage.isOnTwoBogeys())
				bogey.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, partialTicks, !first);

			ms.popPose();
		});
		ms.popPose();

	}

}
