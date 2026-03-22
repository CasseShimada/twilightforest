package tamaized.beanification.junit;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Minimal local replacement for the upstream test helper.
 *
 * <p>Current tests use this only as a marker extension and do not rely on
 * beanification-specific setup, so a no-op implementation keeps behavior
 * intact while avoiding the remote test-only dependency.</p>
 */
public final class MockitoFixer implements BeforeEachCallback {

	@Override
	public void beforeEach(ExtensionContext context) {
		// No-op by design.
	}
}
