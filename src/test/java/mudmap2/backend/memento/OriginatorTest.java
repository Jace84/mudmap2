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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author neop
 */
public class OriginatorTest {

    private static Field fieldHistory;

    public OriginatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        try {
            fieldHistory = Originator.class.getDeclaredField("history");
            assertNotNull(fieldHistory);
            fieldHistory.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException ex) {
            Logger.getLogger(OriginatorTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of mementoPush method, of class Originator.
     */
    @Test
    public void testMementoPush() {
        System.out.println("mementoPush");

        TestOriginator instance = new TestOriginator();
        TestOriginator parent = new TestOriginator();
        instance.setMementoParent(parent);

        final int valueBefore = 2;
        final int valueAfter = 5;

        try {
            LinkedList<MementoAggregate> instanceHistory = (LinkedList<MementoAggregate>) fieldHistory.get(instance);
            LinkedList<MementoAggregate> parentHistory = (LinkedList<MementoAggregate>) fieldHistory.get(parent);
            assertNotNull(instanceHistory);
            assertNotNull(parentHistory);

            // before push
            assertEquals(0, instanceHistory.size());
            assertEquals(0, parentHistory.size());

            // push first
            instance.testVar = valueBefore;
            instance.mementoPush();

            assertEquals(1, instanceHistory.size());
            assertEquals(1, parentHistory.size());

            assertNotNull(instanceHistory.getLast().getOwnMemento());
            assertEquals(valueBefore, ((TestMemento) instanceHistory.getLast().getOwnMemento()).testVar);
            assertNull(parentHistory.getLast().getOwnMemento());
            assertFalse(parentHistory.getLast().isEmpty());

            // push second, don't wait
            instance.testVar = valueAfter;
            instance.mementoPush();

            assertEquals(1, instanceHistory.size());
            assertEquals(1, parentHistory.size());

            assertNotNull(instanceHistory.getLast().getOwnMemento());
            assertEquals(valueBefore, ((TestMemento) instanceHistory.getLast().getOwnMemento()).testVar);
            assertNull(parentHistory.getLast().getOwnMemento());
            assertFalse(parentHistory.getLast().isEmpty());

            // push third, wait
            Thread.sleep(1100);
            instance.mementoPush();

            assertEquals(2, instanceHistory.size());
            assertEquals(2, parentHistory.size());

            assertNotNull(instanceHistory.getLast().getOwnMemento());
            assertEquals(valueAfter, ((TestMemento) instanceHistory.getLast().getOwnMemento()).testVar);
            assertNull(parentHistory.getLast().getOwnMemento());
            assertFalse(parentHistory.getLast().isEmpty());
        } catch (IllegalArgumentException | IllegalAccessException | InterruptedException ex) {
            Logger.getLogger(OriginatorTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

    }

    /**
     * Test of mementoRestore method, of class Originator.
     */
    @Test
    public void testMementoRestore() {
        System.out.println("mementoRestore");

        final int val1 = 123;
        final int val2 = 456;
        final int val3 = 789;
        final int val4 = 5432;

        final int val5 = 321;
        final int val6 = 654;

        TestOriginator instance = new TestOriginator();
        try {
            instance.testVar = val1;
            instance.mementoPush();
            Thread.sleep(1100);
            instance.testVar = val2;
            instance.mementoPush();
            instance.testVar = val3;

            LinkedList<MementoAggregate> history = (LinkedList<MementoAggregate>) fieldHistory.get(instance);
            assertNotNull(history);

            // preconditions
            assertEquals(val3, instance.testVar);
            assertEquals(2, history.size());

            // reset to val2, save unsaved val3
            instance.mementoRestore();
            assertEquals(val2, instance.testVar);
            assertEquals(3, history.size());

            // reset to val1
            instance.mementoRestore();
            assertEquals(val1, instance.testVar);
            assertEquals(3, history.size());

            // reset to val1, should have no effect
            instance.mementoRestore();
            assertEquals(val1, instance.testVar);
            assertEquals(3, history.size());

            // change value and reset, should 'not' update history unless a child is changed
            instance.testVar = val4;
            instance.mementoRestore();
            assertEquals(val1, instance.testVar);
            assertEquals(3, history.size());
            assertEquals(val3, ((TestMemento) history.getLast().getOwnMemento()).testVar);

            // add child, update it
            TestOriginator child = new TestOriginator();
            child.setMementoParent(instance);
            child.testVar = val5;
            child.mementoPush();
            child.testVar = val6;

            assertEquals(val6, child.testVar);
            assertEquals(2, history.size()); // history gets updated by push of child

            instance.mementoRestore();
            assertEquals(val1, instance.testVar);
            assertEquals(val5, child.testVar);
        } catch (IllegalArgumentException | IllegalAccessException | InterruptedException ex) {
            Logger.getLogger(OriginatorTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of mementoStore method, of class Originator.
     */
    @Test
    public void testMementoStore() {
        System.out.println("mementoStore");

        final int val1 = 123;
        final int val2 = 456;
        final int val3 = 789;

        // test without parent / child
        try {
            TestOriginator instance = new TestOriginator();

            // fill history
            instance.testVar = val1;
            instance.mementoPush();
            instance.testVar = val2;
            Thread.sleep(1100);
            instance.mementoPush();
            instance.testVar = val3;

            // go back in history
            instance.mementoRestore();
            instance.mementoRestore();
            instance.mementoRestore();
            assertEquals(val1, instance.testVar);

            // check history / precondition
            LinkedList<MementoAggregate> history = (LinkedList<MementoAggregate>) fieldHistory.get(instance);
            assertNotNull(history);
            assertEquals(3, history.size());

            instance.mementoStore();
            assertEquals(val2, instance.testVar);

            instance.mementoStore();
            assertEquals(val3, instance.testVar);

            // storing again has no effect
            instance.mementoStore();
            assertEquals(val3, instance.testVar);

            // check if history changed
            assertEquals(3, history.size());
        } catch (InterruptedException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(OriginatorTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        // test with parent / child
        try {
            TestOriginator parent = new TestOriginator();
            TestOriginator child = new TestOriginator();
            child.setMementoParent(parent);

            // fill history
            child.testVar = val1;
            child.mementoPush();
            child.testVar = val2;
            Thread.sleep(1100);
            child.mementoPush();
            child.testVar = val3;

            // go back in history
            parent.mementoRestore();
            parent.mementoRestore();
            parent.mementoRestore();

            // check history / precondition
            LinkedList<MementoAggregate> historyParent = (LinkedList<MementoAggregate>) fieldHistory.get(parent);
            LinkedList<MementoAggregate> historyChild = (LinkedList<MementoAggregate>) fieldHistory.get(child);
            assertNotNull(historyParent);
            assertNotNull(historyChild);
            assertEquals(3, historyParent.size());
            assertEquals(3, historyChild.size());

            assertEquals(val1, child.testVar);

            parent.mementoStore();
            assertEquals(val2, child.testVar);

            parent.mementoStore();
            assertEquals(val3, child.testVar);

            // storing again has no effect
            parent.mementoStore();
            assertEquals(val3, child.testVar);

            // check if history changed
            assertEquals(3, historyParent.size());
            assertEquals(3, historyChild.size());
        } catch (InterruptedException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(OriginatorTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of setParent method, of class Originator.
     */
    @Test
    public void testSetParent() {
        System.out.println("setParent");

        Originator parent = new TestOriginator();
        Originator instance = new TestOriginator();

        try {
            Method hasParent = Originator.class.getDeclaredMethod("hasParent");
            hasParent.setAccessible(true);

            assertFalse((boolean) hasParent.invoke(instance));

            instance.setMementoParent(parent);
            assertTrue((boolean) hasParent.invoke(instance));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(OriginatorTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of setMementoParentEnabled method, of class Originator.
     */
    @Test
    public void testSetMementoParentEnabled() {
        System.out.println("setMementoParentEnabled");

        Originator instance = new TestOriginator();
        Originator parent = new TestOriginator();
        instance.setMementoParent(parent);

        try {
            Field fieldEnabled = Originator.class.getDeclaredField("enabled");
            fieldEnabled.setAccessible(true);

            assertTrue(fieldEnabled.getBoolean(instance));
            assertTrue(fieldEnabled.getBoolean(parent));

            instance.setMementoParentEnabled(false);
            assertTrue(fieldEnabled.getBoolean(instance));
            assertFalse(fieldEnabled.getBoolean(parent));

            instance.setMementoParentEnabled(true);
            assertTrue(fieldEnabled.getBoolean(instance));
            assertTrue(fieldEnabled.getBoolean(parent));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(OriginatorTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of isMementoParentEnabled method, of class Originator.
     */
    @Test
    public void testIsMementoParentEnabled() {
        System.out.println("isMementoParentEnabled");

        Originator instance = new TestOriginator();
        Originator parent = new TestOriginator();
        instance.setMementoParent(parent);

        assertTrue(instance.isMementoParentEnabled());
        assertTrue(parent.isMementoParentEnabled());

        instance.setMementoParentEnabled(false);
        assertFalse(instance.isMementoParentEnabled());
        assertFalse(parent.isMementoParentEnabled());

        instance.setMementoParentEnabled(true);
        assertTrue(instance.isMementoParentEnabled());
        assertTrue(parent.isMementoParentEnabled());
    }

    /**
     * Test of goToTimestamp method, of class Originator.
     */
    @Test
    public void testGoToTimestamp() {
        System.out.println("goToTimestamp");

        final int val1 = 123;
        final int val2 = 345;
        final int val3 = 678;

        TestOriginator instance = new TestOriginator();

        try {
            instance.testVar = val1;
            instance.mementoPush();
            Thread.sleep(1100);
            instance.testVar = val2;
            instance.mementoPush();
            instance.testVar = val3;

            final LinkedList<MementoAggregate> history = (LinkedList<MementoAggregate>) fieldHistory.get(instance);
            assertNotNull(history);
            assertEquals(2, history.size());

            final long timestamp1 = history.get(0).getTimestamp();
            final long timestamp2 = history.get(1).getTimestamp();

            // precondition: value is val3
            assertEquals(val3, instance.testVar);

            // reset to val1, should not create new history entry
            instance.goToTimestamp(timestamp1);
            assertEquals(val1, instance.testVar);
            assertEquals(2, history.size());

            // reset to val2
            instance.goToTimestamp(timestamp2);
            assertEquals(val2, instance.testVar);

        } catch (IllegalArgumentException | IllegalAccessException | InterruptedException ex) {
            Logger.getLogger(OriginatorTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    public void testCanRestore() {
        System.out.println("canRestore");

        TestOriginator instance = new TestOriginator();
        assertFalse(instance.canRestore());

        instance.mementoPush();
        assertTrue(instance.canRestore());

        instance.mementoRestore();
        assertFalse(instance.canRestore());

        instance.mementoStore();
        assertTrue(instance.canRestore());
    }

    public void testCanStore() {
        System.out.println("canStore");

        TestOriginator instance = new TestOriginator();
        assertFalse(instance.canStore());

        instance.mementoPush();
        assertFalse(instance.canStore());

        instance.mementoRestore();
        assertTrue(instance.canStore());

        instance.mementoStore();
        assertFalse(instance.canStore());
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
