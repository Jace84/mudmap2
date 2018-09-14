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

/**
 * Originator of Memento pattern, this Originator aggregates multiple events in one memento
 * @author neop
 */
public abstract class AggregatingOriginator extends Originator implements OriginatorListener {

    // Only add events to aggregate if this is on top of stack
    private MementoAggregate currentAggregate = null;

    /**
     * Start a new aggregate
     */
    public void mementoAggregateBegin(){
        currentAggregate = new MementoAggregate();
        mementoStored.push(currentAggregate);
        mementoRestored.clear();

        // announce new aggregate
        if(originatorListener != null) {
            originatorListener.onMementoPush(this);
        }
    }

    /**
     * Close an aggregate
     */
    public void mementoAggregateEnd(){
        currentAggregate = null;
    }

    /**
     * "save state"
     */
    @Override
    public void mementoPush(){
        // if an aggregate is open: add to aggregate
        if(currentAggregate != null && mementoStored.peek() == currentAggregate) {
            // set own state if it has not been set yet
            if(!currentAggregate.hasOwnState()){
                currentAggregate.setOwnState(createMemento());
            }
        } else {
            currentAggregate = null;
            super.mementoPush();
        }
    }

    /**
     * "undo"
     */
    @Override
    public void mementoRestore(){
        currentAggregate = null;
        Memento restoreThis = mementoStored.peek();
        if(restoreThis != null && restoreThis instanceof MementoAggregate){
            // restore aggregate
            ((MementoAggregate) restoreThis).restore();
        }
        super.mementoRestore();
    }

    /**
     * "redo"
     */
    @Override
    public void mementoStore(){
        currentAggregate = null;
        Memento storeThis = mementoRestored.peek();
        if(storeThis != null && storeThis instanceof MementoAggregate){
            // store aggregate
            ((MementoAggregate) storeThis).store();
        }
        super.mementoStore();
    }

    /**
     * Receive originator event
     * @param source
     */
    @Override
    public void onMementoPush(Originator source) {
        if(currentAggregate != null && mementoStored.peek() == currentAggregate) {
            // if an aggregate is open: add to aggregate
            currentAggregate.add(source);
        } else if(originatorListener != null) {
            // if this Originator has a parent: forward event
            currentAggregate = null;
            originatorListener.onMementoPush(source);
        } else {
            // if this Originator has no parent and no open aggregate: add to memento list
            currentAggregate = null;
            mementoRestored.clear();
            // encapsulate event in Aggregate so it is handled as memento
            mementoStored.add(new MementoAggregate(source));
        }
    }

}
