package twilightforest.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
public class TFCommand {

	private final CenterCommand centerCommand = new CenterCommand();
	private final ConquerCommand conquerCommand = new ConquerCommand();
	private final GenerateBookCommand generateBookCommand = new GenerateBookCommand();
	private final InfoCommand infoCommand = new InfoCommand();
	private final MapBiomesCommand mapBiomesCommand = new MapBiomesCommand();
	private final MapLocatorCommand mapLocatorCommand = new MapLocatorCommand();
	private final ShieldCommand shieldCommand = new ShieldCommand();
	private final SinisterSpawnerCommand spawnerCommand = new SinisterSpawnerCommand();
	private final DisplayPiecesCommand displayPiecesCommand = new DisplayPiecesCommand();
	private final CountLootCommand countLootCommand = new CountLootCommand();
	private final CountTemplateCommand countTemplateCommand = new CountTemplateCommand();
	private final StructureDistanceCommand structureDistanceCommand = new StructureDistanceCommand();
	private final ClearDisplayCommand clearDisplayCommand = new ClearDisplayCommand();
	private final GetSeedAndTeleportCommand getSeedAndTeleportCommand = new GetSeedAndTeleportCommand();

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
		LiteralArgumentBuilder<CommandSourceStack> structureBranch = Commands.literal("structure_util")
			.then(displayPiecesCommand.register())
			.then(clearDisplayCommand.register())
			.then(countLootCommand.register())
			.then(countTemplateCommand.register())
			.then(structureDistanceCommand.register());

		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("twilightforest")
			.executes(this::run)
			.then(centerCommand.register())
			.then(mapLocatorCommand.register())
			.then(conquerCommand.register())
			.then(generateBookCommand.register())
			.then(infoCommand.register())
			.then(mapBiomesCommand.register())
			.then(shieldCommand.register())
			.then(spawnerCommand.register(buildContext))
			.then(getSeedAndTeleportCommand.register(selection != Commands.CommandSelection.INTEGRATED))
			.then(structureBranch);
		LiteralCommandNode<CommandSourceStack> node = dispatcher.register(builder);
		dispatcher.register(Commands.literal("tf").executes(this::run).redirect(node));
		dispatcher.register(Commands.literal("tffeature").executes(this::run).redirect(node));
	}

	private int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		throw new SimpleCommandExceptionType(Component.translatable("commands.tffeature.usage", ctx.getInput())).create();
	}
}
