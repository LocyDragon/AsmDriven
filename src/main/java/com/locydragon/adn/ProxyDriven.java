package com.locydragon.adn;

import com.esotericsoftware.reflectasm.MethodAccess;
import org.bukkit.Bukkit;
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
	public ProxyDriven(CustomTimingsHandler handler, Class<?> eventClass,
					   MethodAccess access, Method method) {
		this.timings = handler;
		this.eventClass = eventClass;
		this.access = access;
		this.method = method;
	}
	@Override
	public void execute(Listener listener, Event event) throws EventException {
		try {
			if (!eventClass.isAssignableFrom(event.getClass())) {
				return;
			}
			boolean isAsync = event.isAsynchronous();
			if (!isAsync) {
				timings.startTiming();
			}
			access.invoke(listener, method.getName(), new Object[] {event});
			if (!isAsync) {
				timings.stopTiming();
			}
		} catch (Throwable t) {
			throw new EventException(t);
		}
	}
}
