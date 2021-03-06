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
package mudmap2.backend.WorldFileReader;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;
import mudmap2.backend.WorldFileReader.current.WorldFileDefault;
import mudmap2.frontend.dialog.OpenWorldDialog;

/**
 *
 * @author neop
 */
public class WorldFileFilterJSON extends FileFilter {

    @Override
    public boolean accept(File file) {
        if(file == null) return false;
        if(file.isDirectory()) return true;
        String worldname = null;

        try {
            worldname = (new WorldFileDefault(file.getPath()).readWorldName());
        } catch (Exception ex) {
            Logger.getLogger(OpenWorldDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        return worldname != null && !worldname.equals("");
    }

    @Override
    public String getDescription() {
        return "MUD Map 2 World Files (all)";
    }

}
