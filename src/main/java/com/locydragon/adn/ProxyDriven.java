package com.locydragon.adn;

import com.esotericsoftware.reflectasm.MethodAccess;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.spigotmc.CustomTimingsHandler;
import java.lang.reflect.Method;


/**
 * @author LocyDragon
 */
public class ProxyDriven implements EventExecutor {
	private CustomTimingsHandler timings;
	private Class<?> eventClass;
	private MethodAccess access;
	private Method method;
	private int name;
	public ProxyDriven(CustomTimingsHandler handler, Class<?> eventClass,
					   MethodAccess access, Method method) {
		this.timings = handler;
		this.eventClass = eventClass;
		this.access = access;
		this.method = method;
		String methodName = method.getName();
		this.name = access.getIndex(methodName);
	}
	@Override
	public void execute(Listener listener, Event event) throws EventException {
		try {
			if (!eventClass.isAssignableFrom(event.getClass())) {
				return;
			}
			boolean isSync = !event.isAsynchronous();
			if (isSync) {
				timings.startTiming();
			}
			access.invoke(listener, name, event);
			if (isSync) {
				timings.stopTiming();
			}
		} catch (Throwable t) {
			throw new EventException(t);
		}
	}
}
