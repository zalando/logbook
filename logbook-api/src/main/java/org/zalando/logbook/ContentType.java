package org.zalando.logbook;

import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;


@UtilityClass
class ContentType {

    @Nullable
    String parseMimeType(@Nullable String contentTypeValue) {
        if (contentTypeValue != null) {
            int indexOfSemicolon = contentTypeValue.indexOf(SEMICOLON);
            if (indexOfSemicolon != -1) {
                return contentTypeValue.substring(0, indexOfSemicolon);
            } else {
                return contentTypeValue.length() > 0 ? contentTypeValue : null;
            }
        }
        return null;
    }

    @Nullable
    Charset parseCharset(@Nullable String contentTypeValue) {
        if (contentTypeValue != null) {
            String charsetRaw = null;

            int indexOfCharset = contentTypeValue.toLowerCase().indexOf(CHARSET_PREFIX);
            if (indexOfCharset != -1) {
                int indexOfEncoding = indexOfCharset + CHARSET_PREFIX.length();
                if (indexOfEncoding < contentTypeValue.length()) {
                    String charsetCandidate = contentTypeValue.substring(indexOfEncoding);
                    int indexOfSemicolon = charsetCandidate.indexOf(SEMICOLON);
                    charsetRaw = indexOfSemicolon == -1 ? charsetCandidate : charsetCandidate.substring(0, indexOfSemicolon);
                }
            }

            if (charsetRaw != null) {
                if (charsetRaw.length() > 2) {
                    if (charsetRaw.charAt(0) == '"' && charsetRaw.charAt(charsetRaw.length() - 1) == '"') {
                        charsetRaw = charsetRaw.substring(1, charsetRaw.length() - 1);
                    }
                }
                try {
                    return Charset.forName(charsetRaw);
                } catch (IllegalCharsetNameException | UnsupportedCharsetException ignored) {
                    // ignore
                }
            }
        }
        return null;
    }

    static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String SEMICOLON = ";";
    private static final String CHARSET_PREFIX = "charset=";
}
