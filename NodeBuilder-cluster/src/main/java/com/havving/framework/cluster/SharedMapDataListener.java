package com.havving.framework.cluster;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.MapEvent;
import com.hazelcast.map.listener.*;

/**
 * @author HAVVING
 * @since 2021-05-02
 */
public abstract class SharedMapDataListener<T> implements EntryAddedListener<String, T>,
        EntryRemovedListener<String, T>,
        EntryUpdatedListener<String, T>,
        EntryEvictedListener<String, T>,
        MapEvictedListener,
        MapClearedListener {

    @Override
    public abstract void entryAdded(EntryEvent<String, T> event);

    @Override
    public abstract void entryEvicted(EntryEvent<String, T> event);

    @Override
    public abstract void entryRemoved(EntryEvent<String, T> event);

    @Override
    public abstract void entryUpdated(EntryEvent<String, T> event);

    @Override
    public abstract void mapCleared(MapEvent event);

    @Override
    public abstract void mapEvicted(MapEvent event);
}
