
(function($){var current=null;$.fn.rssfeed=function(url,options){var defaults={limit:10,header:true,titletag:'h4',date:true,content:true,snippet:true,showerror:true,errormsg:'',key:null};var options=$.extend(defaults,options);return this.each(function(i,e){var $e=$(e);if(!$e.hasClass('rssFeed'))$e.addClass('rssFeed');if(url==null)return false;var api="http://ajax.googleapis.com/ajax/services/feed/load?v=1.0&callback=?&q="+url;if(options.limit!=null)api+="&num="+options.limit;if(options.key!=null)api+="&key="+options.key;$.getJSON(api,function(data){if(data.responseStatus==200){_callback(e,data.responseData.feed,options);}else{if(options.showerror)
if(options.errormsg!=''){var msg=options.errormsg;}else{var msg=data.responseDetails;};$(e).html('<div class="rssError"><p>'+msg+'</p></div>');};});});};var _callback=function(e,feeds,options){if(!feeds){return false;}
var html='';var row='odd';if(options.header)
html+='<div class="rssHeader">'+'<a href="'+feeds.link+'" title="'+feeds.description+'">'+feeds.title+'</a>'+'</div>';html+='<div class="rssBody">'+'<ul>';for(var i=0;i<feeds.entries.length;i++){var entry=feeds.entries[i];var entryDate=new Date(entry.publishedDate);var pubDate=entryDate.toLocaleDateString()+' '+entryDate.toLocaleTimeString();html+='<li class="rssRow '+row+'">'+'<'+options.titletag+'><a href="'+entry.link+'" title="View this feed at '+feeds.title+'">'+entry.title+'</a></'+options.titletag+'>'
if(options.date)html+='<div>'+pubDate+'</div>'
if(options.content){if(options.snippet&&entry.contentSnippet!=''){var content=entry.contentSnippet;}else{var content=entry.content;}
html+='<p>'+content+'</p>'}
html+='</li>';if(row=='odd'){row='even';}else{row='odd';}}
html+='</ul>'+'</div>'
$(e).html(html);};})(jQuery);