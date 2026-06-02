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

import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.properties.GetProperties;
import com.tractionsoftware.commons.properties.GetProperty;
import com.tractionsoftware.commons.util.CollectionsUtil;

import jakarta.annotation.Nonnull;
import jakarta.mail.Header;
import jakarta.mail.internet.InternetAddress;
import java.util.*;

/**
 * Encapsulates a set of email headers, which may come from an incoming or outgoing message, or some other source where
 * they were recorded.
 *
 * @author Andy Keller, Dave Shepperton
 * @see MailUtil#createDynamicEmailHeaders(jakarta.mail.internet.MimeMessage)
 * @see MailUtil#createEmailHeaders(jakarta.mail.internet.MimeMessage)
 * @see MailUtil#createEmailHeaders(Iterable, boolean)
 * @see MailUtil#createEmailHeaders(com.google.common.collect.Multimap, boolean)
 * @see MailUtil#createEmailHeadersFromRawLines(Iterable)
 */
public interface EmailHeaders extends Iterable<Header> {

    /**
     * Per
     * <a href="https://tools.ietf.org/html/rfc5322#section-3.6.4">RFC
     * 5322 section 3.6.4</a>, this header represents a unique identifier for a message, which really should be
     * required:
     *
     * <blockquote>Though listed as optional in the table in section
     * 3.6, every message SHOULD have a "Message-ID:" field.</blockquote>
     */
    public static final String NAME_MESSAGE_ID = "Message-ID";

    public static final String NAME_SUBJECT = "Subject";

    public static final String NAME_DATE = "Date";

    public static final String NAME_FROM = "From";

    public static final String NAME_TO = "To";

    public static final String NAME_CC = "Cc";

    public static final String NAME_BCC = "Bcc";

    public static final String NAME_REPLY_TO = "Reply-To";

    /**
     * Per
     * <a href="https://tools.ietf.org/html/rfc5322#section-3.6.4">RFC
     * 5322 section 3.6.4</a>, this header refers to the {@link #NAME_MESSAGE_ID} of another message which indicates the
     * target a reply, and should be present on a message that represents a reply.
     *
     * <blockquote>Furthermore, reply messages SHOULD have
     * "In-Reply-To:" and "References:" fields as appropriate ...</blockquote>
     *
     * <p>
     * In practice, it seems that many reply messages do not have "In-Reply-To" headers by the time they appear in a
     * mailbox, and a reply target may instead have to be inferred by examining {@link #NAME_REFERENCES} headers.
     */
    public static final String NAME_IN_REPLY_TO = "In-Reply-To";

    /**
     * Per
     * <a href="https://tools.ietf.org/html/rfc5322#section-3.6.4">RFC
     * 5322 section 3.6.4</a>, these headers (there are often multiple such headers) refer to the {@link
     * #NAME_MESSAGE_ID}s of one or more other messages, indicating the target a reply or previous messages in a thread,
     * and should be present on a message that represents a reply.
     *
     * <blockquote>Furthermore, reply messages SHOULD have
     * "In-Reply-To:" and "References:" fields as appropriate ...</blockquote>
     */
    public static final String NAME_REFERENCES = "References";

    /**
     * Per <a href="https://tools.ietf.org/search/rfc3834">RFC 3834</a>, this header should be used an address to which
     * an automatically generated reply could be sent, but is meant to be set by the SMTP server, not the client:
     *
     * <blockquote>In general, automatic responses SHOULD be sent to
     * the Return-Path field if generated after delivery.</blockquote>
     */
    public static final String NAME_RETURN_PATH = "Return-Path";

    /**
     * Per
     * <a href="https://tools.ietf.org/search/rfc3834">RFC 3834</a>,
     * this header used to indicate whether
     *
     * <blockquote>The purpose of the Auto-Submitted header field is
     * to indicate that the message was originated by an automatic process, or an automatic responder, rather than by a
     * human; and to facilitate automatic filtering of messages from signal paths for which automatically generated
     * messages and automatic responses are not desirable.</blockquote>
     *
     * @see MailUtil#setHeadersForAutomaticallyGeneratedMessage(jakarta.mail.Message, boolean)
     * @see MailUtil#setHeadersForAutomaticallyGeneratedReplyMessage(jakarta.mail.Message, boolean)
     */
    public static final String NAME_AUTO_SUBMITTED = "Auto-Submitted";

