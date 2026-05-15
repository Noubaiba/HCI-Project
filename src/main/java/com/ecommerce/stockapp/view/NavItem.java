package com.ecommerce.stockapp.view;

import javafx.scene.Node;

public class NavItem {
    private Node icon;
    private String label;
    private Runnable action;

    public NavItem(Node icon, String label, Runnable action) {
        this.icon = icon;
        this.label = label;
        this.action = action;
    }

    public Node getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public Runnable getAction() {
        return action;
    }
}