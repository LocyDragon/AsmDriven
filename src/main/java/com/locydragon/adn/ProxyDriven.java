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
	protected Class<?> eventClass;
	private MethodAccess access;
	private int name;
	public ProxyDriven(CustomTimingsHandler handler, Class<?> eventClass,
					   MethodAccess access, Method method) {
		this.timings = handler;
		this.eventClass = eventClass;
		this.access = access;
		String methodName = method.getName();
		this.name = access.getIndex(methodName);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		try {
			if (!this.eventClass.isAssignableFrom(event.getClass())) {
				return;
			}
			access.invoke(listener, name, event);
		} catch (Throwable t) {
			throw new EventException(t);
		}
	}

}
