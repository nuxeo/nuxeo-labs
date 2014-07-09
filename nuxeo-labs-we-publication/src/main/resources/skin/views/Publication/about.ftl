<@extends src="base.ftl">
<@block name="pageTitle"></@block>
<@block name="subTitle">
<img src="${skinPath}/img/Nuxeo-Open-Source-ECM.png" height="50px"/>
</@block>

<@block name="content">
<#setting url_escaping_charset="UTF-8">
<div class="hero-unit">
  		<h1>Nuxeo</h1>
  		<p>Nuxeo provides an Open Source Platform for Enterprise Content Management (ECM) enabling architects and developers to easily build, deploy, and run the best applications, period. The Nuxeo product offering surpasses traditional solutions, affording much more flexibility as a platform, so that your content management application aligns with your business and technical needs.</p>
  		<a class="label label-info" href="http://www.nuxeo.com" target="blank"/>www.nuxeo.com<a/>
</div>

		<div class="gadgets-gadget-chrome gadgets" style="float:left; padding-top: 10px"></div>
		<div class="gadgets-gadget-chrome gadgets" style="float:left; padding-top: 10px"></div>
		<div class="gadgets-gadget-chrome gadgets" style="float:left; padding-top: 10px"></div>
<script type="text/javascript">
  $(document).ready(function() {
    $('.gadgets').openSocialGadget({
      baseURL: '${contextPath}' + '/',
      language: '${Context.locale.language}',
      gadgetDefs: [ 
        { specUrl: '${Runtime.getProperty('nuxeo.loopback.url')}/site/gadgets/userworkspaces/userworkspaces.xml',
          title: 'User Workspaces' }, 
        { specUrl: '${Runtime.getProperty('nuxeo.loopback.url')}/site/gadgets/lastdocuments/lastdocuments.xml',
          title: 'Last Modified Documents' },
        { specUrl: '${Runtime.getProperty('nuxeo.loopback.url')}/site/gadgets/quicksearch/quicksearch.xml',
          title: 'Quick Search' },
          
      ]
    });
  })
</script>
</@block>

</@extends>
