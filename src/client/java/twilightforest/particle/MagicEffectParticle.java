package twilightforest.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;

public class MagicEffectParticle extends SingleQuadParticle {
	private final SpriteSet sprites;

	protected MagicEffectParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());
		this.sprites = sprites;
		this.friction = 0.96F;
		this.gravity = -0.1F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.yd *= 0.2D;
		if (xSpeed == 0.0D && zSpeed == 0.0D) {
			this.xd *= 0.1D;
			this.zd *= 0.1D;
		}
		this.quadSize *= 0.75F;
		this.lifetime = (int) (8.0D / (this.random.nextFloat() * 0.8D + 0.2D));
		this.hasPhysics = false;
		this.setSpriteFromAge(sprites);
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
	}

	@Override
	public int getLightCoords(float partialTick) {
		return 0xF000F0;
	}

	public static class Factory implements ParticleProvider<ColorParticleOption> {
		private final SpriteSet sprite;

		public Factory(SpriteSet sprite) {
			this.sprite = sprite;
		}

		public Particle createParticle(ColorParticleOption type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource random) {
			MagicEffectParticle particle = new MagicEffectParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite);
			particle.setColor(type.getRed(), type.getGreen(), type.getBlue());
			particle.setAlpha(type.getAlpha());
			return particle;
		}
	}
}
