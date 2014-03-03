package clag;

// based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/util/contextaccess/ContextAccess.java
public interface ContextAccess {
	public static final ContextAccess $ = ContextAccessProvider.getContextAccess();
	
	public Class getContext(int depth);
}