    /**
     * This is the header name used by some email services to indicate the address of the mailbox to which a message was
     * delivered, or in some cases, like the {@link #NAME_DELIVERED_TO} header, may contain an address that came from a
     * {@link #NAME_BCC} header that appeared on the original message.
     */
    public static final String NAME_X_DELIVERED_TO = "X-Delivered-To";

    /**
     * Headers with this name serve the same purpose as the {@link #NAME_X_DELIVERED_TO} header, but without the
     * experimental "X-" prefix on the name (even though it does not seem to be an officially recognized standard header
     * name). Like X-Delivered-To, it will generally contain the address of the mailbox to which a message was
     * delivered, or in some cases, it may contain an address that came from a {@link #NAME_BCC} header that appeared on
     * the original message.
     */
    public static final String NAME_DELIVERED_TO = "Delivered-To";

    /**
     * Per <a href="https://tools.ietf.org/html/rfc4021">RFC 4021</a>, this header represents a "description of message
     * body part."
     */
    public static final String NAME_CONTENT_DESCRIPTION = "Content-Description";

    /**
     * Per <a href="https://tools.ietf.org/html/rfc2045#section-6">RFC 2045 section 6</a>, this header is used to
     * indicate the encoding that was used to represent a particular part of the message, since not all data can be sent
     * via email in its natural unencoded state.
     */
    public static final String NAME_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

    /**
     * This non-standard (but widely supported) header is generally used to send a "text/calendar" type body part
     * representing data about a calendar event.
     */
    public static final String NAME_CONTENT_CLASS = "Content-Class";

    /**
     * Per <a href="https://tools.ietf.org/html/rfc2045#section-7">RFC 2045 section 7</a>, this header allows an
     * identifier to be applied to a message part, which in turn allows that part to be referenced in another part.
     */
    public static final String NAME_CONTENT_ID = "Content-ID";

    public static final String NAME_CONTENT_BASE = "Content-Base";

    public static final String NAME_CONTENT_LOCATION = "Content-Location";

    public static final String NAME_MIME_VERSION = "MIME-Version";

    public static final String NAME_CONTENT_TYPE = "Content-Type";

    /**
     * The "X-Original-To" header is supported by
     * <a href="http://www.postfix.org/virtual.8.html">postfix</a>,
     * and according to should indicate "the original recipient of the email that was received." See also {@link
     * #NAME_DELIVERED_TO} and {@link #NAME_X_DELIVERED_TO}.
     */
    public static final String NAME_X_ORIGINAL_TO = "X-Original-To";

    /**
     * This is a Microsoft Exchange specific header. See <a href= "http://msdn.microsoft.com/en-us/library/ee219609(v=exchg.80).aspx">this
     * page</a> for some information.
     *
     * @see MailUtil#setHeadersForAutomaticallyGeneratedMessageExchange(jakarta.mail.Message)
     */
    public static final String NAME_X_AUTO_RESPONSE_SUPPRESS = "X-Auto-Response-Suppress";

    /**
     * This header is set on most messages automatically generated by TeamPage. It will contain the published base URL
     * of the server.
     */
    public static final String NAME_X_TRACTION_SERVER = "X-Traction-Server";

    public static final String AUTO_SUBMITTED_VALUE_AUTO_GENERATED = "auto-generated";

    public static final String AUTO_SUBMITTED_VALUE_AUTO_REPLIED = "auto-replied";

    public static final String X_AUTO_RESPONSE_SUPPRESS_VALUE_ALL = "All";

    /**
     * Returns the email header value for the given name if at least one exists exists. If multiple values exist, this
     * method should return a String representing all the values concatenated together, separated by a comma and space.
     *
     * <p>
     * This default implementation uses {@link #getHeaders(String)} to retrieve the List of values, returning null if
     * the List is empty, and otherwise returning a concatenation of the values separated by a comma and space. It
     * should be suitable for most implementations, but subclasses should override it as necessary.
     *
     * @return the email header value for the given name if at least one exists; null otherwise.
     */
    public default String getHeader(String name) {
        List<String> valuesForName = getHeaders(name);
        if (valuesForName.isEmpty()) {
            return null;
        }
        return StringUtil.join(valuesForName, ", ");
    }

