package twilightforest.util.multiparts;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.TFMultipartEntity;
import twilightforest.entity.TFPart;

import java.util.Iterator;

public class MultipartEntityIteratorWrapper implements Iterator<Entity> {

	private final Iterator<Entity> delegate;
	private TFPart<?> @Nullable [] parts;
	private int partIndex;

	MultipartEntityIteratorWrapper(Iterator<Entity> iter) {
		this.delegate = iter;
	}

	@Override
	public boolean hasNext() {
		return parts != null || delegate.hasNext();
	}

	@Override
	public Entity next() {
		if (parts != null) {
			Entity next = parts[partIndex];
			partIndex++;
			if (partIndex >= parts.length)
				parts = null;
			return next;
		}
		Entity next = delegate.next();
		if (next instanceof TFMultipartEntity multipart) {
			TFPart<?>[] arr = multipart.getParts();
			if (arr != null) {
				int size = 0;
				for (TFPart<?> partEntity : arr) {
					if (partEntity != null)
						size++;
				}
				if (size > 0) {
					partIndex = 0;
					parts = new TFPart<?>[size];
					int index = 0;
					for (TFPart<?> partEntity : arr) {
						parts[index] = partEntity;
						index++;
					}
				}
			}
		}
		return next;
	}

	@Override
	public void remove() {
		if (parts == null || partIndex <= 0) {
			delegate.remove();
		} else {
			if (partIndex >= parts.length) {
				parts = null;
			} else {
				System.arraycopy(parts, partIndex, parts, partIndex - 1, parts.length - 1 - partIndex - 1);
			}
		}
	}

}
