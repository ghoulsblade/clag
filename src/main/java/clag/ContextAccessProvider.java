package clag;

import cpw.mods.fml.common.FMLLog;

// based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/util/contextaccess/ContextAccessProvider.java

class ContextAccessProvider {
	private static final Class[] contextAccessClasses = {
			//ContextAccessReflection.class, // does not work in eclipse
			ContextAccessSecurityManager.class,
	};

	static ContextAccess getContextAccess() {
		for (Class<?> clazz : contextAccessClasses) {
			try {
				ContextAccess contextAccess = (ContextAccess) clazz.newInstance();
				Class<?> currentClass = contextAccess.getContext(0);
				if (currentClass != ContextAccessProvider.class) {
					StringBuilder sb = new StringBuilder();
					sb.append("Stack:\n");
					for (int i = -2; i < 3; i++) {
						try {
							sb.append(contextAccess.getContext(i).getName()).append(" at ").append(i).append('\n');
						} catch (Throwable ignored) {
						}
					}
					throw new Error("Wrong class returned: " + currentClass + ", expected ContextAccessProvider. " + sb);
				}
				return contextAccess;
			} catch (Throwable t) {
				FMLLog.info("Unable to set up context access class " + clazz + ". " + t.getMessage() + ", falling back to slower context access. On JRE: " + System.getProperty("java.version"));				
			}
		}
		throw new Error("Failed to set up any context access");
	}
}
