/**
 * Provides the ability to use XML label tags to create JTextAreas.
 */

require(Packages.javax.swing.JTextArea);
require(Packages.javax.swing.JScrollPane);

__widgetTypes.registerWidget("textarea", function (parentComposite, widgetElement, parentID) {

    //Create a new textarea
    widget = new JTextArea();

    //Create a new scrollpane and add the textarea to it
    scrollPane = new JScrollPane(widget);

    //Set the text of the textArea
    widget.setText(widgetElement.getTextContent());

    //Initialize the widget
    var id = __widgetOps.initializeWidget(widget, widgetElement, parentID);

    //Add the scrollpane to the parentComposite
    parentComposite.add(scrollPane, __widgetOps.applyWidgetConstraint(id));

	//add word wrap unless specified not to
	//if it has a "wrap" attribute
	if(widgetElement.getAttributeNode("wrap") != null){
		//if word wrap is set to true
		if(widgetElement.getAttributeNode("wrap").getNodeValue() == "true"){
			widget.setLineWrap(true);
			widget.setWrapStyleWord(true);
		}
	}
	//if it's unspecified default is to use word wrap
	else{
		widget.setLineWrap(true);
		widget.setWrapStyleWord(true);
	}

    return widget;
});
