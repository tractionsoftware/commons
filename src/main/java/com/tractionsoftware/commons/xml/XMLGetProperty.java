/*
 *
 *    Copyright 1996-2026 Traction Software, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.

package com.tractionsoftware.commons.xml;

import com.tractionsoftware.commons.properties.GetProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is a very basic implementation that gets properties by XML tag name.
 *
 * @example <pre>
 *             <xml>
 *             ...
 *             <message>This is a message</message>
 *             ...
 *
 *             XMLGetProperty xml = new XMLGetProperty(myXmlDocument);
 *             return getProperty("message"); // "This is a message"
 *             </pre>
 */
public class XMLGetProperty implements GetProperty {

    private final Document dom;

    public XMLGetProperty(Document dom) {
        this.dom = dom;
    }

    @Override
    public String getProperty(String name) {
        return getNodeValueForTagName(name);
    }

    @Override
    public final Set<String> getPropertyNames() {
        Set<String> allNames = new LinkedHashSet<>();
        JaxpUtil.safelyTraverseDescendants(dom, (n) -> allNames.add(n.getNodeName()));
        return allNames;
    }

    protected final String getNodeValueForTagName(String tagName) {
        if (tagName == null) {
            return null;
        }
        NodeList tags = dom.getElementsByTagName(tagName);
        if (tags == null || tags.getLength() == 0) {
            return null;
        }
        Node tag = tags.item(0);
        if (tag.hasChildNodes()) {
            return tag.getFirstChild().getNodeValue();
        }
        return null;
    }

}
