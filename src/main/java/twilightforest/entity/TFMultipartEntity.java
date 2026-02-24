package twilightforest.entity;

/**
 * Replacement for the previous multipart entity support.
 *
 * <p>Multipart parents expose their {@link TFPart} instances via this interface so we can inject them
 * into rendering/lookup and synchronize them without relying on a loader-specific {@code PartEntity}.</p>
 */
public interface TFMultipartEntity {
	TFPart<?>[] getParts();
}
