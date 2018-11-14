package com.locydragon.adn;

import com.esotericsoftware.reflectasm.MethodAccess;
import javassist.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class AsmDriven {
	public static MethodAccess NULL_ARGMENT;
	public static final String targetLocation = "org/bukkit/plugin/java/JavaPluginLoader";
	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
			if (className.equals(targetLocation)) {
				ClassPool pool = ClassPool.getDefault();
				{
					pool.importPackage("com.esotericsoftware.reflectasm.MethodAccess");
					pool.importPackage("com.locydragon.adn.ProxyDriven");
					pool.importPackage("org.apache.commons.lang.Validate");
					pool.importPackage("java.util.Map");
					pool.importPackage("java.util.HashMap");
					pool.importPackage("java.lang.reflect.Method");
					pool.importPackage("java.util.Set");
					pool.importPackage("java.util.HashSet");
					pool.importPackage("org.bukkit.event.EventHandler");
					pool.importPackage("org.bukkit.event.Event");
					pool.importPackage("org.bukkit.*");
					pool.importPackage("org.bukkit.plugin.AuthorNagException");
					pool.importPackage("org.spigotmc.CustomTimingsHandler");
					pool.importPackage("org.bukkit.plugin.EventExecutor");
					pool.importPackage("org.bukkit.plugin.RegisteredListener");
					pool.importPackage("org.bukkit.event.Listener");
					pool.importPackage("org.bukkit.plugin.Plugin");
					pool.importPackage("java.util.Iterator");
				}
				try {
					CtClass pluginClass = pool.getCtClass(className.replace("/", "."));
					CtMethod createListenerMethod =
							pluginClass.getDeclaredMethod("createRegisteredListeners"
									, new CtClass[] { pool.getCtClass("org.bukkit.event.Listener")
											, pool.getCtClass("org.bukkit.plugin.Plugin") });
					pluginClass.removeMethod(createListenerMethod);
					try {
						String code = "public Map createRegisteredListeners(Listener listener, Plugin plugin)" +
								"{ \n" +
								"    Validate.notNull(plugin, \"Plugin can not be null\");\n" +
								"    Validate.notNull(listener, \"Listener can not be null\");\n" +
								"\n" +
								"    this.server.getPluginManager().useTimings();\n" +
								"    Map ret = new HashMap();\n" +
								"    Method[] privateMethods;\n" +
								"Set methods = null;\n" +
								"    try { Method[] publicMethods = listener.getClass().getMethods();\n" +
								"      privateMethods = listener.getClass().getDeclaredMethods();\n" +
								"      methods = new HashSet(publicMethods.length + privateMethods.length, 1.0F);\n" +
								"      for (int i = 0;i < publicMethods.length;i++) {\n" +
								"           Method method = publicMethods[i];" +
								"        methods.add(method);\n" +
								"      }\n" +
								"      for (int j = 0;j < privateMethods.length;j++) {\n" +
								"          Method method = privateMethods[j];" +
								"        methods.add(method);\n" +
								" }\n"+
								"    } catch (NoClassDefFoundError e)\n" +
								"    {\n" +
								"      plugin.getLogger().severe(\"Plugin \" + plugin.getDescription().getFullName() + \" has failed to register events for \" + listener.getClass() + \" because \" + e.getMessage() + \" does not exist.\");\n" +
								"      return ret;\n" +
								"    }\n" +
								"    Iterator iterator = methods.iterator();\n" +
								"    while (iterator.hasNext()) {\n" +
								"    Method method = (Method)iterator.next();" +
								"      EventHandler eh = (EventHandler)method.getAnnotation(EventHandler.class);\n" +
								"      if (eh != null)\n" +
								"      {\n" +
								"        Class checkClass = method.getParameterTypes()[0];\n" +
								"        if ((method.getParameterTypes().length != 1) || (!Event.class.isAssignableFrom(checkClass))) {\n" +
								"          plugin.getLogger().severe(plugin.getDescription().getFullName() + \" attempted to register an invalid EventHandler method signature \\\"\" + method.toGenericString() + \"\\\" in \" + listener.getClass());\n" +
								"        }\n" +
								"        else\n" +
								"        {\n" +
								"          final Class eventClass = checkClass.asSubclass(Event.class);\n" +
								"          method.setAccessible(true);\n" +
								"          Set eventSet = (Set)ret.get(eventClass);\n" +
								"          if (eventSet == null) {\n" +
								"            eventSet = new HashSet();\n" +
								"            ret.put(eventClass, eventSet);\n" +
								"          }\n" +
								"          MethodAccess access = MethodAccess.get(listener.getClass()); \n" +
								"          final CustomTimingsHandler timings = new CustomTimingsHandler(\"Plugin: \" + plugin.getDescription().getFullName() + \" Event: \" + listener.getClass().getName() + \"::\" + method.getName() + \"(\" + eventClass.getSimpleName() + \")\", pluginParentTimer);\n" +
								"          EventExecutor executor = new ProxyDriven(timings, eventClass, access, method);\n" +
								"          ((Set)eventSet).add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));\n" +
								"        }\n" +
								"      }\n" +
								"    }\n" +
								"    return ret; \n" +
								"}";
						File codes = new File("C:\\Users\\Administrator\\Desktop\\Driven.yml");
						if (!codes.exists()) {
							try {
								codes.createNewFile();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						try {
							FileWriter writer = new FileWriter(codes);
							writer.write(code);
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						CtMethod newMethod = CtNewMethod.make(code, pluginClass);
						pluginClass.addMethod(newMethod);
						//createListenerMethod.setBody(code);
					} catch (CannotCompileException e) {
						e.printStackTrace();
					}
					try {
						return pluginClass.toBytecode();
					} catch (IOException | CannotCompileException e) {
						e.printStackTrace();
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				}
			}
			return null;
		});
	}
}
