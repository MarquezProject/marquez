/* jshint laxcomma: true */
var utils = require('./utils');

exports.ExclusiveCanonicalization = ExclusiveCanonicalization;
exports.ExclusiveCanonicalizationWithComments = ExclusiveCanonicalizationWithComments;

function ExclusiveCanonicalization() {
	this.includeComments = false;
};

ExclusiveCanonicalization.prototype.attrCompare = function(a,b) {
  if (!a.namespaceURI && b.namespaceURI) { return -1; }
  if (!b.namespaceURI && a.namespaceURI) { return 1; }

  var left = a.namespaceURI + a.localName
  var right = b.namespaceURI + b.localName

  if (left===right) return 0
  else if (left<right) return -1
  else return 1

};

ExclusiveCanonicalization.prototype.nsCompare = function(a,b) {
  var attr1 = a.prefix;
  var attr2 = b.prefix;
  if (attr1 == attr2) { return 0; }
  return attr1.localeCompare(attr2);
};

ExclusiveCanonicalization.prototype.renderAttrs = function(node, defaultNS) {
  var a, i, attr
    , res = []
    , attrListToRender = [];

  if (node.nodeType===8) { return this.renderComment(node); }

  if (node.attributes) {
    for (i = 0; i < node.attributes.length; ++i) {
      attr = node.attributes[i];
      //ignore namespace definition attributes
      if (attr.name.indexOf("xmlns") === 0) { continue; }
      attrListToRender.push(attr);
    }
  }

  attrListToRender.sort(this.attrCompare);

  for (a in attrListToRender) {
    if (!attrListToRender.hasOwnProperty(a)) { continue; }

    attr = attrListToRender[a];
    res.push(" ", attr.name, '="', utils.encodeSpecialCharactersInAttribute(attr.value), '"');
  }

  return res.join("");
};

function isPrefixInScope(prefixesInScope, prefix, namespaceURI)
{
  var ret = false;
  prefixesInScope.forEach(function (pf) {
    if (pf.prefix === prefix && pf.namespaceURI === namespaceURI) {
      ret = true;
    }
  })

  return ret;
}

/**
 * Create the string of all namespace declarations that should appear on this element
 *
 * @param {Node} node. The node we now render
 * @param {Array} prefixesInScope. The prefixes defined on this node
 *                parents which are a part of the output set
 * @param {String} defaultNs. The current default namespace
 * @return {String}
 * @api private
 */
ExclusiveCanonicalization.prototype.renderNs = function(node, prefixesInScope, defaultNs, defaultNsForPrefix, inclusiveNamespacesPrefixList) {
  var a, i, p, attr
    , res = []
    , newDefaultNs = defaultNs
    , nsListToRender = []
    , currNs = node.namespaceURI || "";

  //handle the namespaceof the node itself
  if (node.prefix) {
    if (!isPrefixInScope(prefixesInScope, node.prefix, node.namespaceURI || defaultNsForPrefix[node.prefix])) {
      nsListToRender.push({"prefix": node.prefix, "namespaceURI": node.namespaceURI || defaultNsForPrefix[node.prefix]});
      prefixesInScope.push({"prefix": node.prefix, "namespaceURI": node.namespaceURI || defaultNsForPrefix[node.prefix]});
    }
  }
  else if (defaultNs!=currNs) {
      //new default ns
      newDefaultNs = node.namespaceURI;
      res.push(' xmlns="', newDefaultNs, '"');
  }

  //handle the attributes namespace
  if (node.attributes) {
    for (i = 0; i < node.attributes.length; ++i) {
      attr = node.attributes[i];

      //handle all prefixed attributes that are included in the prefix list and where
      //the prefix is not defined already
      if (attr.prefix && !isPrefixInScope(prefixesInScope, attr.localName, attr.value) && inclusiveNamespacesPrefixList.indexOf(attr.localName) >= 0) {
        nsListToRender.push({"prefix": attr.localName, "namespaceURI": attr.value});
        prefixesInScope.push({"prefix": attr.localName, "namespaceURI": attr.value});
      }

      //handle all prefixed attributes that are not xmlns definitions and where
      //the prefix is not defined already
      if (attr.prefix && !isPrefixInScope(prefixesInScope, attr.prefix, attr.namespaceURI) && attr.prefix!="xmlns" && attr.prefix!="xml") {
        nsListToRender.push({"prefix": attr.prefix, "namespaceURI": attr.namespaceURI});
        prefixesInScope.push({"prefix": attr.prefix, "namespaceURI": attr.namespaceURI});
      }
    }
  }

  nsListToRender.sort(this.nsCompare);

  //render namespaces
  for (a in nsListToRender) {
    if (!nsListToRender.hasOwnProperty(a)) { continue; }

    p = nsListToRender[a];
    res.push(" xmlns:", p.prefix, '="', p.namespaceURI, '"');
  }

  return {"rendered": res.join(""), "newDefaultNs": newDefaultNs};
};

