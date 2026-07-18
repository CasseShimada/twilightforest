package twilightforest.mixin.accessor;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(SavedDataStorage.class)
public interface SavedDataStorageAccessor {
	@Accessor("dataFolder")
	Path twilightforest$getDataFolder();

	@Accessor("registries")
	HolderLookup.Provider twilightforest$getRegistries();
}
