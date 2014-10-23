var xPath = "";

$(document).ready(function() {
	$("*").not("button[id='show_cacophony_config']").not("div[id='cacophony_config']").click(function(event) {
			event.stopPropagation();
			$(this).effect('highlight', {color : "red"}, 1000)
			xPath = getElementXPath($(this).get(0));
	});

    $("button[id='show_cacophony_config']").click(function(event) {
            event.stopPropagation();
            launchCNode();
    });

    $("div[id='cacophony_config']").click(function(event) {
            event.stopPropagation();
    });
});

function launchCNode() {
	var params = {'name':'CNODE_NAME', 'url':$.QueryString['url'], 'format':'html', 'path':xPath, 'regex':'(.*)'}

	$.ajax({
			url: "//localhost/launch",
			type: 'POST',
			dataType: 'json',
			data: params,
			success: handleResults});
}

function handleResults(data, status) {
	// TODO: check value of status
	alert(data.status);
}

// From http://stackoverflow.com/questions/901115/how-can-i-get-query-string-values-in-javascript
(function($) {
    $.QueryString = (function(a) {
        if (a == "") return {};
        var b = {};
        for (var i = 0; i < a.length; ++i)
        {
            var p=a[i].split('=');
            if (p.length != 2) continue;
            b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
        }
        return b;
    })(window.location.search.substr(1).split('&'))
})(jQuery);


// THIS IS BASED ON FIREBUG CODE, UNDER THE BSD LICENSE: http://code.google.com/p/fbug/source/browse/branches/firebug1.6/content/firebug/lib.js#1294
// ************************************************************************************************
// XPath

/**
 * Gets an XPath for an element which describes its hierarchical location.
 */
var getElementXPath = function(element)
{
    if (element && element.id)
        return '//*[@id="' + element.id + '"]';
    else
        return this.getElementTreeXPath(element);
};

var getElementTreeXPath = function(element)
{
    var paths = [];

    // Use nodeName (instead of localName) so namespace prefix is included (if any).
    for (; element && element.nodeType == 1; element = element.parentNode)
    {
        var index = 0;
        for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling)
        {
            // Ignore document type declaration.
            if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE)
                continue;

            if (sibling.nodeName == element.nodeName)
                ++index;
        }

        var tagName = element.nodeName.toLowerCase();
        var pathIndex = (index ? "[" + (index+1) + "]" : "");
        paths.splice(0, 0, tagName + pathIndex);
    }

    return paths.length ? "/" + paths.join("/") : null;
};

// END OF FIREBUG XPATH CODE.
// ************************************************************************************************