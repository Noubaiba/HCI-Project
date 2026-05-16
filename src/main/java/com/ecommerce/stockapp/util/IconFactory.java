package com.ecommerce.stockapp.util;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class IconFactory {

    public static SVGPath home() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 12L12 3l9 9h-3v9h-12v-9H3z");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }

    public static SVGPath box() {
        SVGPath icon = new SVGPath();
        icon.setContent("M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }

    public static SVGPath users() {
        SVGPath icon = new SVGPath();
        icon.setContent("M16 11c1.66 0 3-1.34 3-3S17.66 5 16 5s-3 1.34-3 3 1.34 3 3 3zM8 11c1.66 0 3-1.34 3-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.67 0-8 1.34-8 4v3h10v-3c0-1.66.66-3.12 1.74-4.26C10.61 12.27 9.38 13 8 13zm8 0c-1.38 0-2.61-.73-3.74-1.26C13.34 13.88 14 15.34 14 17v3h10v-3c0-2.66-5.33-4-8-4z");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }

    public static SVGPath ordersIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 3h18v2H3v-2zm0 4h18v2H3V7zm0 4h12v2H3v-2z");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }

    public static SVGPath chart() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 3v18h18v-2H5V3H3zm4 12h2v4H7zm4-6h2v10h-2zm4 3h2v7h-2z");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }

    public static SVGPath log() {
        SVGPath icon = new SVGPath();
        icon.setContent("M4 4h16v2H4v-2zm0 4h16v2H4V8zm0 4h10v2H4v-2zm0 4h10v2H4v-2z");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }

    // ================= EXTRA ICONS =================

    public static SVGPath catalogIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }
    
    

    public static SVGPath cartIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M7 4h-2l-1 2h2l3.6 7.59-1.35 2.44C8.16 16.37 8 16.68 8 17a2 2 0 002 2h12v-2H10.42a.25.25 0 01-.22-.37l.03-.06L11.1 15h7.45a2 2 0 001.79-1.11l3.58-6.49A1 1 0 0023 6H6");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }

    public static SVGPath profileIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M12 12c2.7 0 5-2.3 5-5s-2.3-5-5-5-5 2.3-5 5 2.3 5 5 5zm0 2c-3.3 0-10 1.7-10 5v3h20v-3c0-3.3-6.7-5-10-5z");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }
}