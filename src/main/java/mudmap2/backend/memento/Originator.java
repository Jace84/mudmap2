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

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Originator of Memento pattern, this Originator aggregates multiple events in one memento
 * @author neop
 */
public abstract class Originator {

    // wait 1000 ms before an aggregate is created
    private static final long WAITING_TIME = 1000;

    // history
    private final LinkedList<MementoAggregate> history = new LinkedList<>();
    private int curHistoryIdx = 0;

    // modified Originators
    private final HashSet<Originator> modified = new HashSet<>();

    private Originator parent;
    private boolean enabled = true;

    // memento event listeners for external observers (ui etc)
    private final HashSet<Listener> listeners = new HashSet<>();

    public Originator() {
        parent = null;
    }

    /**
     * Save state
     */
    public void mementoPush() {
        if(isMementoParentEnabled()) {
            truncateHistory();

            // check whether the existing entry is recent
            MementoAggregate entry = null;
            final long curTime = getParentTimestamp();
            if(!history.isEmpty() && curTime - history.getLast().getTimestamp() < WAITING_TIME) {
                entry = history.getLast();
                if(entry.getOwnMemento() == null) {
                    entry.setOwnMemento(createMemento());
                }
            }

            // create entry and add to end of list
            if(entry == null) {
                entry = new MementoAggregate(curTime);
                entry.setOwnMemento(createMemento());
                saveModified(entry);
                history.add(entry);
            }

            // reset index
            curHistoryIdx = history.size()-1;

            // notify parent
            if(hasParent()) {
                notifyChange(this, curTime);
            } else if(!modified.contains(this)) {
                modified.add(this);
                callListeners();
            }
        }
    }

    /**
     * Undo state
     */
    public void mementoRestore() {
        if(hasParent()) {
            parent.mementoRestore();
        } else {
            // save unregistered changes
            if(!modified.isEmpty()) {
                truncateHistory();

                final long currentTimestamp = getCurrentTimestamp();
                final long curTime = System.currentTimeMillis();
                long time = curTime;

                while(time == currentTimestamp) {
                    time++;
                }

                final MementoAggregate entry = new MementoAggregate(time);
                saveModified(entry);
                history.add(entry);
                curHistoryIdx = history.size()-1;
            }

            // move history index
            if(curHistoryIdx > 0) {
                --curHistoryIdx;
            }

            // apply state
            if(history.size() >= curHistoryIdx+1) {
                applyMementoAggregate(history.get(curHistoryIdx));
            }

            callListeners();
        }
    }

    /**
     * Redo state
     */
    public void mementoStore() {
        if(hasParent()) {
            parent.mementoStore();
        } else {
            // go forward and apply state
            if(curHistoryIdx+1 < history.size()) {
                applyMementoAggregate(history.get(++curHistoryIdx));
            }

            callListeners();
        }
    }

    /**
     * Check whether the state can be restored
     * @return true if the state can be restored
     */
    public boolean canRestore() {
        return !history.isEmpty();
    }

    /**
     * Check whether the state can be stored
     * @return true if the state can be stored
     */
    public boolean canStore() {
        return history.size() > curHistoryIdx+1;
    }

    /**
     * Save current state without announcing it to the parent
     * @param timestamp
     */
    private void saveState(final long timestamp) {
        truncateHistory();

        // check whether the existing entry is recent
        MementoAggregate entry = null;
        if(!history.isEmpty() && history.getLast().getTimestamp() == timestamp) {
            entry = history.getLast();
            if(entry.getOwnMemento() == null) {
                entry.setOwnMemento(createMemento());
            }
        }

        // create entry and add to end of list
        if(entry == null) {
            entry = new MementoAggregate(timestamp);
            entry.setOwnMemento(createMemento());
            history.add(entry);
        }

        // reset index
        curHistoryIdx = Math.max(0, history.size()-1);
    }

    /**
     * Save modified Originators
     * @param target Memento that the changed Originators will be added to
     */
    private void saveModified(MementoAggregate target) {
        if(!modified.isEmpty()) {
            for(Originator originator: modified) {
                if(originator != this) {
                    originator.saveState(target.getTimestamp());
                    target.addModifiedOriginator(originator);
                } else {
                    target.setOwnMemento(createMemento());
                }
            }
            modified.clear();
        }
    }

    /**
     * op all entries after the current one
     */
    private void truncateHistory(){
        while(history.size() > curHistoryIdx+1) {
            history.pollLast();
        }
    }

    /**
     * Set a parent originator
     * @param parent
     */
    public void setMementoParent(Originator parent) {
        if(parent != this) {
            this.parent = parent;
        } else {
            System.err.println("Tried to set parent originator to itself on " + toString());
        }
    }

    /**
     * Check whether a parent is set
     * @return true if a parent is set
     */
    private boolean hasParent() {
        return parent != null;
    }

