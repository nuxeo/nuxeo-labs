<@extends src="base.ftl">

<@block name="content">
	<div class="hero-unit">
  		<h1>Publication browser</h1>
  		<p>Welcome to the publication brower webengine demo, you can navigate in sections from here.</p>
  		<p>For each section, you will see its publications and also its sub-sections with their latest publications</p>
	</div>
	<#list Context.getProperty("higherSections") as higherSection>
		<div class="row-fluid">
			<h3><a href="${Context.getBaseURL()}${Context.getBasePath()}/publication/section/${higherSection.id}">${higherSection.title}</a><small> (Latest publications)</small></h3>
		</div>
		<div class="row-fluid">
			<#if Context.getProperty("latestPublicationsMap")[higherSection.id]?has_content>
				<#list Context.getProperty("latestPublicationsMap")[higherSection.id] as sectionContent>
					<div  class="span4">
						<#include "minidoc.ftl"/>
					</div>
				</#list>		
			<#else>
				<div class="alert alert-block">
				No publication in this section yet!
				</div>
			</#if>
		</div>
	</#list>

</@block>
</@extends>
