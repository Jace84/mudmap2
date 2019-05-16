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
package mudmap2.frontend.dialog.informationColor;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import mudmap2.backend.InformationColor;
import mudmap2.backend.World;
import mudmap2.frontend.dialog.ListDialog;
import mudmap2.utils.AlphanumComparator;

/**
 * A dialog for creating, removing and modifying information color
 * @author neop
 */
public class InformationColorListDialog extends ListDialog {

    World world;

    public InformationColorListDialog(JFrame parent, World world) {
        super(parent, "Colored information rings", false);
        this.world = world;
        setCellRenderer(new InformationColorListCellRenderer());
    }

    @Override
    protected void create(){
        super.create();

        // only select one information color at once
        getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setPreferredSize(new Dimension(250, 300));
        pack();
        setLocation(getParent().getX() + (getParent().getWidth() - getWidth()) / 2, getParent().getY() + (getParent().getHeight() - getHeight()) / 2);
    }

    @Override
    protected void updateList(){
        List selectedValuesList = getList().getSelectedValuesList();

        Collection<InformationColor> informationColor = world.getInformationColors();
        // sort by name
        List<Object> sorted = informationColor.stream().sorted(new AlphanumComparator<>()).collect(Collectors.toList());
        getList().setListData(sorted.toArray());

        // select previously selected value(s)
        if(!selectedValuesList.isEmpty()){
            int[] indices = new int[selectedValuesList.size()];
            int indicesCnt = 0;

            ListModel model = getList().getModel();
            for(Integer i = 0; i < model.getSize(); ++i){
                if(selectedValuesList.contains(model.getElementAt(i))){
                    indices[indicesCnt++] = i;
                }
            }

            /* number of selected values does not match if places got removed
             * Remove extra entries from list
             */
            if(indicesCnt != selectedValuesList.size()){
                int[] indicesTemp = indices;
                indices = new int[indicesCnt];
                for(int i = 0; i < indicesCnt; ++i){
                    indices[i] = indicesTemp[i];
                }
            }

            getList().setSelectedIndices(indices);
        }
    }

    @Override
    protected void addEntry(){
        (new InformationColorDialog((JFrame) getParent(), world)).setVisible(true);
        updateList();
    }

    @Override
    protected void removeEntry(){
        int response = JOptionPane.showConfirmDialog(this, "Remove selected entries? This can not be undone!", "Place Groups", JOptionPane.WARNING_MESSAGE);

        if(response == JOptionPane.OK_OPTION){
            List selectedValuesList = getList().getSelectedValuesList();
            for(Object entry: selectedValuesList){
                InformationColor informationColor = (InformationColor) entry;
                try {
                    world.removeInformationColor(informationColor);
                } catch (Exception ex) {
                    Logger.getLogger(InformationColorListDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            updateList();
        }
    }

    @Override
    protected void modifyEntry(){
        if(!getList().isSelectionEmpty()){
            InformationColor rl = (InformationColor) getList().getSelectedValue();
            (new InformationColorDialog((JFrame) getParent(), world, rl)).setVisible(true);
            updateList();
        }
    }

    private class InformationColorListCellRenderer extends ColoredListCellRenderer<InformationColor> {

        @Override
        protected String getText(InformationColor object) {
            return object.getDescription();
        }

        @Override
        protected Color getColor(InformationColor object) {
            return object.getColor();
        }

    }

}
