# nuxeo-labs-widgets
===

Here a few custom widget templates:

## externalLink_widget.xhtml

Widget template for a text field, will display the value as a link in view mode, simple input text in edit mode.

## externalheadermenu_widget.xhtml

This widget is an action widget template: [http://doc.nuxeo.com/x/X4cPAQ](http://doc.nuxeo.com/x/X4cPAQ).
It is designed to display a list of links in the header menu (similar to the user menu but with links instead of actions in it).

It must be contributed to the header though the action extension point using the action type template, here is a simple example of how to do it:


	<extension point="actions" target="org.nuxeo.ecm.platform.actions.ActionService">
	    <action id="externalMenuHeader" order="101" type="template">
	      <category>MAIN_TABS</category>
	      <properties>
	        <property name="addForm">false</property>
	        <property name="template">/widgets/externalheadermenu_widget.xhtml
	        </property>
	      </properties>
	    </action>
	</extension>








