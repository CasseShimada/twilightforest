package twilightforest.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import twilightforest.block.entity.DryingRackBlockEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@WailaPlugin
public final class JadeCompat implements IWailaPlugin {
	private static final String CLIENT_REGISTRAR = "twilightforest.compat.jade.JadeClientCompat";

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(DryingRackDataProvider.INSTANCE, DryingRackBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		try {
			Class<?> registrarClass = Class.forName(CLIENT_REGISTRAR, true, JadeCompat.class.getClassLoader());
			Method register = registrarClass.getMethod("register", IWailaClientRegistration.class);
			register.invoke(null, registration);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
			throw new IllegalStateException("Unable to load the Twilight Forest Jade client registrar", exception);
		} catch (InvocationTargetException exception) {
			throw new IllegalStateException("The Twilight Forest Jade client registrar failed", exception.getCause());
		}
	}
}
