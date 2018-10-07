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
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.World;
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
public class MementoIntegrationTest {
    
    private static Field fieldHistory;
    
    public MementoIntegrationTest() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test
    public void testQuadtreeMoveSinglePlace1Step() {
        World world = new World();
        Layer layer1 = world.getNewLayer();
        
        final int place0x0 = 0, place0y0 = 0;
        final int place0x1 = 1, place0y1 = 3;
        final int place0x2 = -7, place0y2 = -5;
        Place place0 = world.putPlaceholder(layer1.getId(), place0x0, place0y0);
        
        assertEquals(place0x0, place0.getX());
        assertEquals(place0y0, place0.getY());
        assertEquals(place0, layer1.get(place0x0, place0y0));
        
        try {
            LinkedList<MementoAggregate> historyWorld = (LinkedList<MementoAggregate>) fieldHistory.get(world);
            assertNotNull(historyWorld);
            assertEquals(1, historyWorld.size());
            
            // Test 1: move place once
            Thread.sleep(1100);
            layer1.put(place0, place0x1, place0y1);
            assertEquals(2, historyWorld.size());
            assertEquals(place0x1, place0.getX());
            assertEquals(place0y1, place0.getY());
            assertEquals(place0, layer1.get(place0x1, place0y1));
            
            // undo
            world.mementoRestore();
            assertEquals(3, historyWorld.size());
            assertEquals(place0x0, place0.getX());
            assertEquals(place0y0, place0.getY());
            assertEquals(place0, layer1.get(place0x0, place0y0));
            
            // redo
            world.mementoStore();
            assertEquals(3, historyWorld.size());
            assertEquals(place0x1, place0.getX());
            assertEquals(place0y1, place0.getY());
            assertEquals(place0, layer1.get(place0x1, place0y1));
            
            
            // Test 2: move place twice
            Thread.sleep(1100);
            System.out.println("#>> vor put");
            layer1.put(place0, place0x2, place0y2);
            System.out.println("#>> nach put");
            assertEquals(3, historyWorld.size());
            // TODO: notifyChange erzeugt überflüssigen Eintrag bei World. Layer.remove()?
            assertEquals(place0x2, place0.getX());
            assertEquals(place0y2, place0.getY());
            assertEquals(place0, layer1.get(place0x2, place0y2));
            
            // undo x1
            world.mementoRestore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x1, place0.getX());
            assertEquals(place0y1, place0.getY());
            assertEquals(place0, layer1.get(place0x1, place0y1));
            
            // undo x2
            world.mementoRestore();
            //world.mementoRestore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x0, place0.getX());
            assertEquals(place0y0, place0.getY());
            assertEquals(place0, layer1.get(place0x0, place0y0));
            
            // redo x1
            world.mementoStore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x1, place0.getX());
            assertEquals(place0y1, place0.getY());
            assertEquals(place0, layer1.get(place0x1, place0y1));
            
            // redo x2
            world.mementoStore();
            //world.mementoStore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x2, place0.getX());
            assertEquals(place0y2, place0.getY());
            assertEquals(place0, layer1.get(place0x2, place0y2));
        } catch (Exception ex) {
            Logger.getLogger(MementoIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testQuadtreeMoveSinglePlace2Step() {
        World world = new World();
        Layer layer1 = world.getNewLayer();
        
        final int place0x0 = 0, place0y0 = 0;
        final int place0x1 = 1, place0y1 = 3;
        final int place0x2 = -7, place0y2 = -5;
        Place place0 = world.putPlaceholder(layer1.getId(), place0x0, place0y0);
        
        assertEquals(place0x0, place0.getX());
        assertEquals(place0y0, place0.getY());
        assertEquals(place0, layer1.get(place0x0, place0y0));
        
        try {
            LinkedList<MementoAggregate> historyWorld = (LinkedList<MementoAggregate>) fieldHistory.get(world);
            assertNotNull(historyWorld);
            assertEquals(1, historyWorld.size());
            
            // Test 1: move place twice
            Thread.sleep(1100);
            layer1.put(place0, place0x1, place0y1);
            assertEquals(2, historyWorld.size());
            assertEquals(place0x1, place0.getX());
            assertEquals(place0y1, place0.getY());
            assertEquals(place0, layer1.get(place0x1, place0y1));
           
            Thread.sleep(1100);
            layer1.put(place0, place0x2, place0y2);
            assertEquals(3, historyWorld.size());
            assertEquals(place0x2, place0.getX());
            assertEquals(place0y2, place0.getY());
            assertEquals(place0, layer1.get(place0x2, place0y2));
            
            // undo x1
            world.mementoRestore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x1, place0.getX());
            assertEquals(place0y1, place0.getY());
            assertEquals(place0, layer1.get(place0x1, place0y1));
            
            // undo x2
            world.mementoRestore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x0, place0.getX());
            assertEquals(place0y0, place0.getY());
            assertEquals(place0, layer1.get(place0x0, place0y0));
            
            // undo x3: before place has been inserted
            world.mementoRestore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x0, place0.getX()); // no effect
            assertEquals(place0y0, place0.getY()); // no effect
            assertNull(layer1.get(place0x0, place0y0));
            
            // undo x4: no effect
            world.mementoRestore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x0, place0.getX()); // no effect
            assertEquals(place0y0, place0.getY()); // no effect
            assertNull(layer1.get(place0x0, place0y0));
            
            // redo x1
            world.mementoStore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x0, place0.getX());
            assertEquals(place0y0, place0.getY());
            assertEquals(place0, layer1.get(place0x0, place0y0));
            
            // redo x2
            world.mementoStore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x1, place0.getX());
            assertEquals(place0y1, place0.getY());
            assertEquals(place0, layer1.get(place0x1, place0y1));
            
            // redo x3
            world.mementoStore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x2, place0.getX());
            assertEquals(place0y2, place0.getY());
            assertEquals(place0, layer1.get(place0x2, place0y2));
            
            // redo x4: no effect
            world.mementoStore();
            assertEquals(4, historyWorld.size());
            assertEquals(place0x2, place0.getX());
            assertEquals(place0y2, place0.getY());
            assertEquals(place0, layer1.get(place0x2, place0y2));
        } catch (Exception ex) {
            Logger.getLogger(MementoIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
}
