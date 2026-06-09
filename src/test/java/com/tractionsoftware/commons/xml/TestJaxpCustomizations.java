package com.tractionsoftware.commons.xml;

import org.xml.sax.EntityResolver;

/** Minimal Customizations implementation for unit tests — no custom entity resolver. */
public final class TestJaxpCustomizations implements JaxpUtil.Customizations {
    @Override
    public EntityResolver getPreferredResolver() {
        return null;
    }
}
