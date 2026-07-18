package twilightforest.components.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GiantPickaxeMiningAttachment {
	public static final Codec<GiantPickaxeMiningAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.LONG.fieldOf("giantPickMining").forGetter(GiantPickaxeMiningAttachment::getMining),
		Codec.BOOL.fieldOf("giantPickBreaking").forGetter(GiantPickaxeMiningAttachment::getBreaking),
		Codec.INT.fieldOf("giantBlockConversion").forGetter(GiantPickaxeMiningAttachment::getGiantBlockConversion)
	).apply(instance, GiantPickaxeMiningAttachment::new));

	private long mining;
	private boolean breaking;
	private int giantBlockConversion;

	public GiantPickaxeMiningAttachment() {
	}

	private GiantPickaxeMiningAttachment(long mining, boolean breaking, int giantBlockConversion) {
		this.mining = mining;
		this.breaking = breaking;
		this.giantBlockConversion = giantBlockConversion;
	}

	public void setMining(long mining) {
		this.mining = mining;
	}

	public long getMining() { // Is block breaking with the use of a giant pickaxe occurring
		return this.mining;
	}

	public void setBreaking(boolean breaking) {
		this.breaking = breaking;
	}

	public boolean getBreaking() { // Is code in the process of breaking a 4x4x4 cube of blocks
		return this.breaking;
	}

	public void setGiantBlockConversion(int giantBlockConversion) {
		this.giantBlockConversion = giantBlockConversion;
	}

	public int getGiantBlockConversion() {
		return this.giantBlockConversion;
	}

	public boolean canMakeGiantBlock() { // Is code in the process of converting block drops to a giant block drop
		return this.giantBlockConversion > 0;
	}
}
