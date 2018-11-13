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
				pool.importPackage("com.esotericsoftware.reflectasm.MethodAccess");
				try {
					CtClass pluginClass = pool.getCtClass(className.replace("/", "."));
					CtMethod createListenerMethod =
							pluginClass.getDeclaredMethod("createRegisteredListeners"
									, new CtClass[] { pool.getCtClass("org.bukkit.event.Listener")
											, pool.getCtClass("org.bukkit.plugin.Plugin") });
					pluginClass.removeMethod(createListenerMethod);
					try {
						String code = "{ \n" +
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
								"          Method method = privateMethods[i];" +
								"        methods.add(method);\n" +
								" }\n"+
								"    } catch (NoClassDefFoundError e)\n" +
								"    {\n" +
								"      plugin.getLogger().severe(\"Plugin \" + plugin.getDescription().getFullName() + \" has failed to register events for \" + listener.getClass() + \" because \" + e.getMessage() + \" does not exist.\");\n" +
								"      return ret;\n" +
								"    }\n" +
								"    for (int k = 0;k < methods.size();k++) {\n" +
								"      final Method method = methods.get(k);"+
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
								"          for (Class clazz = eventClass;Event.class.isAssignableFrom(clazz);clazz = clazz.getSuperclass()) {\n" +
								"            if (clazz.getAnnotation(Deprecated.class) != null) {\n" +
								"              Warning warning = (Warning)clazz.getAnnotation(Warning.class);\n" +
								"              Warning.WarningState warningState = this.server.getWarningState();\n" +
								"              if (!warningState.printFor(warning)) {\n" +
								"                break;\n" +
								"              }\n" +
								"              plugin.getLogger().log(\n" +
								"                Level.WARNING, \n" +
								"                String.format(\n" +
								"                \"\\\"%s\\\" has registered a listener for %s on method \\\"%s\\\", but the event is Deprecated. \\\"%s\\\"; please notify the authors %s.\", new Object[] { \n" +
								"                plugin.getDescription().getFullName(), \n" +
								"                clazz.getName(), \n" +
								"                method.toGenericString(), \n" +
								"                (warning != null) && (warning.reason().length() != 0) ? warning.reason() : \"Server performance will be affected\", \n" +
								"                Arrays.toString(plugin.getDescription().getAuthors().toArray()) }), \n" +
								"                warningState == Warning.WarningState.ON ? new AuthorNagException(null) : null);\n" +
								"              break;\n" +
								"            }\n" +
								"          }\n" +
								"          MethodAccess access = MethodAccess.get(listener.getClass()); \n" +
								"          final CustomTimingsHandler timings = new CustomTimingsHandler(\"Plugin: \" + plugin.getDescription().getFullName() + \" Event: \" + listener.getClass().getName() + \"::\" + method.getName() + \"(\" + eventClass.getSimpleName() + \")\", pluginParentTimer);\n" +
								"          EventExecutor executor = new EventExecutor() {" +
								"            public void execute(Listener listener, Event event) throws EventException {" +
								"              try {" +
								"                if (!eventClass.isAssignableFrom(event.getClass())) {" +
								"                  return;" +
								"                }" +
								"                boolean isAsync = event.isAsynchronous();" +
								"                if (!isAsync) { timings.startTiming(); }" +
								"                access.invoke(listener, method.getName(), new Object[] { event });" +
								"                if (!isAsync) { timings.stopTiming(); } " +
								"              } catch (Throwable t) {" +
								"                throw new EventException(t);" +
								"              }" +
								"            }" +
								"          };" +
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
						createListenerMethod.setBody(code);
					} catch (CannotCompileException e) {
						e.printStackTrace();
					}
					try {
						pluginClass.addMethod(createListenerMethod);
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
