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
import java.util.List;

/**
 * A collection of event Originators
 * @author neop
 */
public class MementoAggregate implements Memento {

    /**
     * List of Originator events
     */
    private final List<Originator> events = new LinkedList<>();

    /**
     * State of own Originator
     */
    private Memento ownState = null;

    public MementoAggregate(){
    }

    public MementoAggregate(Originator event){
        events.add(event);
    }

    /**
     * Add Originator event to list
     * @param event Originator that threw this event
     */
    public void add(Originator event){
        events.add(event);
    }

    /**
     * Check whether any events have been added
     * @return true if there are no events in list
     */
    public boolean isEmpty(){
        return events.isEmpty();
    }

    /**
     * store all Originators event
     */
    public void store(){
        for(Originator event: events){
            event.mementoStore();
        }
    }

    /**
     * Restore all Originators event
     */
    public void restore(){
        for(Originator event: events){
            event.mementoRestore();
        }
    }

    /**
     * Get state of own Originator
     * @return Memento or null
     */
    public Memento getOwnState() {
        return ownState;
    }

    /**
     * Set state of own Originator
     * @param ownState
     */
    public void setOwnState(Memento ownState) {
        this.ownState = ownState;
    }

    /**
     * Check whether ownState has been set
     * @return true if ownState has been set
     */
    public boolean hasOwnState(){
        return ownState != null;
    }

}
