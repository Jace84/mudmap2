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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author neop
 */
public class MementoAggregateTest {

    /**
     * Test of getTimestamp method, of class MementoAggregate.
     */
    @Test
    public void testGetTimestamp() {
        System.out.println("getTimestamp");
        
        long expResult = System.currentTimeMillis();
        MementoAggregate instance = new MementoAggregate(expResult);
        long result = instance.getTimestamp();
        assertEquals(expResult, result);
        
        long expResult2 = 0L;
        MementoAggregate instance2 = new MementoAggregate(expResult2);
        long result2 = instance2.getTimestamp();
        assertEquals(expResult2, result2);
        
        long expResult3 = Long.MAX_VALUE;
        MementoAggregate instance3 = new MementoAggregate(expResult3);
        long result3 = instance3.getTimestamp();
        assertEquals(expResult3, result3);
        
        long expResult4 = Long.MIN_VALUE;
        MementoAggregate instance4 = new MementoAggregate(expResult4);
        long result4 = instance4.getTimestamp();
        assertEquals(expResult4, result4);
    }

    /**
     * Test of getOwnMemento method, of class MementoAggregate.
     */
    @Test
    public void testGetOwnMemento() {
        System.out.println("getOwnMemento");
        
        MementoAggregate instance = new MementoAggregate(0L);
        Memento expResult = null;
        Memento result = instance.getOwnMemento();
        assertEquals(expResult, result);
    }

    /**
     * Test of setOwnMemento method, of class MementoAggregate.
     */
    @Test
    public void testSetOwnMemento() {
        System.out.println("setOwnMemento");
        
        Memento ownMemento = new Memento() {};
        MementoAggregate instance = new MementoAggregate(0L);
        assertNull(instance.getOwnMemento());
        
        instance.setOwnMemento(ownMemento);
        assertEquals(ownMemento, instance.getOwnMemento());
    }

    /**
     * Test of addModifiedOriginator method, of class MementoAggregate.
     */
    @Test
    public void testAddModifiedOriginator() {
        System.out.println("addModifiedOriginator");
        
        Originator originator1 = new Originator() {
            @Override
            protected Memento createMemento() {
                return null;
            }

            @Override
            protected void applyMemento(Memento memento) {}
        };
        
        Originator originator2 = new Originator() {
            @Override
            protected Memento createMemento() {
                return null;
            }

            @Override
            protected void applyMemento(Memento memento) {}
        };
        
        MementoAggregate instance = new MementoAggregate(0L);
        try {
            // access internal field by reflection
            Field field = MementoAggregate.class.getDeclaredField("modifiedOriginators");
            field.setAccessible(true);
            
            HashSet<Originator> modifiedOriginators = (HashSet) field.get(instance);
            assertNotNull(modifiedOriginators);
            
            assertTrue(modifiedOriginators.isEmpty());
            
            // add originator1
            instance.addModifiedOriginator(originator1);
            assertEquals(1, modifiedOriginators.size());
            assertTrue(modifiedOriginators.contains(originator1));
            
            // add originator1 again: no double entries
            instance.addModifiedOriginator(originator1);
            assertEquals(1, modifiedOriginators.size());
            assertTrue(modifiedOriginators.contains(originator1));
            
            // add originator2
            instance.addModifiedOriginator(originator2);
            assertEquals(2, modifiedOriginators.size());
            assertTrue(modifiedOriginators.contains(originator1));
            assertTrue(modifiedOriginators.contains(originator2));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(MementoAggregateTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of isEmpty method, of class MementoAggregate.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        
        // empty
        MementoAggregate instance = new MementoAggregate(0L);
        assertTrue(instance.isEmpty());
        
        // only own Memento
        instance.setOwnMemento(new Memento() {});
        assertFalse(instance.isEmpty());
        
        // clear: empty
        instance.setOwnMemento(null);
        assertTrue(instance.isEmpty());
        
        // only modified Originator
        instance.addModifiedOriginator(new TestOriginator());
        assertFalse(instance.isEmpty());
        
        // both
        instance.setOwnMemento(new Memento() {});
        assertFalse(instance.isEmpty());
    }

    /**
     * Test of updateModifiedOriginators method, of class MementoAggregate.
     */
    @Test
    public void testUpdateModifiedOriginators() {
        System.out.println("updateModifiedOriginators");
        
        TestOriginator originator = new TestOriginator();
        TestOriginator parent = new TestOriginator();
        originator.setMementoParent(parent);
        
        final int valueBefore = 2;
        final int valueAfter = 5;
        final int valueTemp = 19;
        
        try {
            Field field = Originator.class.getDeclaredField("history");
            field.setAccessible(true);
            LinkedList<MementoAggregate> history = (LinkedList<MementoAggregate>) field.get(parent);
            
            assertNotNull(history);
            assertTrue(history.isEmpty());
            
            originator.testVar = valueBefore;
            originator.mementoPush();
            originator.testVar = valueAfter;
            assertEquals(1, history.size());
            
            MementoAggregate instance = history.getLast();
            assertEquals(valueAfter, originator.testVar);
            
            originator.testVar = valueTemp;
            
            instance.updateModifiedOriginators();
            assertEquals(valueBefore, originator.testVar);
            
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(MementoAggregateTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
    
    private class TestMemento implements Memento {
        
        final int testVar;

        public TestMemento(int testVar) {
            this.testVar = testVar;
        }

        public int getTestVar() {
            return testVar;
        }
        
    }
    
    private class TestOriginator extends Originator {
            
        public int testVar = 0;

        @Override
        protected Memento createMemento() {
            return new TestMemento(testVar);
        }

        @Override
        protected void applyMemento(Memento memento) {
            testVar = ((TestMemento) memento).getTestVar();
        }
    };
}
