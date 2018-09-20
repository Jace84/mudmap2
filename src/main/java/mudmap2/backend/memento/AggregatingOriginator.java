/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2018  Neop (email: mneop@web.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package mudmap2.backend.memento;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * Originator of Memento pattern, this Originator aggregates multiple events in one memento
 * @author neop
 */
public abstract class AggregatingOriginator {

    // wait 1000 ms before an aggregate is created
    private static final long WAITING_TIME = 1000;

    /**
     * Saved states
     */
    private final LinkedList<Key> events = new LinkedList<>();
    private long currentTimeKey = 0;

    /**
     * All pop and push events will be notified to this listener
     */
    protected AggregatingOriginator parent = null;

    private boolean savedLatestState = false;

    /**
     * "save state"
     */
    public void mementoPush(){
        final long timeKey = onMementoPush(this);

        // remove all keys ahead of currentTimeKey
        while(!events.isEmpty() && events.peek().getTimestamp() > currentTimeKey) {
            events.pop();
        }
        currentTimeKey = timeKey;

        // create memento
        final Memento memento = createMemento();
        mementoPush(this, memento);
    }

    private void mementoPush(AggregatingOriginator originator, Memento memento){
        // get key or create a new one
        Key currentKey;
        if(!events.isEmpty() && events.peek().getTimestamp() == currentTimeKey) {
            currentKey = events.peek();
        } else {
            currentKey = new Key(currentTimeKey);
            events.add(currentKey);
        }

        System.out.println(">> push " + events.size() + ", " + this.toString());

        // add memento to key
        currentKey.add(originator, memento);
    }

    private int getKeyIdx(final long timeKey){
        // find current key
        int keyIdx = 0;
        for(;keyIdx < events.size(); ++keyIdx) {
            if(events.get(keyIdx).getTimestamp() == timeKey) {
                break;
            }
        }
        return keyIdx;
    }

    private void setKey(final long timestamp){
        // don't do anything if history is empty or if timestamp is equal
        if(events.isEmpty() || timestamp == currentTimeKey) {
            return;
        }

        // find current key
        int keyIdx = getKeyIdx(currentTimeKey);

        // apply keys
        long lastKeyIdx = currentTimeKey;
        if(timestamp > currentTimeKey) {
            for(;keyIdx < events.size(); ++keyIdx) {
                Key event = events.get(keyIdx);
                if(event.getTimestamp() <= timestamp) {
                    event.apply();
                    lastKeyIdx = event.getTimestamp();
                } else {
                    System.out.println(">> set from " + lastKeyIdx + " to " + lastKeyIdx);
                    break;
                }
            }
        } else if(timestamp < currentTimeKey) {
            for(;keyIdx >= 0; --keyIdx) {
                Key event = events.get(keyIdx);
                if(event.getTimestamp() >= timestamp) {
                    event.apply();
                    lastKeyIdx = event.getTimestamp();
                } else {
                    System.out.println(">> set from " + lastKeyIdx + " to " + lastKeyIdx);
                    break;
                }
            }
        }
        currentTimeKey = lastKeyIdx;
    }

    /**
     * "undo"
     */
    public void mementoRestore(){
        // find current key
        int keyIdx = getKeyIdx(currentTimeKey);

        System.out.println(">> restore " + keyIdx + " / " + events.size() + ", " + this.toString());

        if(keyIdx > 0) {
            setKey(events.get(keyIdx - 1).getTimestamp());
        }
    }

    /**
     * "redo"
     */
    public void mementoStore(){
        // find current key
        int keyIdx = getKeyIdx(currentTimeKey);

        System.out.println(">> store " + keyIdx + " / " + events.size() + ", " + this.toString());

        if(keyIdx < events.size()-1) {
            setKey(events.get(keyIdx + 1).getTimestamp());
        }
    }

    /**
     * Clear events
     */
    public void mementoClear(){
        events.clear();
    }

    public boolean mementoCanRestore(){
        // find current key
        int keyIdx = getKeyIdx(currentTimeKey);
        return keyIdx > 0;
    }

    public boolean mementoCanStore(){
        // find current key
        int keyIdx = getKeyIdx(currentTimeKey);
        return keyIdx < events.size()-1;
    }

    public void setParent(AggregatingOriginator originatorListener) {
        this.parent = originatorListener;
    }

    private boolean hasParent(){
        return parent != null;
    }

    /**
     * Store data in Memento object
     * @return
     */
    protected abstract Memento createMemento();

    /**
     * Apply data stored in Memento
     * @param memento
     */
    protected abstract void applyMemento(Memento memento);

    public long onMementoPush(AggregatingOriginator source){
        if(hasParent()){
            // aggregate if parent is set
            return parent.onMementoPush(source);
        } else {
            final long curTime = System.currentTimeMillis();
            if(curTime - currentTimeKey > WAITING_TIME) {
                currentTimeKey = curTime;
            }
            mementoPush(source, null);
            return currentTimeKey;
        }
    }

    private class Key {

        private final long timestamp;
        private final HashMap<AggregatingOriginator, Memento> events = new HashMap<>();

        public Key(long timestamp) {
            this.timestamp = timestamp;
        }

        public void add(AggregatingOriginator originator, Memento memento) {
            if(!events.containsKey(originator)) {
                events.put(originator, memento);
            }
        }

        public long getTimestamp() {
            return timestamp;
        }

        public HashMap<AggregatingOriginator, Memento> getEvents() {
            return events;
        }

        private void apply() {
            for(Entry<AggregatingOriginator, Memento> entry: events.entrySet()) {
                if(entry.getValue() != null) {
                    entry.getKey().applyMemento(entry.getValue());
                } else {
                    entry.getKey().setKey(timestamp);
                }
            }
        }

    }

}