    /**
     * Enable parent
     * @param enabled true to enable, false to disable memento creation
     */
    public void setMementoParentEnabled(final boolean enabled) {
        if(hasParent()) {
            parent.setMementoParentEnabled(enabled);
        } else {
            this.enabled = enabled;
        }
    }

    /**
     * Ask parent whether mementos should be created
     * @return
     */
    public boolean isMementoParentEnabled() {
        if(hasParent()) {
            return parent.isMementoParentEnabled();
        } else {
            return enabled;
        }
    }

    /**
     * Get current timestamp of parent
     * @return
     */
    private long getParentTimestamp() {
        if(hasParent()) {
            return parent.getParentTimestamp();
        } else {
            return getNextTimestamp();
        }
    }

    /**
     * Get next timestamp to be used
     * @return
     */
    private long getNextTimestamp(){
        final long currentTimestamp = getCurrentTimestamp();
        final long curTime = System.currentTimeMillis();
        if(curTime - currentTimestamp < WAITING_TIME) {
            return currentTimestamp;
        } else {
            return curTime;
        }
    }

    /**
     * Get current timestamp
     * @return timestamp of history event pointed at or 0 if history is empty
     */
    private long getCurrentTimestamp() {
        if(history.size() > curHistoryIdx) {
            return history.get(curHistoryIdx).getTimestamp();
        } else {
            return 0;
        }
    }

    /**
     * Store or restore all mementos to reach the given timestamp
     * @param timestamp
     */
    protected void goToTimestamp(final long timestamp) {
        if(!history.isEmpty()) {
            /**
             * - Save current state if changed but not saved yet: handled by mementoRestore()
             * - Find memento that is described by this timestamp
             * - go back or forth step by step
             */

            // find target index
            long currentTimestamp = getCurrentTimestamp();
            if(currentTimestamp == 0) {
                // history empty or invalid index: set last index
                if(!history.isEmpty()) {
                    applyMementoAggregate(history.getLast());
                    curHistoryIdx = history.size()-1;
                }
            } else if(currentTimestamp >= timestamp) {
                // go back in time: restore
                while(history.get(curHistoryIdx).getTimestamp() >= timestamp) {
                    applyMementoAggregate(history.get(curHistoryIdx));
                    if(curHistoryIdx > 0) {
                        --curHistoryIdx;
                    } else {
                        break;
                    }
                }
            } else if(currentTimestamp < timestamp) {
                // go forward in time: store
                while(history.get(curHistoryIdx).getTimestamp() < timestamp) {
                    if(curHistoryIdx < history.size()-1) {
                        applyMementoAggregate(history.get(++curHistoryIdx));
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Notify parent that a child changed
     * @param modifiedOriginator modified Originator
     * @param currentTime timestamp as given by getParentTimestamp()
     */
    private void notifyChange(final Originator modifiedOriginator, final long currentTime) {
        if(hasParent()) {
            parent.notifyChange(modifiedOriginator, currentTime);
        } else {
            /**
             * - get current aggregate if available
             * - or create new aggregate
             * - then add modifiedOriginator to it
             */

            truncateHistory();

            // find current aggregate or create a new one
            MementoAggregate curAggregate;
            final long currentTimestamp = getCurrentTimestamp();
            if(currentTimestamp > 0 && currentTime == currentTimestamp) {
                // if current aggregate exists and is recent
                curAggregate = history.get(curHistoryIdx);
            } else {
                // if no aggregate exists or is not recent
                curAggregate = new MementoAggregate(currentTime);
                history.add(curAggregate);
                curHistoryIdx = history.size()-1;
            }

            // add modifiedOriginator to aggregate
            curAggregate.addModifiedOriginator(modifiedOriginator);

            // add to modified list
            if(!modified.contains(modifiedOriginator)) {
                modified.add(modifiedOriginator);
            }

            callListeners();
        }
    }

    /**
     * Create instance of Memento containing a copy of the object's current values
     * @return Memento
     */
    protected abstract Memento createMemento();

    /**
     * Apply values of Memento
     * @param memento Memento containing the values to be set
     */
    protected abstract void applyMemento(final Memento memento);

    /**
     * Apply MementoAggregate
     * @param memento aggregate to apply
     */
    private void applyMementoAggregate(MementoAggregate memento) {
        if(memento.getOwnMemento() != null) {
            applyMemento(memento.getOwnMemento());
        }
        memento.updateModifiedOriginators();
    }

    /**
     * Add memento event listener (eg for UI observer)
     * @param listener event listener
     */
    public void addMementoListener(Listener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove memento event listener
     * @param listener event listener
     */
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Call memento event listeners
     */
    private void callListeners() {
        for(Listener listener: listeners) {
            listener.onMementoEvent(this);
        }
    }

    public interface Listener {

        void onMementoEvent(final Object source);

    }
}
