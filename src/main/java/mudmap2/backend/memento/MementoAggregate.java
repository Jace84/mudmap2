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

/**
 * A collection of event Originators
 * @author neop
 */
class MementoAggregate {

    /**
     * Timestamp of this Memento
     */
    private final long timestamp;

    /**
     * Memento of owner, if set
     */
    private Memento ownMemento = null;

    /**
     * References to Originators that have been changed within this timestamp
     */
    private final HashSet<Originator> modifiedOriginators = new HashSet<>();

    /**
     * Constructor
     * @param timestamp timestamp of first modification
     */
    public MementoAggregate(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the timestamp of first modification
     * @return timestamp of first modification
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get Memento of owner
     * @return Memento of owner
     */
    public Memento getOwnMemento() {
        return ownMemento;
    }

    /**
     * Set Memento of owner
     * @param ownMemento Memento of owner
     */
    public void setOwnMemento(final Memento ownMemento) {
        this.ownMemento = ownMemento;
    }

    /**
     * Add modified Originator
     * @param originator modified Originator
     */
    public void addModifiedOriginator(final Originator originator) {
        if(!modifiedOriginators.contains(originator)) {
            modifiedOriginators.add(originator);
        }
    }

    /**
     * Check whether any values have been set
     * @return true if own memento is not set and no modified Originators have been added
     */
    public boolean isEmpty() {
        return ownMemento == null && modifiedOriginators.isEmpty();
    }

    /**
     * Set modified Originators to this state
     */
    public void updateModifiedOriginators() {
        for(Originator originator: modifiedOriginators) {
            originator.goToTimestamp(timestamp);
        }
    }

    /**
     * Join MementoAggregates
     * @param other other aggregate to be joined into this one, will be cleared
     */
    protected void join(MementoAggregate other) {
        // join children
        for(Originator originator: other.modifiedOriginators) {
            originator.mementoJoin(other.getTimestamp(), getTimestamp());
        }

        // join other into this
        setOwnMemento(other.getOwnMemento());
        modifiedOriginators.addAll(other.modifiedOriginators);

        // clear other
        other.setOwnMemento(null);
        other.modifiedOriginators.clear();
    }

}
