/* nuxeo-labs-image-cropperjs
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thibaud Arguillere
 */

 /**
  * General consideration(s).
  * <p>
  * We can't use nuxeo.js here because it defines a "nuxeo" object, while the default
  * JSF stuff also defines one. That's why we do the AJAX calls ourselves. That said,
  * as it's about doing some quite simple calls, it's ok.
  */

var gDocId, gCropAndSave, gCropAndAddToViews, gChangeImgSrc, gTheImg, gTheImgDiv;
var gX1Obj_left, gY1Obj_top, gX2Obj_right, gY2Obj_bottom, gWidthObj, gHeightObj;
var gOrigX1Obj_left, gOrigY1Obj_top, gOrigX2Obj_right, gOrigY2Obj_bottom, gOrigWidthObj, gOrigHeightObj;
var gJcropApi, gTheImgId, gSelectWM, gWMPosition;
var gImageProps, gImagePropsOriginal;

// Utilities
function isInteger(inString) {
	var n = parseInt(inString);
	if(!isNaN(n)) {
		return n.toString().length == inString.length;
	}
	
	return false;
}

NxCrop = {
		
	isOriginalImage: function() {
		
		return gImageProps.scaleH === 1.0 && gImageProps.scaleV === 1.0;
		
	},

	showCoordinates: function(c) {
		
		gX1Obj_left.val(c.x);
		gY1Obj_top.val(c.y);
		gX2Obj_right.val(c.x2);
		gY2Obj_bottom.val(c.y2);
		gWidthObj.val(c.w);
		gHeightObj.val(c.h);
		
		var scaleH = gImageProps.scaleH;
		var scaleV = gImageProps.scaleV;
		
		gOrigX1Obj_left.val(Math.floor(c.x * scaleH) );
		gOrigY1Obj_top.val( Math.floor(c.y * scaleV) );
		gOrigX2Obj_right.val( Math.floor(c.x2 * scaleH) );
		gOrigY2Obj_bottom.val( Math.floor(c.y2 * scaleV) );
		gOrigWidthObj.val( Math.floor(c.w * scaleH) );
		gOrigHeightObj.val( Math.floor(c.h * scaleV) );
				
	},
	
	// We setup the _original_ values, then we calculate the values in current size
	askUserInOriginCoordinates: function(inWhat, inObj) {
		
		var newValue = null,
			tmp, x, y, x2, y2, w, h;
				
		function setValueIfNotNull(inObj, inValue) {
			if(isInteger(inValue)) {
				inObj.val(inValue);
			}
		}
		
		switch(inWhat) {
		case "left":
			newValue = prompt("Left point:");
			setValueIfNotNull(gOrigX1Obj_left, newValue);
			break;

		case "top":
			newValue = prompt("Top point:");
			setValueIfNotNull(gOrigY1Obj_top, newValue);
			break;

		case "w":
			newValue = prompt("Width:");
			if(isInteger(newValue)) {
				tmp = parseInt(gOrigX1Obj_left.val()) + parseInt(newValue);
				gOrigX2Obj_right.val(tmp);
			}
			break;

		case "h":
			newValue = prompt("Height:");
			if(isInteger(newValue)) {
				tmp = parseInt(gOrigY1Obj_top.val()) + parseInt(newValue);
				gOrigY2Obj_bottom.val(tmp);
			}
			break;
		
		default:
			newValue = null;
			break;
		}
		
		if(newValue != null && isInteger(newValue)) {
			
			x = parseInt(gOrigX1Obj_left.val());
			y = parseInt(gOrigY1Obj_top.val());
			x2 = parseInt(gOrigX2Obj_right.val());
			y2= parseInt(gOrigY2Obj_bottom.val());
			w = x2 - x;
			h = y2 - y;
			gOrigWidthObj.val(w);
			gOrigHeightObj.val(h);
			
			// Scale down for resized picture
			x = Math.round( x / gImageProps.scaleH );
			y = Math.round( y / gImageProps.scaleV );
			x2 = Math.round( x2 / gImageProps.scaleH );
			y2 = Math.round( y2 / gImageProps.scaleV );
			gJcropApi.setSelect([x, y, x2, y2]);
		}
	},

	init : function (inCropDivId, inNxDocId, inImageProps) {

		// The code is called twice: When the fancybox is initialized but not
		// yet displayed, and when it is displayed
		// This leads to problem with the cropping tool, which duplicates
		// the picture and losts itself.
		// Also, a quick reminder about using getElementById() with jQuery: we can't
		// use jQuery(@-"#" + inNxDocId) because nuxeo (well, JSF), sometimes adds
		// colons inside the id, so jQuery is lost.
		var fancybox = jQuery("#fancybox-content");
		if(fancybox && fancybox.is(":visible")) {

			gImageProps = inImageProps;
			gImagePropsOriginal = jQuery.extend({}, gImageProps);
					
			gDocId = inNxDocId;
			
			gTheImgId = inCropDivId + "_img";
			gTheImg = jQuery( document.getElementById(gTheImgId) );
			gTheImgDiv = jQuery( document.getElementById(inCropDivId + "_divImg") );

			gCropAndSave = jQuery( document.getElementById(inCropDivId + "_cropAndSave") );
			gCropAndSave.attr("disabled", true);
			
			gCropAndAddToViews = jQuery( document.getElementById(inCropDivId + "_cropAndAddToViews") );
			gCropAndAddToViews.attr("disabled", true);
			
			gChangeImgSrc = jQuery( document.getElementById(inCropDivId + "_changeImgSrc") );
	
			gX1Obj_left = jQuery( document.getElementById(inCropDivId + "_cropX1") );
			gY1Obj_top = jQuery( document.getElementById(inCropDivId + "_cropY1") );
			gX2Obj_right = jQuery( document.getElementById(inCropDivId + "_cropX2") );
			gY2Obj_bottom = jQuery( document.getElementById(inCropDivId + "_cropY2") );
			gWidthObj = jQuery( document.getElementById(inCropDivId + "_cropW") );
			gHeightObj = jQuery( document.getElementById(inCropDivId + "_cropH") );
			
			gOrigX1Obj_left = jQuery( document.getElementById(inCropDivId + "_originalX1") );
			gOrigY1Obj_top = jQuery( document.getElementById(inCropDivId + "_originalY1") );
			gOrigX2Obj_right = jQuery( document.getElementById(inCropDivId + "_originalX2") );
			gOrigY2Obj_bottom = jQuery( document.getElementById(inCropDivId + "_originalY2") );
			gOrigWidthObj = jQuery( document.getElementById(inCropDivId + "_originalW") );
			gOrigHeightObj = jQuery( document.getElementById(inCropDivId + "_originalH") );
			
			gTheImg.Jcrop({
				onSelect: function(c) {
					if(gCropAndSave) {
						gCropAndSave.removeAttr("disabled");
					}
					if(gCropAndAddToViews) {
						gCropAndAddToViews.removeAttr("disabled");
					}
					NxCrop.showCoordinates(c);
				},
				onChange: function(c) {
					if(gCropAndSave) {
						gCropAndSave.removeAttr("disabled");
					}
					if(gCropAndAddToViews) {
						gCropAndAddToViews.removeAttr("disabled");
					}
					NxCrop.showCoordinates(c);
				}
			}, function() {
				gJcropApi = this;
				// Set a selection area
				setTimeout(function() {
					gJcropApi.setSelect([5, 5, 55, 55]);
				}, 500)
			});
		}
	},

	cropAndSave: function() {

	    var c = gJcropApi.tellSelect();
	    if(c.w <= 0 || c.h <= 0) {
	    	alert("There is no crop area.");
	    } else {
	    	var automationParams = {
	    		params: {	incrementVersion : "Minor",
				    		top		: c.y,
				    		left	: c.x,
				    		width	: c.w,
				    		height	: c.h,
				    		pictureWidth  : gTheImg.width(),
				    		pictureHeight : gTheImg.height()
				    	},

				context: {},

				input : gDocId
			}
	    	
	    	var theURL = "/nuxeo/site/automation/ImageCropInDocument";
	    	jQuery.ajax({
				url		: theURL,
				type	: "POST",
				contentType: "application/json+nxrequest",
				data	: JSON.stringify(automationParams),
				headers	: {'X-NXVoidOperation': true, 'Accept': '*/*'}
			})
			.done( function() {
				location.reload(true);
			})
			.fail( function(jqXHR, textStatus, errorThrown) {
				alert( "Request failed: " + textStatus )
			} );
		}
	},

	cropAndAddToViews: function() {

	    var c = gJcropApi.tellSelect();
	    if(c.w <= 0 || c.h <= 0) {
	    	alert("There is no crop area.");
	    } else {
	    	var automationParams = {
	    		params: {	top		: c.y,
				    		left	: c.x,
				    		width	: c.w,
				    		height	: c.h,
				    		pictureWidth  : gTheImg.width(),
				    		pictureHeight : gTheImg.height()
				    	},

				context: {},

				input : gDocId
			}
	    	
	    	var theURL = "/nuxeo/site/automation/ImageCropInViewsOp";
	    	jQuery.ajax({
				url		: theURL,
				type	: "POST",
				contentType: "application/json+nxrequest",
				data	: JSON.stringify(automationParams),
				headers	: {'X-NXVoidOperation': true, 'Accept': '*/*'}
			})
			.done( function() {
				location.reload(true);
			})
			.fail( function(jqXHR, textStatus, errorThrown) {
				//alert("Dommage. Essaye encore.");
				alert( "Request failed: " + textStatus )
			} );
	    }
	},
	
	changeImgSrc: function(inEvt) {
		var src, w, h;

		var c = gJcropApi.tellSelect();
		if(gChangeImgSrc.text() == "Use Original Image") {
			
			c.x = Math.round(c.x * gImageProps.scaleH);
			c.y = Math.round(c.y * gImageProps.scaleV);
			c.x2 = Math.round(c.x2 * gImageProps.scaleH);
			c.y2 = Math.round(c.y2 * gImageProps.scaleV);
			c.w = Math.round(c.w * gImageProps.scaleH);
			c.h = Math.round(c.h * gImageProps.scaleV);
			
			src = gImageProps.originalUrl;
			w = gImageProps.originalWidth;
			h = gImageProps.originalHeight;
			gChangeImgSrc.text("Use Resized Image View");
			
			gImageProps.cropWidth = w;
			gImageProps.cropHeight = h;
			gImageProps.scaleH = 1.0;
			gImageProps.scaleV = 1.0;
			gImageProps.cropUrl = src;
			
		} else {
			
			gImageProps = jQuery.extend({}, gImagePropsOriginal);
			
			src = gImageProps.cropUrl;
			w = gImageProps.cropWidth;
			h = gImageProps.cropHeight;
			gChangeImgSrc.text("Use Original Image");

			Math.round( parseInt(gX1Obj_left.val()) / gImageProps.scaleH );
			
			c.x = Math.round(c.x / gImageProps.scaleH);
			c.y = Math.round(c.y / gImageProps.scaleV);
			c.x2 = Math.round(c.x2 / gImageProps.scaleH);
			c.y2 = Math.round(c.y2 / gImageProps.scaleV);
			c.w = Math.round(c.w / gImageProps.scaleH);
			c.h = Math.round(c.h / gImageProps.scaleV);
		}
		
		gJcropApi.destroy();
		gTheImgDiv.empty();

		var img = "<img id='" + gTheImgId + "' src='" + src + "' style='display:block; margin-left: auto; margin-right:auto' width='" + w + "px' height='" + h + "px' />";
		gTheImgDiv.append(img);
		gTheImg = jQuery( document.getElementById(gTheImgId) );

		gTheImg.Jcrop({
			onSelect: function(c) {
				NxCrop.showCoordinates(c);
			},
			onChange: NxCrop.showCoordinates
		}, function() {
			gJcropApi = this;
			setTimeout(function() {
				gJcropApi.setSelect([c.x, c.y, c.x2, c.y2]);
			}, 500);
			
		});
	},

	lastItem: "just for no comma in the JSON"

};
// = = = = = = = = = = End of nuxeo-labs-image-cropper.js



