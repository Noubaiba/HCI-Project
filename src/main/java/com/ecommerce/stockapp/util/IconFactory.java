package com.ecommerce.stockapp.util;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;


public class IconFactory {

    public static SVGPath catalogIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"); // maison/catalog
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }

    public static SVGPath cartIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M7 4h-2l-1 2h2l3.6 7.59-1.35 2.44C8.16 16.37 8 16.68 8 17a2 2 0 002 2h12v-2H10.42a.25.25 0 01-.22-.37l.03-.06L11.1 15h7.45a2 2 0 001.79-1.11l3.58-6.49A1 1 0 0023 6H6");
        icon.setFill(Color.web("#083f7c"));
        return icon;
    }

    public static SVGPath ordersIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 3h18v2H3V3zm0 4h18v2H3V7zm0 4h12v2H3v-2z");
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