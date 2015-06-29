function doMoreLikeThisSearch(id) {

  var esClient= new jQuery.es.Client({
    hosts: {
            protocol: window.location.protocol,
            host: window.location.hostname,
            port: window.location.port.length != 0 ? window.location.port : 80,
            path: '/nuxeo/site/es',
            headers: {
                'Content-Type' : 'application/json'
            }
    }
  });

  esClient.search({
    index: 'nuxeo',
    body: {
     fields: ["_source"],
     size : 3,
     query : {
        "bool": {
          "should": [{
               "more_like_this" : {
                "fields" : ["dc:title.fulltext"],
                "docs" : [{
                  "_index" : "nuxeo",
                  "_type" : "doc",
                  "_id" : id}],
                "min_term_freq" : 1,
                "min_word_length" : 5,
                "min_doc_freq" : 3,
                "boost" : 3
              }},{
              "more_like_this" : {
                "docs" : [{
                  "_index" : "nuxeo",
                  "_type" : "doc",
                  "_id" : id}],
                "min_term_freq" : 1,
                "max_query_terms" : 25,
              }}]
        }
      }
    }
  }).then(callbackSearch, function (err) {
      console.trace(err.message);
  });
};

function callbackSearch(resp) {
    resp.hits.hits.forEach(function(doc) {
        var source = doc["_source"];
        var id = doc["_id"];
        var root = jQuery('#similarDocs');

        var style =
            "background-image:url('"+
            window.location.protocol+'//'+
            window.location.host+'/nuxeo/nxthumb/default/'+
            id+"/blobholder:0/"+
            "');";
        var img = jQuery('<div>').addClass('thumbnailContainer').attr('style',style);

        var link = window.location.protocol+'//'+
            window.location.host+'/nuxeo/nxpath/'+
            source["ecm:repository"]+
            source["ecm:path"]+"@view_documents?tabIds=%3A";

        var href = jQuery('<a>').attr('href',link).text(source["dc:title"]);

        var mainDiv = jQuery('<div>').addClass('bubbleBox bubbleListing');

        img.appendTo(mainDiv);
        href.appendTo(mainDiv);
        mainDiv.appendTo(root);
    });
};
