/**
 * Provides the ability to use XML label tags to create JTextAreas.
 */

require(Packages.javax.swing.JEditorPane);
require(Packages.javax.swing.JScrollPane);
require(Packages.jsyntaxpane.DefaultSyntaxKit);

widgetTypes.registerWidget("codearea", function (parentComposite, widgetElement, engine) {
    DefaultSyntaxKit.initKit();

    //Create a new textarea
    widget = new JEditorPane();

    //Create a new scrollpane and add the textarea to it
    scrollPane = new JScrollPane(widget);

    widget.setContentType("text/plain");

    if (widgetElement.getAttributeNode("type") != null) {
        widget.setContentType(widgetElement.getAttributeNode("type").getTextContent());
    }

    //Set the text of the textArea
    widget.setText(widgetElement.getTextContent());

    //Add the scrollpane to the parentComposite
    parentComposite.add(scrollPane, Layouts.getWidgetConstraint(widgetElement));

    widgetOps.initializeWidget(widget, widgetElement, engine);

    return widget;
});

