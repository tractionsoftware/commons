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

package com.tractionsoftware.commons.mail;

import com.tractionsoftware.commons.net.URLUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author Dave Shepperton
 */
public final class EmailAddress {

    private final String address;

    private final String friendlyName;

    public EmailAddress(String address, String friendlyName) {
        Objects.requireNonNull(address, "address");
        this.address = address;
        this.friendlyName = StringUtils.trimToNull(friendlyName);
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof EmailAddress otherAddress)) {
            return false;
        }
        if (address.equals(otherAddress.address) &&
            Objects.equals(friendlyName, otherAddress.friendlyName)) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(address, friendlyName);
    }

    @Override
    public final String toString() {
        return getAddressWithFriendlyName();
    }

    public final String getDisplayName() {
        if (hasFriendlyName()) {
            return friendlyName;
        }
        return address;
    }

    public final String getAddress() {
        return address;
    }

    public final boolean hasFriendlyName() {
        if (friendlyName == null) {
            return false;
        }
        return true;
    }

    public final String getFriendlyName() {
        return friendlyName;
    }

    public final String getAddressWithFriendlyName() {
        return MailUtil.makeFriendlyNameAddress(address, friendlyName);
    }

    public final String getUrl() {
        return "mailto:" + URLUtil.getUrlEncoding(address);
    }

}
