package clag;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.lang.reflect.*;
import java.util.*;

// based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/entitylist/EntityList.java


/*
* Used to override World.loadedTile/EntityList.
* */
public abstract class EntityList<T> extends ArrayList<T> {
    protected final ArrayList<T> innerList;
    protected final World world;
    private final Field overridenField;


    EntityList(World world, Field overriddenField) {
        this.overridenField = overriddenField;
        this.world = world;
        overriddenField.setAccessible(true);
        ArrayList<T> worldList = new ArrayList<T>();
        try {
            worldList = (ArrayList<T>) overriddenField.get(world);
            if (worldList.getClass() != ArrayList.class) {
                FMLLog.severe("CLag: Another mod has replaced an entity list with "+worldList.getClass());
            }
        } catch (Throwable t) {
            FMLLog.severe("CLag: Failed to get " + overriddenField.getName() + " in world " + (world));
        }
        innerList = worldList;
        try {
            overriddenField.set(world, this);
        } catch (Exception e) {
            throw new RuntimeException("CLag: Failed to override " + overriddenField.getName() + " in world " + (world), e);
        }
    }

    public boolean isProfiling ()
    {
        //EntityTickProfiler.profilingState != ProfileCommand.ProfilingState.NONE
        return true;
    }

    // see https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/util/contextaccess/ContextAccessSecurityManager.java
    public Class getContext(int depth) {
        // Broken on newer JDKs, automatically falls back to security manager implementation when this doesn't work.
        //noinspection deprecation
        return sun.reflect.Reflection.getCallerClass(depth + 2);

        // TODO: check if this might be broken, then we need the ContextAccessSecurityManager thing from nallar/TickProfiler

        //  extends SecurityManager
        // return getClassContext()[depth + 1];
    }

    public void unhook() throws IllegalAccessException {
        overridenField.set(world, innerList);
    }

    public abstract void tick();

    @Override
    public void trimToSize() {
        innerList.trimToSize();
    }

    @Override
    public void ensureCapacity(final int minCapacity) {
        innerList.ensureCapacity(minCapacity);
    }

    @Override
    public int size() {
        boolean tick = isProfiling() && World.class.isAssignableFrom(getContext(1));
        if (tick) {
            Class secondCaller = getContext(2);
            if (secondCaller == MinecraftServer.class || World.class.isAssignableFrom(secondCaller)) {
                doTick();
                return 0;
            }
        }
        return innerList.size();
    }

    private void doTick() {
        try {
            tick();
        } catch (Throwable t) {
            FMLLog.severe("CLag: Caught error while profiling in TP tick hook " + this, t);
        }
    }

    @Override
    public boolean isEmpty() {
        return innerList.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return innerList.contains(o);
    }

    @Override
    public int indexOf(final Object o) {
        return innerList.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return innerList.lastIndexOf(o);
    }

    @Override
    public Object clone() {
        return innerList.clone();
    }

    @Override
    public Object[] toArray() {
        return innerList.toArray();
    }

    @Override
    public <T1> T1[] toArray(final T1[] a) {
        return innerList.toArray(a);
    }

    @Override
    public T get(final int index) {
        return innerList.get(index);
    }

    @Override
    public T set(final int index, final T element) {
        return innerList.set(index, element);
    }

    @Override
    public boolean add(final T t) {
        return innerList.add(t);
    }

    @Override
    public void add(final int index, final T element) {
        innerList.add(index, element);
    }

    @Override
    public T remove(final int index) {
        return innerList.remove(index);
    }

    @Override
    public boolean remove(final Object o) {
        return innerList.remove(o);
    }

    @Override
    public void clear() {
        innerList.clear();
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        return innerList.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
        return innerList.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return innerList.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return innerList.retainAll(c);
    }

    @Override
    public ListIterator<T> listIterator(final int index) {
        return innerList.listIterator(index);
    }

    @Override
    public ListIterator<T> listIterator() {
        return innerList.listIterator();
    }

    @Override
    public Iterator<T> iterator() {
        boolean tick = isProfiling() && World.class.isAssignableFrom(getContext(1));
        if (tick) {
            Class secondCaller = getContext(2);
            if (secondCaller == MinecraftServer.class || World.class.isAssignableFrom(secondCaller)) {
                doTick();
                return Collections.<T>emptyList().iterator();
            }
        }
        return innerList.iterator();
    }

    @Override
    public List<T> subList(final int fromIndex, final int toIndex) {
        return innerList.subList(fromIndex, toIndex);
    }
}
