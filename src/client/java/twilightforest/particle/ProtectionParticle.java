package twilightforest.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class ProtectionParticle extends SingleQuadParticle {

	public ProtectionParticle(ClientLevel level, double x, double y, double z, double velX, double velY, double velZ, SpriteSet sprites) {
		super(level, x, y, z, velX, velY, velZ, sprites.get(level.getRandom()));
		float f = this.random.nextFloat() * 0.1F + 0.2F;
		this.rCol = f;
		this.gCol = f;
		this.bCol = f;
		this.setSize(0.02F, 0.02F);
		this.quadSize *= this.random.nextFloat() * 0.6F + 0.5F;
		this.xd *= 0.02D;
		this.yd *= 0.02D;
		this.zd *= 0.02D;
		this.lifetime = (int) (20.0D / (this.random.nextFloat() * 0.8D + 0.2D));
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.OPAQUE;
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().move(x, y, z));
		this.setLocationFromBoundingbox();
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		if (--this.lifetime <= 0) {
			this.remove();
			return;
		}

		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.99D;
		this.yd *= 0.99D;
		this.zd *= 0.99D;
	}

	@Override
	public int getLightCoords(float partialTicks) {
		return 0xF000F0;
	}

	public record Factory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, net.minecraft.util.RandomSource random) {
			ProtectionParticle particle = new ProtectionParticle(level, x, y, z, vx, vy, vz, this.sprite);
			particle.setColor(1.0F, 1.0F, 1.0F);
			return particle;
		}
	}

}
