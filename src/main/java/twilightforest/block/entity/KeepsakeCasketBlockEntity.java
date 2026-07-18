package twilightforest.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFSounds;
import twilightforest.util.PlayerMessaging;

import java.util.Optional;
import java.util.UUID;

public class KeepsakeCasketBlockEntity extends SkullChestBlockEntity {
	static final String LEGACY_OWNER_UUID_TAG = "deadPlayer";
	static final String LEGACY_OWNER_NAME_TAG = "playerName";

	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(Level level, BlockPos pos, BlockState state) {
			level.playSound(null, pos, TFSounds.CASKET_OPEN, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
		}

		@Override
		protected void onClose(Level level, BlockPos pos, BlockState state) {
			level.playSound(null, pos, TFSounds.CASKET_CLOSE, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
		}

		@Override
		protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int id, int param) {
			Block block = state.getBlock();
			level.blockEvent(pos, block, 1, param);
		}

		@Override
		public boolean isOwnContainer(Player player) {
			if (player.containerMenu instanceof ChestMenu) {
				Container container = ((ChestMenu)player.containerMenu).getContainer();
				return container == KeepsakeCasketBlockEntity.this;
			} else {
				return false;
			}
		}
	};

	public KeepsakeCasketBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.KEEPSAKE_CASKET, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		writeLegacyOwner(output, this.owner);
	}

	@Override
	@Nullable
	protected ResolvableProfile readOwner(ValueInput input) {
		return readCompatibleOwner(input);
	}

	@Nullable
	static ResolvableProfile readCompatibleOwner(ValueInput input) {
		Optional<ResolvableProfile> current = input.read(OWNER_TAG, ResolvableProfile.CODEC);
		if (current.isPresent()) {
			return current.get();
		}
		// A present but malformed current value must not be hidden by a valid legacy fallback.
		if (input.child(OWNER_TAG).isPresent() || input.getString(OWNER_TAG).isPresent() || input.getIntArray(OWNER_TAG).isPresent()) {
			return null;
		}

		Optional<UUID> legacyId = input.read(LEGACY_OWNER_UUID_TAG, UUIDUtil.CODEC);
		if (legacyId.isEmpty()) {
			// 1.20.1 also wrote playerName for unlocked caskets, so a name alone must not create a lock.
			return null;
		}
		Optional<String> legacyName = input.getString(LEGACY_OWNER_NAME_TAG).filter(name -> !name.isBlank());
		return legacyName
			.<ResolvableProfile>map(name -> ResolvableProfile.createResolved(new GameProfile(legacyId.get(), name)))
			.orElseGet(() -> ResolvableProfile.createUnresolved(legacyId.get()));
	}

	static void writeLegacyOwner(ValueOutput output, @Nullable ResolvableProfile owner) {
		if (owner == null || owner.partialProfile().id() == null) {
			return;
		}
		output.store(LEGACY_OWNER_UUID_TAG, UUIDUtil.CODEC, owner.partialProfile().id());
		owner.name().filter(name -> !name.isBlank())
			.ifPresent(name -> output.putString(LEGACY_OWNER_NAME_TAG, name));
	}

	@Override
	public ContainerOpenersCounter getOpenersCounter() {
		return this.openersCounter;
	}

	@Override
	public void displayLockedInfo(Player player) {
		player.playSound(TFSounds.CASKET_LOCKED, 0.5F, 0.5F);
		String ownerName = this.owner != null ? this.owner.name().orElse("unknown") : "unknown";
		PlayerMessaging.displayClientMessage(player, Component.translatable("block.twilightforest.casket.locked", ownerName).withStyle(ChatFormatting.RED), true);
	}
}
