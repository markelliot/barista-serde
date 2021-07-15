/* This file is derived in part from
 * https://github.com/palantir/conjure/blob/79fb5b6ac52d17500a01c01b5d8df19bb4deb108\
 * /conjure-core/src/main/java/com/palantir/parsec/StringParserState.java
 *
 * And those derivations are subject to the following notice:
 *
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package barista.serde.runtime.parsec;

public final class ParseState {

    /** End of stream sentinel value. */
    public static final int EOS = -1;

    private final CharSequence seq;
    private int index = 0;

    public ParseState(CharSequence seq) {
        this.seq = seq;
    }

    public static ParseState of(String str) {
        return new ParseState(str);
    }

    /** Returns current character in the stream. */
    public int current() {
        return index < seq.length() ? seq.charAt(index) : EOS;
    }

    public boolean isEndOfStream() {
        return index == seq.length();
    }

    /**
     * Moves the underlying index forward and then returns the newly current character in the
     * stream.
     */
    public int next() {
        index = Math.min(index + 1, seq.length());
        return current();
    }

    /** Returns a pointer to the current index. */
    public Mark mark() {
        return new Mark(index);
    }

    /** Resets the index to the provided mark. */
    public void rewind(Mark mark) {
        index = mark.markIndex;
    }

    public CharSequence slice(Mark from) {
        return seq.subSequence(from.markIndex, index);
    }

    public final class Mark {
        private final int markIndex;

        private Mark(int index) {
            this.markIndex = index;
        }

        public ParseError error(String message) {
            int markAdjustment = 0;
            // we need to "adjust" markIndex if it lands on a line break or the end of the stream
            // (where "adjustment" causes the error column to refer to one column past the actual
            // line length)
            if ((markIndex < seq.length() && seq.charAt(markIndex) == '\n')
                    || markIndex == seq.length()) {
                markAdjustment = 1;
            }
            int adjustedMarkIndex = Math.max(markIndex - markAdjustment, 0);

            int lineNumber = 1;
            int columnNumber = 1;
            int markStartLineCharIndex = 0;
            for (int i = 0; i < adjustedMarkIndex; i++) {
                if (seq.charAt(i) == '\n') {
                    lineNumber++;
                    columnNumber = 1;
                    markStartLineCharIndex = i;
                } else {
                    columnNumber++;
                }
            }

            int markEndLineCharIndex = seq.length();
            int errorColumnNumber = columnNumber;
            for (int i = adjustedMarkIndex; i < seq.length(); i++) {
                if (seq.charAt(i) == '\n') {
                    markEndLineCharIndex = i;
                    break;
                }
                if (i < index) {
                    errorColumnNumber++;
                }
            }

            String contextLines =
                    seq.subSequence(markStartLineCharIndex, markEndLineCharIndex).toString();

            return new ParseError(
                    lineNumber, columnNumber, errorColumnNumber, contextLines, message);
        }
    }
}
