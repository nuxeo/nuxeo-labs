<#setting url_escaping_charset="UTF-8">
<div class="alert alert-info">
<h4>${sectionContent.title}</h5>
<p>${sectionContent.dublincore.description}</p>
<div class="row-fluid">
	<div class="span4">
		<div class="label label-info">Nature</div> 
	</div>
	<div class="span8">
		${sectionContent.dublincore.nature}
	</div>
</div>
<div class="row-fluid">
	
	<div class="span4">
		<div class="label label-info">Published</div> 
	</div>
	<div class="span8">
		${sectionContent.dublincore.issued?date}
	</div> 
</div>
<#if sectionContent.file.content.filename!="">
	<div class="row-fluid">
		<a href="${This.getDownloadURL(sectionContent)}">${sectionContent.file.content.filename}</a>
	</div>
</#if>
</div>