    /**
     * Returns all email header values for the given name, if any exist.
     *
     * @return all email header values for the given name, if any exist; an empty List otherwise.
     */
    public List<String> getHeaders(String name);

    /**
     * Returns true if this EmailHeaders has at least one header corresponding to the requested name.
     *
     * <p>
     * This default implementation returns true if the {@link List} returned by {@link #getHeaders(String)} {@link
     * List#isEmpty() is empty}. It should be suitable for most implementations, but subclasses should override it as
     * necessary.
     *
     * @param name
     *     the name of the header.
     * @return true if this EmailHeaders has at least one header corresponding to the requested name; false otherwise.
     */
    public default boolean hasHeader(String name) {
        if (getHeaders(name).isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Returns the raw header lines associated with the underlying message. "raw" here means that the header values have
     * not been encoded according to RFC 2047, or otherwise processed, and would need to be decoded for display
     * purposes.
     *
     * @return the raw header lines, if any, associated with the underlying message.
     * @throws UnsupportedOperationException
     *     if the raw header lines are not available, although implementations should make every effort to support it.
     */
    public Iterable<String> getRawHeaderLines();

    /**
     * Returns the {@link InternetAddress}es that were parsed from the header.
     *
     * <p>
     * This default implementation defers to {@link MailUtil#parseAddresses(String)}.
     *
     * @return the {@link InternetAddress}es that were parsed from the header, or null if no such header existed or
     *     there was some failure parsing the addresses.
     */
    public default List<? extends InternetAddress> getAddresses(String headerName) {
        return MailUtil.safeParseAddresses(getHeader(headerName));
    }

    /**
     * Returns an {@link Iterator} covering all {@link Header}s.
     *
     * @return an {@link Iterator} covering all {@link Header}s.
     */
    @Nonnull
    @Override
    public Iterator<Header> iterator();

    /**
     * Returns a {@link GetProperty} that provides access to these email headers, treating header names as property
     * names, and header values as the corresponding property values. If there are multiple headers with the same name,
     * {@link GetProperty#getProperty(String)} will return the values concatenated together.
     *
     * <p>
     * The returned GetProperty should represent a dynamic view of the current state of the headers at the time its
     * methods are invoked.
     *
     * @return a {@link GetProperty} that provides access to these email headers as property name-value pairs.
     */
    public default GetProperty asGetProperty() {

        return new GetProperty() {

            @Override
            public final String getProperty(String propName) {
                List<String> values = EmailHeaders.this.getHeaders(propName);
                if (CollectionsUtil.isEmpty(values)) {
                    return null;
                }
                return NativeTypeConversion.iterableToString(values);
            }

            @Override
            public final Set<String> getPropertyNames() {
                Set<String> allNames = new LinkedHashSet<>();
                for (Header header : EmailHeaders.this) {
                    allNames.add(header.getName());
                }
                return allNames;
            }

            @Override
            public final boolean hasProperty(String propName) {
                return EmailHeaders.this.hasHeader(propName);
            }

        };

    }

    /**
     * Returns a {@link GetProperties} that provides access to these email headers, treating header names as property
     * names, and header values as the corresponding property values.
     *
     * <p>
     * The returned GetProperties should represent a dynamic view of the current state of the headers at the time its
     * methods are invoked.
     *
     * @return a {@link GetProperties} that provides access to these email headers, treating header names as property
     *     names, and header values as the corresponding property values.
     */
    public default GetProperties<String> asGetProperties() {

        return new GetProperties<>() {

            @Override
            public final String toString() {
                return "properties for " + EmailHeaders.this;
            }

            @Override
            public final boolean getProperties(String name, Collection<? super String> list) {
                List<String> values = EmailHeaders.this.getHeaders(name);
                if (values == null) {
                    return false;
                }
                list.addAll(values);
                return true;
            }

        };

    }

}
