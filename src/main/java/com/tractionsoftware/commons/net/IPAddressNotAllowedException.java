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

package com.tractionsoftware.commons.net;

import com.google.common.annotations.Beta;

import java.io.Serial;
import java.security.GeneralSecurityException;

/**
 * A {@link GeneralSecurityException} representing a disallowed IP address in cases such as
 * {@link HostAddressUtil.InetAddressWrapper#checkAllowedX(HostAddressUtil.IPAddressOutgoingRequestFilterMode)}.
 *
 * @author Dave Shepperton
 */
public final class IPAddressNotAllowedException extends GeneralSecurityException {

    @Serial
    private static final long serialVersionUID = -6094122205719091311L;

    public IPAddressNotAllowedException(String message) {
        super(message);
    }

    public IPAddressNotAllowedException(Throwable cause) {
        super(cause);
    }

    public IPAddressNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

}
