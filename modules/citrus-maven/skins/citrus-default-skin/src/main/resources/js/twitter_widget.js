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