ExclusiveCanonicalization.prototype.processInner = function(node, prefixesInScope, defaultNs, defaultNsForPrefix, inclusiveNamespacesPrefixList) {

  if (node.nodeType === 8) { return this.renderComment(node); }
  if (node.data) { return utils.encodeSpecialCharactersInText(node.data); }

  var i, pfxCopy
    , ns = this.renderNs(node, prefixesInScope, defaultNs, defaultNsForPrefix, inclusiveNamespacesPrefixList)
    , res = ["<", node.tagName, ns.rendered, this.renderAttrs(node, ns.newDefaultNs), ">"];

  for (i = 0; i < node.childNodes.length; ++i) {
    pfxCopy = prefixesInScope.slice(0);
    res.push(this.processInner(node.childNodes[i], pfxCopy, ns.newDefaultNs, defaultNsForPrefix, inclusiveNamespacesPrefixList));
  }

  res.push("</", node.tagName, ">");
  return res.join("");
};

// Thanks to deoxxa/xml-c14n for comment renderer
ExclusiveCanonicalization.prototype.renderComment = function (node) {

  if (!this.includeComments) { return ""; }

  var isOutsideDocument = (node.ownerDocument === node.parentNode),
      isBeforeDocument = null,
      isAfterDocument = null;

  if (isOutsideDocument) {
    var nextNode = node,
        previousNode = node;

    while (nextNode !== null) {
      if (nextNode === node.ownerDocument.documentElement) {
        isBeforeDocument = true;
        break;
      }

      nextNode = nextNode.nextSibling;
    }

    while (previousNode !== null) {
      if (previousNode === node.ownerDocument.documentElement) {
        isAfterDocument = true;
        break;
      }

      previousNode = previousNode.previousSibling;
    }
  }

  return (isAfterDocument ? "\n" : "") + "<!--" + utils.encodeSpecialCharactersInText(node.data) + "-->" + (isBeforeDocument ? "\n" : "");
};

/**
 * Perform canonicalization of the given node
 *
 * @param {Node} node
 * @return {String}
 * @api public
 */
ExclusiveCanonicalization.prototype.process = function(node, options) {
  options = options || {};
  var inclusiveNamespacesPrefixList = options.inclusiveNamespacesPrefixList || [];
  var defaultNs = options.defaultNs || "";
  var defaultNsForPrefix = options.defaultNsForPrefix || {};
  if (!(inclusiveNamespacesPrefixList instanceof Array)) { inclusiveNamespacesPrefixList = inclusiveNamespacesPrefixList.split(' '); }

  var ancestorNamespaces = options.ancestorNamespaces || [];
  
  /**
   * If the inclusiveNamespacesPrefixList has not been explicitly provided then look it up in CanonicalizationMethod/InclusiveNamespaces
   */  
  if (inclusiveNamespacesPrefixList.length == 0) {
    var CanonicalizationMethod = utils.findChilds(node, "CanonicalizationMethod")
    if (CanonicalizationMethod.length != 0) {
      var inclusiveNamespaces = utils.findChilds(CanonicalizationMethod[0], "InclusiveNamespaces")
      if (inclusiveNamespaces.length != 0) {
          inclusiveNamespacesPrefixList = inclusiveNamespaces[0].getAttribute('PrefixList').split(" ");
      }
    }
  }
  
  /**
   * If you have a PrefixList then use it and the ancestors to add the necessary namespaces
   */
  if (inclusiveNamespacesPrefixList) {
    var prefixList = inclusiveNamespacesPrefixList instanceof Array ? inclusiveNamespacesPrefixList : inclusiveNamespacesPrefixList.split(' ');
    prefixList.forEach(function (prefix) {
      if (ancestorNamespaces)  {
        ancestorNamespaces.forEach(function (ancestorNamespace) {
          if (prefix == ancestorNamespace.prefix) {
            node.setAttributeNS('http://www.w3.org/2000/xmlns/', 'xmlns:' + prefix, ancestorNamespace.namespaceURI);
          }
        })
      }
    })
  }

  var res = this.processInner(node, [], defaultNs, defaultNsForPrefix, inclusiveNamespacesPrefixList);
  return res;
};

ExclusiveCanonicalization.prototype.getAlgorithmName = function() {
  return "http://www.w3.org/2001/10/xml-exc-c14n#";
};

// Add c14n#WithComments here (very simple subclass)
exports.ExclusiveCanonicalizationWithComments = ExclusiveCanonicalizationWithComments;

function ExclusiveCanonicalizationWithComments() {
	ExclusiveCanonicalization.call(this);
	this.includeComments = true;
};

ExclusiveCanonicalizationWithComments.prototype = Object.create(ExclusiveCanonicalization.prototype);

ExclusiveCanonicalizationWithComments.prototype.constructor = ExclusiveCanonicalizationWithComments;

ExclusiveCanonicalizationWithComments.prototype.getAlgorithmName = function() {
  return "http://www.w3.org/2001/10/xml-exc-c14n#WithComments";
};
