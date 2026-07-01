package twilightforest.init;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import twilightforest.TwilightForestMod;
import twilightforest.advancements.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class TFAdvancements {

	private static final Map<Identifier, CriterionTrigger<?>> TRIGGERS = new LinkedHashMap<>();
	private static boolean registered;

	public static final SimpleAdvancementTrigger MADE_TF_PORTAL = makeTrigger("make_tf_portal", new SimpleAdvancementTrigger());
	public static final SimpleAdvancementTrigger CONSUME_HYDRA_CHOP = makeTrigger("consume_hydra_chop_on_low_hunger", new SimpleAdvancementTrigger());
	public static final SimpleAdvancementTrigger QUEST_RAM_COMPLETED = makeTrigger("complete_quest_ram", new SimpleAdvancementTrigger());
	public static final SimpleAdvancementTrigger PLACED_TROPHY_ON_PEDESTAL = makeTrigger("placed_on_trophy_pedestal", new SimpleAdvancementTrigger());
	public static final SimpleAdvancementTrigger ACTIVATED_GHAST_TRAP = makeTrigger("activate_ghast_trap", new SimpleAdvancementTrigger());
	public static final StructureClearedTrigger STRUCTURE_CLEARED = makeTrigger("structure_cleared", new StructureClearedTrigger());
	public static final DrinkFromFlaskTrigger DRINK_FROM_FLASK = makeTrigger("drink_from_flask", new DrinkFromFlaskTrigger());
	public static final KillBugTrigger KILL_BUG = makeTrigger("kill_bug", new KillBugTrigger());
	public static final HurtBossTrigger HURT_BOSS = makeTrigger("hurt_boss", new HurtBossTrigger());
	public static final SimpleAdvancementTrigger KILL_ALL_PHANTOMS = makeTrigger("kill_all_phantoms", new SimpleAdvancementTrigger());
	public static final UncraftItemTrigger UNCRAFT_ITEM = makeTrigger("uncraft_item", new UncraftItemTrigger());
	public static final SimpleAdvancementTrigger BROKE_GLASS_SWORD = makeTrigger("broke_glass_sword", new SimpleAdvancementTrigger());

	private static <T extends CriterionTrigger<?>> T makeTrigger(String name, T trigger) {
		TRIGGERS.put(TwilightForestMod.prefix(name), trigger);
		return trigger;
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		TRIGGERS.forEach((id, trigger) -> Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger));
	}
}
