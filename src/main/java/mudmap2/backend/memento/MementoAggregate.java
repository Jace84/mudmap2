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
    private final List<AggregatingOriginator> events = new LinkedList<>();

    /**
     * State of own Originator
     */
    private Memento ownState = null;

    /**
     * Time of first event in ms
     */
    private long firstEventTime = 0;

    public MementoAggregate(){
    }

    public MementoAggregate(AggregatingOriginator event){
        events.add(event);
    }

    /**
     * Add Originator event to list
     * @param event Originator that threw this event
     */
    public void add(AggregatingOriginator event){
        if(firstEventTime == 0) {
            firstEventTime = System.currentTimeMillis();
        }
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
        for(AggregatingOriginator event: events){
            event.mementoStore();
        }
    }

    /**
     * Restore all Originators event
     */
    public void restore(){
        for(AggregatingOriginator event: events){
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

    /**
     * Check whether list contains source
     * @param source event source
     * @return true if list contains source
     */
    public boolean contains(AggregatingOriginator source){
        return events.contains(source);
    }

    /**
     * Get list of events
     * @return list of events
     */
    protected List<AggregatingOriginator> getList(){
        return events;
    }

    public long getFirstEventTime() {
        return firstEventTime;
    }

}
