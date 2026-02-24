package twilightforest.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.List;

public class OptifineWarningScreen extends Screen {

	private final Screen lastScreen;
	private int ticksUntilEnable = 20 * 10;
	private List<FormattedCharSequence> messageLines = List.of();
	private List<FormattedCharSequence> suggestionLines = List.of();
	private static final Component text = Component.translatable("gui.twilightforest.optifine.message");
	private static final MutableComponent url = Component.translatable("gui.twilightforest.optifine.suggestions").withStyle(style -> style
		.withColor(ChatFormatting.GREEN)
		.applyFormat(ChatFormatting.UNDERLINE)
		.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/NordicGamerFE/usefulmods"))));
	private Button exitButton;

	public OptifineWarningScreen(Screen screen) {
		super(Component.translatable("gui.twilightforest.optifine.title"));
		this.lastScreen = screen;
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), text);
	}

	@Override
	protected void init() {
		super.init();
		this.exitButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_PROCEED, (pressed) -> Minecraft.getInstance().setScreen(this.lastScreen)).bounds(this.width / 2 - 75, this.height * 3 / 4, 150, 20).build());
		this.exitButton.active = false;

		this.messageLines = this.font.split(text, this.width - 50);
		this.suggestionLines = this.font.split(url, this.width - 50);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 16777215);
		renderCenteredLines(graphics, this.messageLines, this.width / 2, 70);
		renderCenteredLines(graphics, this.suggestionLines, this.width / 2, 160);
		super.render(graphics, mouseX, mouseY, partialTicks);

		this.exitButton.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();
		if (--this.ticksUntilEnable <= 0) {
			this.exitButton.active = true;
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return this.ticksUntilEnable <= 0;
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(this.lastScreen);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean canceled) {
		double mouseX = event.x();
		double mouseY = event.y();
		if (mouseY > 160 && mouseY < 170) {
			Style style = this.getClickedComponentStyleAt(mouseX);
			ClickEvent clickEvent = style != null ? style.getClickEvent() : null;
			if (clickEvent instanceof ClickEvent.OpenUrl openUrl) {
				Screen.clickUrlAction(Minecraft.getInstance(), this, openUrl.uri());
				return true;
			}
		}

		return super.mouseClicked(event, canceled);
	}

	private void renderCenteredLines(GuiGraphics graphics, List<FormattedCharSequence> lines, int centerX, int startY) {
		int y = startY;
		for (FormattedCharSequence line : lines) {
			graphics.drawCenteredString(this.font, line, centerX, y, 0xFFFFFF);
			y += this.font.lineHeight;
		}
	}

	@Nullable
	private Style getClickedComponentStyleAt(double xPos) {
		int wid = Minecraft.getInstance().font.width(url);
		int left = this.width / 2 - wid / 2;
		int right = this.width / 2 + wid / 2;
		return xPos >= left && xPos <= right ? url.getStyle() : null;
	}
}
