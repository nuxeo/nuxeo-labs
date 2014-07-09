# Description
A project to manage Image converters through automation.
New converters can be declared in XML. See conversions.xml in ressources for instances.

# Using the plugin 
The project creates a new operation that can be eclared in Studio.
The name of the operation is: 

- GenericConverter (category conversion)

The parameters of the operation are:

- converterName : the one declared in Studio
- parameters : the parameters of command line to pass to the operation (the comand line can be found in the declaration of the converters)

## Command line examples

The conversion contribution declares several conversions, here are a few example on how to use them.
### changeFormat
the parameter string in the declaration is: 

`#{targetFilePath}.#{format}
`

So the parameters of the operation should be :

- converter = changeFormat
- parameters = 
	
	targetFilePath=@{This.filename}
	
	format=png

Please not the png format here is just an example

### Overlaying
This converter awaits more parameters :
` #{sourceFilePath} -gravity #{gravity} -fill #{textColor}  -stroke '#A84100' -strokewidth 1 -pointsize #{textSize} -annotate #{textRotation}x#{textRotation}+#{xOffset}+#{yOffset} #{textValue} "#{targetFilePath}"
`

Here is an example of the parameters of the operation:

- converter = overlaying
- parameters = 

	targetFilePath=@{This.filename}
	
	textColor=red
	
	textValue=@{textValue}
	
	gravity=NorthEast
	
	textRotation=0
	
	xOffset=20
	
	yOffset=80
	
	textSize=30
	
Please not here that in the example above, @{textValue} reference a context variable set earlier in the automation chain.

### Notes
This project needs ImageMagick to work

This project is not unit-tested, please use with care

