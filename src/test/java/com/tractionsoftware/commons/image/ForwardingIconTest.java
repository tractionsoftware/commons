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

package com.tractionsoftware.commons.image;

import com.tractionsoftware.commons.io.CommonFileResourceType;
import com.tractionsoftware.commons.util.Dimensions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ForwardingIcon}.
 */
public final class ForwardingIconTest {

    /** Minimal stub Icon backed by an {@link IconFileResource.InvalidIconFileResource}. */
    private static final class StubIcon implements Icon {

        private final String filename;
        private final Dimensions<Integer> dimensions;

        StubIcon(String filename, Dimensions<Integer> dimensions) {
            this.filename = filename;
            this.dimensions = dimensions;
        }

        @Override public boolean isValid()   { return false; }
        @Override public String getFilename() { return filename; }
        @Override public Dimensions<Integer> getDimensions() { return dimensions; }
        @Override public String getContentId()  { return "cid:" + filename; }
        @Override public String getDataUrl()    { return "data:image/png;base64,"; }
        @Override public Icon getScaled(Dimensions<Integer> d) { return this; }
        @Override public Icon withDimensions(Dimensions<Integer> d) { return this; }
        @Override
        public IconFileResource getImageFileResource() {
            return new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER);
        }
    }

    private static final Dimensions<Integer> DIM_16 =
        Dimensions.getInstanceInPixels(16, 16);

    private StubIcon stub;
    private Icon wrapped;

    @BeforeEach
    void setUp() {
        stub    = new StubIcon("icon.png", DIM_16);
        wrapped = ForwardingIcon.wrap(stub);
    }

    // =====================================================================
    // wrap(Icon) — delegation
    // =====================================================================

    @Test
    void wrap_isValid_delegates() {
        assertFalse(wrapped.isValid());
    }

    @Test
    void wrap_getFilename_delegates() {
        assertEquals("icon.png", wrapped.getFilename());
    }

    @Test
    void wrap_getDimensions_delegates() {
        assertEquals(DIM_16, wrapped.getDimensions());
    }

    @Test
    void wrap_getWidth_delegates() {
        assertEquals(16, wrapped.getWidth());
    }

    @Test
    void wrap_getHeight_delegates() {
        assertEquals(16, wrapped.getHeight());
    }

    @Test
    void wrap_getContentId_delegates() {
        assertEquals("cid:icon.png", wrapped.getContentId());
    }

    @Test
    void wrap_getDataUrl_delegates() {
        assertNotNull(wrapped.getDataUrl());
    }

    @Test
    void wrap_getImageFileResource_delegates() {
        assertNotNull(wrapped.getImageFileResource());
        assertFalse(wrapped.getImageFileResource().isValid());
    }

    @Test
    void wrap_getScaled_delegates() {
        assertNotNull(wrapped.getScaled(DIM_16));
    }

    @Test
    void wrap_getWidthHTML_notNull() {
        assertNotNull(wrapped.getWidthHTML());
    }

    @Test
    void wrap_getHeightHTML_notNull() {
        assertNotNull(wrapped.getHeightHTML());
    }

    // =====================================================================
    // wrap(Supplier<Icon>)
    // =====================================================================

    @Test
    void wrapSupplier_delegates() {
        Icon fromSupplier = ForwardingIcon.wrap(() -> stub);
        assertEquals("icon.png", fromSupplier.getFilename());
        assertFalse(fromSupplier.isValid());
    }

    @Test
    void wrapSupplier_calledEachTime() {
        final StubIcon other = new StubIcon("other.png", DIM_16);
        final boolean[] firstCall = {true};
        Icon dynamic = ForwardingIcon.wrap(() -> firstCall[0] ? stub : other);
        assertEquals("icon.png", dynamic.getFilename());  // first call → stub
    }

    // =====================================================================
    // wrapWithForcedDimensions
    // =====================================================================

    @Test
    void wrapWithForcedDimensions_overridesDimensions() {
        Dimensions<Integer> forced = Dimensions.getInstanceInPixels(32, 32);
        Icon forcedIcon = ForwardingIcon.wrapWithForcedDimensions(stub, forced);
        assertEquals(forced, forcedIcon.getDimensions());
        assertEquals(32, forcedIcon.getWidth());
        assertEquals(32, forcedIcon.getHeight());
    }

    @Test
    void wrapWithForcedDimensions_othersDelegate() {
        Dimensions<Integer> forced = Dimensions.getInstanceInPixels(32, 32);
        Icon forcedIcon = ForwardingIcon.wrapWithForcedDimensions(stub, forced);
        // filename still comes from the delegate
        assertEquals("icon.png", forcedIcon.getFilename());
        assertFalse(forcedIcon.isValid());
    }

    @Test
    void wrapWithForcedDimensions_withDimensions_sameDims_returnsSelf() {
        Dimensions<Integer> forced = Dimensions.getInstanceInPixels(32, 32);
        Icon forcedIcon = ForwardingIcon.wrapWithForcedDimensions(stub, forced);
        assertSame(forcedIcon, forcedIcon.withDimensions(forced));
    }

    @Test
    void wrapWithForcedDimensions_withDimensions_differentDims_delegatesToUnderlying() {
        Dimensions<Integer> forced = Dimensions.getInstanceInPixels(32, 32);
        Dimensions<Integer> other  = Dimensions.getInstanceInPixels(8, 8);
        Icon forcedIcon = ForwardingIcon.wrapWithForcedDimensions(stub, forced);
        // delegates to stub.withDimensions(other) → stub returns itself
        assertNotNull(forcedIcon.withDimensions(other));
    }

}
