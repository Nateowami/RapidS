/**
 * Provides the ability to use XML menu tags to create JMenus.
 */

require(Packages.javax.swing.JMenu);
require(Packages.javax.swing.JMenuItem);
require(Packages.us.derfers.tribex.rapids.jvStdLib.Window);

widgetTypes.registerWidget("menu", function (parentComposite, widgetElement, engine) {
    var widget = new JMenu();

    if (widgetElement.getParentNode().getNodeName() == "body") {
        parentComposite = window.getElementById(widgetElement.getParentNode().getParentNode().getAttributeNode("id").getTextContent()).getJMenuBar();
    }

    if (widgetElement.getAttributeNode("value") != null) {
        widget.setText(widgetElement.getAttributeNode("value").getTextContent());
    } else {
        Utilities.showError("Error: No value tag in menu element.");
    }
    parentComposite.add(widget);

    GUI.loadInComposite(widget, widgetElement, engine);

    widgetOps.initializeWidget(widget, widgetElement, engine);
    return widget;
});

widgetTypes.registerWidget("menuitem", function (parentComposite, widgetElement, engine) {
    var widget = new JMenuItem();

    widget.setText(widgetElement.getTextContent());

    parentComposite.add(widget);

    widgetOps.initializeWidget(widget, widgetElement, engine);
    return widget;
});
