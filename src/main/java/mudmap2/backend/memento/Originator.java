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
 * Originator base class of Memento pattern
 * @author neop
 */
public abstract class Originator {

    /**
     * All pop and push events will be notified to this listener
     */
    protected OriginatorListener originatorListener = null;

    /**
     * Saved states
     */
    protected final LinkedList<Memento> mementoRestored = new LinkedList<>();
    protected final LinkedList<Memento> mementoStored = new LinkedList<>();

    /**
     * "save state"
     */
    public void mementoPush(){
        /**
         * - try to create memento
         * - clear restored mementos
         * - add new memento
         * - announce new state
         */

        Memento memento = createMemento();
        if(memento != null){
            System.out.println(">> push");
            mementoStored.push(memento);
            mementoRestored.clear();

            if(originatorListener != null) {
                originatorListener.onMementoPush(this);
            }
        }
    }

    /**
     * "undo"
     */
    public void mementoRestore(){
        /**
         * Lock this method in case it's not called by its parent
         */
        if(originatorListener == null){
            /**
             * - remove last top stored element
             * - add it to restore elements
             */
            if(mementoCanRestore()) {
                System.out.println(">> restore, s: " + mementoStored.size() + ", r: " + mementoRestored.size());
                // apply Memento if it is no aggregate, then push list
                if(!(mementoStored.poll() instanceof AggregatingOriginator)) {
                    applyMemento(mementoStored.poll());
                }

                mementoRestored.push(mementoStored.pop());
            }
        }
    }

    /**
     * "redo"
     */
    public void mementoStore(){
        /**
         * Lock this method in case it's not called by its parent
         */
        if(originatorListener == null){
            /**
             * - remove last restored element
             * - add it to stored elements
             */
            if(mementoCanStore()) {
                System.out.println(">> store");
                // apply Memento if it is no aggregate, then push list
                if(!(mementoStored.poll() instanceof AggregatingOriginator)) {
                    applyMemento(mementoStored.poll());
                }

                mementoStored.push(mementoRestored.pop());
            }
        }
    }

    public boolean mementoCanRestore(){
        return !mementoStored.isEmpty();
    }

    public boolean mementoCanStore(){
        return !mementoRestored.isEmpty();
    }

    public void setOriginatorListener(OriginatorListener originatorListener) {
        this.originatorListener = originatorListener;
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

    /**
     * Clear events
     */
    public void mementoClear(){
        mementoRestored.clear();
        mementoStored.clear();
    }

}
