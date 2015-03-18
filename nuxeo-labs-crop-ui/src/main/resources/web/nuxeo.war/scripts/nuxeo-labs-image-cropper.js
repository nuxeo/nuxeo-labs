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
var gDocId, gCropAndSave, gCropAndAddToViews, gChangeImgSrc, gTheImg, gTheImgDiv;
var gImgObj;
var gX1Obj, gX2Obj, gY1Obj, gY2Obj, gWidthObj, gHeightObj;
var gOrigX1Obj, gOrigX2Obj, gOrigY1Obj, gOrigY2Obj, gOrigWidthObj, gOrigHeightObj;
var gJcropApi;
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

	showCoordinates: function(c) {		
		
		gX1Obj.val(c.x);
		gX2Obj.val(c.y);
		gY1Obj.val(c.x2);
		gY2Obj.val(c.y2);
		gWidthObj.val(c.w);
		gHeightObj.val(c.h);
		
		var scaleH = gImageProps.scaleH;
		var scaleV = gImageProps.scaleV;
		
		gOrigX1Obj.val(Math.floor(c.x * scaleH) );
		gOrigX2Obj.val( Math.floor(c.y * scaleV) );
		gOrigY1Obj.val( Math.floor(c.x2 * scaleH) );
		gOrigY2Obj.val( Math.floor(c.y2 * scaleV) );
		gOrigWidthObj.val( Math.floor(c.w * scaleH) );
		gOrigHeightObj.val( Math.floor(c.h * scaleV) );
				
	},
	
	askUser: function(inWhat, inObj) {
		
		var newValue = null,
			tmp;
		
		//debugger;
		
		function setValueIfNotNull(inObj, inValue) {
			if(isInteger(inValue)) {
				inObj.val(inValue);
			}
		}
		
		switch(inWhat) {
		case "x1":
			newValue = prompt("Left point:");
			setValueIfNotNull(gX1Obj, newValue);
			break;

		case "x2":
			newValue = prompt("Top point:");
			setValueIfNotNull(gX2Obj, newValue);
			gX2Obj.val(newValue);
			break;

		case "w":
			newValue = prompt("Width:");
			if(isInteger(newValue)) {
				tmp = parseInt(gX1Obj.val()) + parseInt(newValue);
				gY1Obj.val(tmp);
			}
			break;

		case "h":
			newValue = prompt("Height:");
			if(isInteger(newValue)) {
				tmp = parseInt(gX2Obj.val()) + parseInt(newValue);
				gY2Obj.val(tmp);
			}
			break;
		
		default:
			newValue = null;
			break;
		}
		
		if(newValue != null && isInteger(newValue)) {
			var x1 = Math.round( parseInt(gX1Obj.val()) / gImageProps.scaleH );
			var x2 = Math.round( parseInt(gX2Obj.val()) / gImageProps.scaleV );
			var y1 = Math.round( parseInt(gY1Obj.val()) / gImageProps.scaleH );
			var y2 = Math.round( parseInt(gY2Obj.val()) / gImageProps.scaleV );
			gJcropApi.setSelect([x1, x2, y1, y2]);
		}
	},

	init : function (inCropDivId, inNxDocId, inImageProps) {

		gImageProps = inImageProps;
		gImagePropsOriginal = jQuery.extend({}, gImageProps);
				
		gDocId = inNxDocId;
		
		gTheImg = jQuery( document.getElementById(inCropDivId + "_img") );
		gTheImgDiv = jQuery( document.getElementById(inCropDivId + "_divImg") );

		gCropAndSave = jQuery( document.getElementById(inCropDivId + "_cropAndSave") );
		gCropAndSave.attr("disabled", true);
		
		gCropAndAddToViews = jQuery( document.getElementById(inCropDivId + "_cropAndAddToViews") );
		gCropAndAddToViews.attr("disabled", true);
		
		gChangeImgSrc = jQuery( document.getElementById(inCropDivId + "_changeImgSrc") );

		gX1Obj = jQuery( document.getElementById(inCropDivId + "_cropX1") );
		gX2Obj = jQuery( document.getElementById(inCropDivId + "_cropX2") );
		gY1Obj = jQuery( document.getElementById(inCropDivId + "_cropY1") );
		gY2Obj = jQuery( document.getElementById(inCropDivId + "_cropY2") );
		gWidthObj = jQuery( document.getElementById(inCropDivId + "_cropW") );
		gHeightObj = jQuery( document.getElementById(inCropDivId + "_cropH") );
		
		gOrigX1Obj = jQuery( document.getElementById(inCropDivId + "_originalX1") );
		gOrigX2Obj = jQuery( document.getElementById(inCropDivId + "_originalX2") );
		gOrigY1Obj = jQuery( document.getElementById(inCropDivId + "_originalY1") );
		gOrigY2Obj = jQuery( document.getElementById(inCropDivId + "_originalY2") );
		gOrigWidthObj = jQuery( document.getElementById(inCropDivId + "_originalW") );
		gOrigHeightObj = jQuery( document.getElementById(inCropDivId + "_originalH") );

		// The code is called twice: When the fancybox is initialized but not
		// yet displayed, and when it is displayed
		// This leads to problem with the cropping tool, which duplicates
		// the picture and losts itself.
		var fancybox = jQuery("#fancybox-content");
		if(fancybox && fancybox.is(":visible")) {
			// Can't use jQuery(@-"#" + inNxDocId) because nuxeo, sometimes adds
			// colons inside the id, so jQuery is lost.
			gImgObj = jQuery( document.getElementById(inCropDivId + "_img") );

			//debugger;
			gOrigX1Obj.dblclick(function() { NxCrop.askUser("x1", gX1Obj); });
			gOrigX2Obj.dblclick(function() { NxCrop.askUser("x2", gX2Obj); });
			gOrigWidthObj.dblclick(function() { NxCrop.askUser("w", gWidthObj); });
			gOrigHeightObj.dblclick(function() { NxCrop.askUser("h", gHeightObj); });
			
			gImgObj.Jcrop({
				onSelect: function(c) {
					if(gCropAndSave) {
						gCropAndSave.removeAttr("disabled");
					}
					if(gCropAndAddToViews) {
						gCropAndAddToViews.removeAttr("disabled");
					}
					NxCrop.showCoordinates(c);
				},
				onChange: NxCrop.showCoordinates
			}, function() {
				gJcropApi = this;
			});
		}
	},

	cropAndSave: function() {

	    var c = gJcropApi.tellSelect();
	    if(c.w <= 0 || c.h <= 0) {
	    	alert("There is no crop area.");
	    } else {
	    	// Call the ImageCropInDocumentOp operation.
	    	// We can't use nuxeo.js, because the "nuxeo" object
	    	// already exists (and it is the main object of
	    	// nuxeo.js...
	    	// And we don't have time to fix that.
	    	var automationParams = {
	    		params: {	incrementVersion : "Minor",
				    		top		: c.y,
				    		left	: c.x,
				    		width	: c.w,
				    		height	: c.h,
				    		pictureWidth  : gImgObj.width(),
				    		pictureHeight : gImgObj.height()
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
				//alert("Dommage. Essaye encore.");
				alert( "Request failed: " + textStatus )
			} );
		}
	},

	cropAndAddToViews: function() {

	    var c = gJcropApi.tellSelect();
	    if(c.w <= 0 || c.h <= 0) {
	    	alert("There is no crop area.");
	    } else {
	    	// Call the ImageCropInViewsOp operation.
	    	// We can't use nuxeo.js, because the "nuxeo" object
	    	// already exists (and it is the main object of
	    	// nuxeo.js...
	    	// And we don't have time to fix that.
	    	var automationParams = {
	    		params: {	top		: c.y,
				    		left	: c.x,
				    		width	: c.w,
				    		height	: c.h,
				    		pictureWidth  : gImgObj.width(),
				    		pictureHeight : gImgObj.height()
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
		}
		
		//gTheImgDiv.width(w);
		//gTheImgDiv.height(h);

		gTheImg.width(w);
		gTheImg.height(h);
		gTheImg.attr("src", src);

		//gJcropApi.setImage(src);
		gJcropApi.destroy();
		gImgObj.Jcrop({
			onSelect: function(c) {
				if(gCropAndSave) {
					gCropAndSave.removeAttr("disabled");
				}
				if(gCropAndAddToViews) {
					gCropAndAddToViews.removeAttr("disabled");
				}
				NxCrop.showCoordinates(c);
			},
			onChange: NxCrop.showCoordinates
		}, function() {
			gJcropApi = this;
		});
	},

	lastItem: "just for no comma in the JSON"

};
// = = = = = = = = = = End of nuxeo-labs-image-cropper.js



