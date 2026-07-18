package twilightforest.mixin;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectileMixinContractTests {
	@Test
	void cancelsPerfectDodgeBeforePolymorphicProjectileImpactDispatch() throws IOException {
		String projectileMixin = Files.readString(Path.of("src/main/java/twilightforest/mixin/ProjectileMixin.java"));
		String mixinConfig = Files.readString(Path.of("src/main/resources/twilightforest.mixins.json"));
		String dodgeMethodMarker = "method = \"hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)"
			+ "Lnet/minecraft/world/entity/projectile/ProjectileDeflection;\"";
		String baseHitMethodMarker = "@Inject(method = \"onHitEntity\"";
		String perfectDodgeCall = "TravellersGearEvents.onProjectileHitEntity";
		int dodgeHookStart = projectileMixin.indexOf(dodgeMethodMarker);
		int baseHitHookStart = projectileMixin.indexOf(baseHitMethodMarker);

		assertTrue(dodgeHookStart >= 0, "Perfect Dodge must intercept the common projectile impact gateway");
		assertTrue(baseHitHookStart > dodgeHookStart, "Perfect Dodge must run before the base onHitEntity hook");
		String dodgeHook = projectileMixin.substring(dodgeHookStart, baseHitHookStart);
		assertTrue(dodgeHook.contains("at = @At(\"HEAD\")"));
		assertFalse(dodgeHook.contains("Projectile;onHit("),
			"Waiting for onHit dispatch would put Perfect Dodge after vanilla deflection handling");
		assertTrue(dodgeHook.contains("cancellable = true"));
		assertTrue(dodgeHook.contains("result instanceof EntityHitResult"));
		assertTrue(dodgeHook.contains(perfectDodgeCall));
		assertTrue(dodgeHook.contains("projectile instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0"));
		assertTrue(dodgeHook.contains("? ProjectileDeflection.REVERSE"),
			"Piercing arrows need a non-NONE control result to leave their collision loop after a dodge");
		assertTrue(dodgeHook.contains(": ProjectileDeflection.NONE"),
			"Non-arrow projectiles must keep the ordinary canceled-impact result");

		String baseHitHook = projectileMixin.substring(baseHitHookStart);
		assertFalse(baseHitHook.contains(perfectDodgeCall),
			"Cancelling Projectile.onHitEntity cannot stop AbstractArrow.onHitEntity from continuing");
		assertTrue(baseHitHook.contains("EntityEvents.handleParryProjectile"));
		assertTrue(baseHitHook.contains("ToolEvents.handleEnderBowHit"));
		assertEquals(projectileMixin.indexOf(perfectDodgeCall), projectileMixin.lastIndexOf(perfectDodgeCall),
			"Perfect Dodge should have exactly one projectile-impact entry point");
		assertTrue(mixinConfig.contains("\"ProjectileMixin\""));
	}
}
