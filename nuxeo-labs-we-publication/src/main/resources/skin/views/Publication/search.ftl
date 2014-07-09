<@extends src="base.ftl">
<@block name="pageTitle">Search result for "${This.searchPattern}"</@block>

<@block name="content">
<#setting url_escaping_charset="UTF-8">

<#list Context.getProperty("searchResult") as sectionContent>
	<div><#include "minidoc.ftl"></div>
</#list>



</@block>
</@extends>
