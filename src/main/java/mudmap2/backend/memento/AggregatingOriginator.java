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

import java.util.LinkedList;

/**
 * Originator of Memento pattern, this Originator aggregates multiple events in one memento
 * @author neop
 */
public abstract class AggregatingOriginator implements OriginatorListener {

    /**
     * All pop and push events will be notified to this listener
     */
    protected OriginatorListener originatorListener = null;

    private boolean savedLatestState = false;

    /**
     * Saved states
     */
    protected final LinkedList<Memento> mementoRestored = new LinkedList<>();
    protected final LinkedList<Memento> mementoStored = new LinkedList<>();

    // wait 1000 ms before an aggregate is created
    private static final long AGGREGATE_WAITING_TIME = 1000;

    /**
     * "save state"
     */
    public void mementoPush(){
        if(onMementoPush(this)) {
            /**
             * do not push event if this is the uppermost originator:
             * this creates infinite loops. It will be handled by onMementoPush()
             */
            if(hasParent()) {
                mementoPush(createMemento());
            }
        }
    }

    private void mementoPush(Memento memento){
        /**
         * - clear restored mementos
         * - add new memento
         * - announce new state
         */
        if(memento != null){
            System.out.println(">> push, s: " + mementoStored.size() + ", r: " + mementoRestored.size() + ", " + this.toString());
            mementoStored.push(memento);
            mementoRestored.clear();
            savedLatestState = false;
        }
    }

    /**
     * "undo"
     */
    public void mementoRestore(){
        System.out.println(">> restore, s: " + mementoStored.size() + ", r: " + mementoRestored.size() + ", " + this.toString());

        if(mementoCanRestore()) {
            // restore stored events
            final Memento top = mementoStored.peek();

            // apply memento
            if(top instanceof MementoAggregate) {
                // aggregate
                ((MementoAggregate) top).restore();
            } else {
                // normal memento
                if(!savedLatestState) {
                    final Memento latestMemento = createMemento();
                    if(latestMemento != null) {
                        System.out.println(">> saved Latest, s: " + mementoStored.size() + ", r: " + mementoRestored.size() + ", " + this.toString());
                        mementoPush();
                    }
                    savedLatestState = true; // TODO: fix double undo issue by checking this variable
                }
                applyMemento(top);
            }

            /**
             * - remove last top stored element
             * - add it to restore elements
             */
            mementoRestored.push(mementoStored.pop());

            // if end is reached, go back one step
            if(mementoStored.isEmpty()) {
                mementoStore();
            }
        }
    }

    /**
     * "redo"
     */
    public void mementoStore(){
        System.out.println(">> store, s: " + mementoStored.size() + ", r: " + mementoRestored.size() + ", " + this.toString());

        if(mementoCanStore()) {
            // store restored events
            final Memento top = mementoRestored.peek();

            // apply memento
            if(top instanceof MementoAggregate) {
                // aggregate
                ((MementoAggregate) top).store();
            } else {
                // normal memento
                applyMemento(top);
            }

            /**
             * - remove last restored element
             * - add it to stored elements
             */
            mementoStored.push(mementoRestored.pop());
        }
    }

    /**
     * Clear events
     */
    public void mementoClear(){
        mementoRestored.clear();
        mementoStored.clear();

        System.out.println(">> clear, s: " + mementoStored.size() + ", r: " + mementoRestored.size() + ", " + this.toString());
    }

    public boolean mementoCanRestore(){
        return !mementoStored.isEmpty();
    }

    public boolean mementoCanStore(){
        return !mementoRestored.isEmpty();
    }

    private boolean hasParent(){
        return originatorListener != null;
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

    public void setOriginatorListener(OriginatorListener originatorListener) {
        this.originatorListener = originatorListener;
    }

    @Override
    public boolean onMementoPush(AggregatingOriginator source){
        if(hasParent()){
            // aggregate if parent is set
            return originatorListener.onMementoPush(source);
        } else {
            MementoAggregate currentAggregate = getCurrentAggregate();
            if(currentAggregate == null) {
                currentAggregate = new MementoAggregate();
                mementoPush(currentAggregate);
            }

            if(!currentAggregate.contains(source)) {
                if(source == this) {
                    currentAggregate.setOwnState(createMemento());
                } else {
                    currentAggregate.add(source);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private MementoAggregate getCurrentAggregate(){
        if(!mementoStored.isEmpty()) {
            final Memento top = mementoStored.peek();
            if(top instanceof MementoAggregate) {
                final MementoAggregate aggregate = (MementoAggregate) top;
                final long curTime = System.currentTimeMillis();
                if(curTime - aggregate.getFirstEventTime() <= AGGREGATE_WAITING_TIME){
                    return aggregate;
                }
            }
        }
        return null;
    }

}
