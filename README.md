RapidS - [![Build Status](https://travis-ci.org/Tribex/RapidS.png?branch=master)](https://travis-ci.org/Tribex/RapidS)
======

A Java toolkit for creating cross-platform programs using just XML (.rsm) and JavaScript.

Progress
========
*22%*

* Initial Loader - 50%
* CSS Parser - 50%
* JavaScript Parsing System - 80%
* XML Widget JavaScript Callbacks - Need to Re-implement from SWT - 60%
* JavaScript-Java Standard Library - 5%
* Linking and relative file support - 5%
* Swing GUI XML Loader - 10% MAJOR TODO
* Swing layout support - TODO
* File format specification - TODO

A demo .rsm file - current version.
========

```
<rsm>
<head>
<title>RapidS: Test init file</title>
<link rel="stylesheet" href="styles.css"></link>
<style type="text/css">
#body {
	margin-left: 20px;
	margin-right: 10px;
	margin-bottom: 2800px;
}

#clickableButton {
	margin-left: 20;
	margin-right: 20;
	position-x: 0;
	position-y: rel;
	anchor: west;
	padding-y: 40;
	padding-x: 20;
}

#onButton {
	margin: 0 25 -30 0;
	anchor: west;
	fill: none;
	foreground-color: #222222;
}

.standardPosition {
	margin: 0 20 0 20;
	background-color: #FaFaFa;
	foreground-color: #335599;
	fill: NONE;
	anchor: west;
}

#rightSide {
	position-x: rel;
	occupied-cells-x: 1;
	occupied-cells-y: 5;
	position-y: 0;
	fill: HORIZONTAL;
	margin: 20 10 20 20;
	width: 400;
	height: 200;
}

label {
	fill: none;
	weight-y: 0.0;
	margin-left: 20;
	margin-right: 20;
	background-color: #999999;
	border: 2 #DDDDDD;
}

spinner {
	fill: HORIZONTAL;
	margin-left: 20;
	margin-right: 20;
}

button {
	margin-left: 20;
}

</style>
</head>

<script type="text/javascript">
			importClass(java.awt.Color);
			importClass(java.lang.System);

		//Button Clicking System
			function runBtnEvt(text, sourceID) {
				var label_list = window.getElementsByClass("changesOnHover");
				for (index = 0; index &lt; label_list.length; ++index) {
					if (sourceID == 0) {
						label_list[index].setText(text+" from Label");
						window.setTitle(text);
					} else {
						label_list[index].setText(text+" from Button");
					}
				}
			}
		
			function changeBackground(widget, state) {
				var widgetID = window.getElementById(widget);
				var color;
				if (state == 'click') {
					var color = Color.decode("#666666");
					//TODO: FixSetTimeout
					window.setTimeout('changeBackground("onButton", "over")', 300);
					
				} else if (state == 'over') {
					var color = Color.decode("#EEEEEE");
				} else if (state == 'out') {
					var color = Color.decode("#999999");
				}
				widgetID.setBackground(color);
			}

	</script> 
<body layout="gridBag" theme="camo">

	<label class="changesOnHover" onmouseup="runBtnEvt('Click', 0)"
		onmouseover="runBtnEvt('Mouse Over', 0)"
		onmouseout="runBtnEvt('Mouse Out', 0)">label changes on	mouseover, mouseout, and click.</label>
		
	<label id="onButton" onclick="changeBackground('onButton', 'click')"
		onmouseover="changeBackground('onButton', 'over')"
		onmouseout="changeBackground('onButton', 'out')">Clickable label on Button</label>
		
	<button id="clickableButton" class="changesOnHover" onclick="runBtnEvt('Click', 1)"
		onmouseover="runBtnEvt('Mouse Over', 1)"
		onmouseout="runBtnEvt('Mouse Out', 1)">Changes the status of the label.</button>
		
	<spinner id="spinner" value="10" max="20" min="-1"
		onselection="runBtnEvt('Spinner: '+$spinner.getValue(), 1)" />
	
	<textfield class="standardPosition">Enter a single line of text here!</textfield>
	
	<textarea id="rightSide">Enter multiple lines of text here!</textarea>

</body>

```