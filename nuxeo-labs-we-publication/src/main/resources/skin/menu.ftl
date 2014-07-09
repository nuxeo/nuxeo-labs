<#setting url_escaping_charset="UTF-8">

<div class="well sidebar-nav">
<h3>Top level sections</h3>
<ul class="nav nav-list">
<#list Context.getProperty("higherSections") as content>
				<li>
				<a href="${Context.getBaseURL()}${Context.getBasePath()}/publication/section/${content.id}">${content.title}</a>				
				</li>
</#list>
</ul>
</div>
