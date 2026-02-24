package twilightforest.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
public class AngryLichParticle extends SingleQuadParticle {
	private final SpriteSet sprites;

	protected AngryLichParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D, sprites.get(level.random));
		this.sprites = sprites;

		this.lifetime = 10;
		this.yd -= 0.05;
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

	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Factory(SpriteSet sprites) {
			this.sprite = sprites;
		}

		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource random) {
			AngryLichParticle lichParticle = new AngryLichParticle(level, x, y, z, this.sprite);
			lichParticle.setColor(1.0F, 1.0F, 1.0F);
			lichParticle.scale(0.75F);
			return lichParticle;
		}
	}
}
