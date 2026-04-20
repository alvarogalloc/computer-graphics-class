package edu.up.cg;

import edu.up.cg.apps.cli.CliApp;
import edu.up.cg.apps.gui.GuiApp;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            CliApp.main(args);
        } else {
            GuiApp.main(args);
        }
    }
}
