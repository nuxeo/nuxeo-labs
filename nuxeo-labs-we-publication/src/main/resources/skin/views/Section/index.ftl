<@extends src="base.ftl">

<@block name="content">
<h1>${Context.getProperty("currentSection").title}</h1>
<#assign x = 0>

<#list Context.getProperty("childrenContent") as sectionContent>
	<#if x%3=0>
		<div class="row-fluid">
	</#if>
	<div class="span4"><#include "minidoc.ftl">
	</div>
	<#assign x = x + 1>
	<#if x%3=0>
		</div>
	</#if>
</#list>
<#if x%3!=0>
		</div>
</#if>

<div class="row-fluid">
		<#list Context.getProperty("childSections") as higherSection>
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
</div>


</@block>
</@extends>
