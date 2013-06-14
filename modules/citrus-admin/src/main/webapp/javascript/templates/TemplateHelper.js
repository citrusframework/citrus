/*
 * Extend String prototype with hashCode function known from Java
 */
String.prototype.hashCode = function(){
    var hash = 0;
    if (this.length == 0) return hash;
    for (i = 0; i < this.length; i++) {
        char = this.charCodeAt(i);
        hash = ((hash<<5)-hash)+char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}

/*
 * Helper for formatting milliseconds to date values
 * By default JSON maps dates to milliseconds, this helper formats the date
 * values using moment.js (default format is provided as 'YYYY-MM-DD HH:mm:ss')
 */
Handlebars.registerHelper('dateFormat', function(context, block) {
    if (context == null) {
        return '';
    }
    if (window.moment) {
      var formatString = block.hash.format || "YYYY-MM-DD HH:mm:ss";
      return moment(new Date(context)).format(formatString);
    } else{
      return context; // moment plugin not available. return data as is.
    };
});

/**
 * If Equals
 * if_eq this compare=that
 */
Handlebars.registerHelper('if_eq', function(context, options) {
    if (context == options.hash.compare)
        return options.fn(this);
    return options.inverse(this);
});

/**
 * Unless Equals
 * unless_eq this compare=that
 */
Handlebars.registerHelper('unless_eq', function(context, options) {
    if (context == options.hash.compare)
        return options.inverse(this);
    return options.fn(this);
});

/*
 * Helper to truncate the string to the specified length
 */
Handlebars.registerHelper('truncate', function(message, block) {
    if (message == null) {
        return '';
    }
    message = String(message);
    truncateStr = '...';
    truncateAfter = block.hash.length;
    return message.length > truncateAfter ? message.slice(0, truncateAfter) + truncateStr : message;
});

/*
 * Helper pretty prints XML fragments with indentation and new lines
 */
Handlebars.registerHelper('prettyPrintXml', function(context, block) {
    if (context == null) {
        return '';
    }
    var formatted = '';
    var reg = /(>)(<)(\/*)/g;
    var xml = context.replace(reg, '$1\r\n$2$3');
    var pad = 0;
    jQuery.each(xml.split('\r\n'), function(index, node) {
        var indent = 0;
        if (node.match( /.+<\/\w[^>]*>$/ )) {
            indent = 0;
        } else if (node.match( /^<\/\w/ )) {
            if (pad != 0) {
                pad -= 1;
            }
        } else if (node.match( /^<\w[^>]*[^\/]>.*$/ )) {
            indent = 1;
        } else {
            indent = 0;
        }

        var padding = '';
        for (var i = 0; i < pad; i++) {
            padding += '  ';
        }

        formatted += padding + node + '\r\n';
        pad += indent;
    });

    return formatted;
});

/*
 * Helper constructs a Handlebars variable expression for nested tempaltes
 */
Handlebars.registerHelper('var', function(block) {
    return '{{' + block.hash.name + '}}'; 
});