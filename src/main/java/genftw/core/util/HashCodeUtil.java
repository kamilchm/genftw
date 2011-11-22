/*
 * Copyright 2011 GenFTW contributors
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

package genftw.core.util;

import java.lang.reflect.Array;

/**
 * Allows easy implementation of {@link Object#hashCode() hashCode}.
 * <p>
 * Source code taken from <a href="http://www.javapractices.com/topic/TopicAction.do?Id=28">Java Practices: Implementing
 * hashCode</a>.
 */
public final class HashCodeUtil {

    private HashCodeUtil() {
        // Avoid instance creation
    }

    public static final int SEED = 23;
    private static final int ODD_PRIME_NUMBER = 37;

    public static int hash(int aSeed, boolean aBoolean) {
        return firstTerm(aSeed) + (aBoolean ? 1 : 0);
    }

    public static int hash(int aSeed, char aChar) {
        return firstTerm(aSeed) + aChar;
    }

    public static int hash(int aSeed, int aInt) {
        // Byte and short are handled by this method through implicit conversion
        return firstTerm(aSeed) + aInt;
    }

    public static int hash(int aSeed, long aLong) {
        return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
    }

    public static int hash(int aSeed, float aFloat) {
        return hash(aSeed, Float.floatToIntBits(aFloat));
    }

    public static int hash(int aSeed, double aDouble) {
        return hash(aSeed, Double.doubleToLongBits(aDouble));
    }

    public static int hash(int aSeed, Object aObject) {
        int result = aSeed;
        if (aObject == null) {
            result = hash(result, 0);
        } else if (!aObject.getClass().isArray()) {
            result = hash(result, aObject.hashCode());
        } else {
            int length = Array.getLength(aObject);
            for (int idx = 0; idx < length; ++idx) {
                Object item = Array.get(aObject, idx);
                // Recursive call
                result = hash(result, item);
            }
        }
        return result;
    }

    private static int firstTerm(int aSeed) {
        return ODD_PRIME_NUMBER * aSeed;
    }

}
