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


package com.tractionsoftware.commons.config;

import jakarta.annotation.Nonnull;

public class StaticForwardingConfiguration extends ForwardingConfiguration {

    protected final Configuration config;

    public StaticForwardingConfiguration(Configuration config) {
        this.config = config;
    }

    @Nonnull
    @Override
    public String toString() {
        return "Configuration: stat fwd {" + config + "}";
    }

    @Nonnull
    @Override
    protected final Configuration delegate() {
        return config;
    }

    @Override
    protected final boolean isStaticallySpecifiedDelegate() {
        return true;
    }

}
