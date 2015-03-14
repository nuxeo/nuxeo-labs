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
var gDocId, gCropAndSave;
var gImgObj, gX1Obj, gX2Obj, gY1Obj, gY2Obj, gWidthObj, gHeightObj;

var gJcropApi;

NxCrop = {

	showCoordinates: function(c) {
		gX1Obj.val(c.x);
		gX2Obj.val(c.y);
		gY1Obj.val(c.x2);
		gY2Obj.val(c.y2);
		gWidthObj.val(c.w);
		gHeightObj.val(c.h);
	},

	init : function (inCropDivId, inNxDocId, inImageWidth, inImageHeight) {
		
		debugger;

		gImageW = inImageWidth;
		gImageH = inImageHeight;

		//console.log(inCropDivId + " - " + inNxDocId + " - " + inImageWidth + "x" + inImageHeight);
		
		gDocId = inNxDocId;

		var gCropAndSave = jQuery( document.getElementById(inCropDivId + "_cropAndSave") );
		if(gCropAndSave) {
			gCropAndSave.attr("disabled", true);
		}
		var gCropAndDownloadButton = jQuery( document.getElementById(inCropDivId + "_download") );
		if(gCropAndDownloadButton) {
			gCropAndDownloadButton.attr("disabled", true);
		}

		gX1Obj = jQuery( document.getElementById(inCropDivId + "_cropX1") );
		gX2Obj = jQuery( document.getElementById(inCropDivId + "_cropX2") );
		gY1Obj = jQuery( document.getElementById(inCropDivId + "_cropY1") );
		gY2Obj = jQuery( document.getElementById(inCropDivId + "_cropY2") );
		gWidthObj = jQuery( document.getElementById(inCropDivId + "_cropW") );
		gHeightObj = jQuery( document.getElementById(inCropDivId + "_cropH") );
		

		// The code is called twice: When the fancybox is initialized but not
		// yes displayed, and when it is displayed
		// This leads to problem with the cropping tool, which duplicates
		// the picture and losts itself.
		var fancybox = jQuery("#fancybox-content");
		if(fancybox && fancybox.is(":visible")) {
			// Can't use jQuery(@-"#" + inNxDocId) because nuxeo, sometimes adds
			// colons inside the id, so jQuery is lost.
			gImgObj = jQuery( document.getElementById(inCropDivId + "_img") );
			/*
			gImgObj.imgAreaSelect({
				handles: true,
				onSelectEnd: function(img, selection) {
					console.log(JSON.stringify(selection));
				}
			});
			*/
			
			gImgObj.Jcrop({
				onSelect: function(c) {
					if(gCropAndSave) {
						gCropAndSave.removeAttr("disabled");
					}
					if(gCropAndDownloadButton) {
						gCropAndDownloadButton.removeAttr("disabled");
					}
					NxCrop.showCoordinates(c);
				},
				onChange: NxCrop.showCoordinates
			}, function() {
				gJcropApi = this;
			});
			
			/*
			gImgObj.cropper({
			    aspectRatio: 1,
			    modal: false,
			    preview: "", //.extra-preview",
			    done: function(data) {
			        console.log(data);
			    }
			});
			*/
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
	    	// Call the ImageCropInDocumentOp operation.
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

	lastItem: "just for no comma in the JSON"

};
// = = = = = = = = = = End of nuxeo-labs-image-cropper.js



