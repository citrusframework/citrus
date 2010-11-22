new TWTR.Widget({
	version: 2,
	type: 'profile',
	rpp: 3,
	interval: 6000,
	width: 200,
	height: 300,
	theme: {
	  shell: {
	    background: '#FAFAFA',
	    color: '#000000'
	  },
	  tweets: {
	    background: '#FFFFFF',
	    color: '#000000',
	    links: '##FF8805'
	  }
	},
	features: {
	  scrollbar: false,
	  loop: false,
	  live: false,
	  hashtags: true,
	  timestamp: true,
	  avatars: false,
	  behavior: 'all'
	}
}).render().setUser('citrus_test').start();

$(document).ready(function () {
	$("#twtr-widget-1").hide();
    
    $("#twtr_slider_in").click(function() { 
      $("#twtr_slider_in").hide("slide",{},100, function() {
          $("#twtr-widget-1").toggle("slide",{},300, function() {
              $("#twtr_slider_out").fadeIn(100);
          });    
      });
    });
    
    $("#twtr_slider_out").click(function() { 
      $("#twtr_slider_out").fadeOut(100, function() {
          $("#twtr-widget-1").toggle("slide",{},300, function() {
              $("#twtr_slider_in").show("slide",{},100);
          });    
      });
    });
    
    $("#twtr_slider_in").show("slide",{},100);
});