<html>
<head>
  <title>
     <@block name="title">
     Publication browser
     </@block>
  </title>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>

  <link rel="stylesheet" href="${skinPath}/css/site.css" type="text/css" media="screen" charset="utf-8">
  <link rel="shortcut icon" href="${skinPath}/img/code-classe.png" />
  <link rel="stylesheet" href="${skinPath}/css/bootstrap.min.css">
  <style type="text/css">
      body {
        padding-top: 60px;
        padding-bottom: 40px;
      }
      .sidebar-nav {
        padding: 9px 0;
      }
  </style>
  
      
    <link rel="stylesheet" href="${contextPath}/css/opensocial/light-container-gadgets.css">

<script type="text/javascript" src="${Context.getBaseURL()}${Context.getBasePath()}/skin/base/script/jquery/jquery.js"></script>
<script type="text/javascript" src="${contextPath}/opensocial/gadgets/js/rpc.js?c=1"></script>
<script type="text/javascript" src="${contextPath}/js/?scripts=opensocial/cookies.js|opensocial/util.js|opensocial/gadgets.js|opensocial/cookiebaseduserprefstore.js|opensocial/jquery.opensocial.gadget.js"></script>
  
  
  <@block name="stylesheets" />
  <@block name="header_scripts" />
</head>

<body>

  		<@block name="header">
  		<div class="navbar navbar-inverse navbar-fixed-top">
      		<div class="navbar-inner">
        		<div class="container-fluid">	
          			<a class="brand" href="${Context.getBaseURL()}${Context.getBasePath()}/publication/">Publications browser</a>
          			<div class="nav-collapse collapse">
            			<p class="navbar-text pull-left">
              			Welcome <a href="#" class="navbar-link">${Context.principal}</a>
            			</p>
            			<ul class="nav">
            			<li><a class="navbar-link"href="${Context.getBaseURL()}${Context.getBasePath()}/publication/about">About</a></li>
            			
          			</div><!--/.nav-collapse -->
          			<#include "search_form.ftl">
        		</div>
      		</div>
    	</div>
    	</@block>

	<div class="container-fluid">
		<div class="row-fluid">
    		<div class="span3"><@block name="menu"><#include "menu.ftl"></@block></div>
    		<div class="span9"><@block name="content">The Content</@block></div>
		</div
	</div>

</body>
</html>
