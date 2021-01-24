/**
 * Returns the character (Unicode code point) at the specified
 * index. The index refers to `char` values
 * (Unicode code units) and ranges from `0` to
 * [.length]` - 1`.
 *
 *
 *  If the `char` value specified at the given index
 * is in the high-surrogate range, the following index is less
 * than the length of this `String`, and the
 * `char` value at the following index is in the
 * low-surrogate range, then the supplementary code point
 * corresponding to this surrogate pair is returned. Otherwise,
 * the `char` value at the given index is returned.
 *
 * @param      index the index to the `char` values
 * @return     the code point value of the character at the
 * `index`
 * @exception  IndexOutOfBoundsException  if the `index`
 * argument is negative or not less than the length of this
 * string.
 * @since      1.5
 */
fun String.codePointAt(index: Int): Int {
    if (index < 0 || index >= length) {
        throw IndexOutOfBoundsException("Index [$index] is out of bounds")
    }
    return toCharArray().codePointAtImpl(index, length)
}


// throws ArrayIndexOutOfBoundsException if index out of bounds
fun CharArray.codePointAtImpl(index: Int, limit: Int): Int {
    var _index = index
    val c1 = get(_index)
    if (isHighSurrogate(c1) && ++_index < limit) {
        val c2 = get(_index)
        if (isLowSurrogate(c2)) {
            return c1.toCodePoint(c2)
        }
    }
    return c1.toInt()
}

/**
 * Converts the specified surrogate pair to its supplementary code
 * point value. This method does not validate the specified
 * surrogate pair. The caller must validate it using [ ][.isSurrogatePair] if necessary.
 *
 * @param  low the low-surrogate code unit
 * @return the supplementary code point composed from the
 * specified surrogate pair.
 * @since  1.5
 */
fun Char.toCodePoint(low: Char): Int {
    // Optimized form of:
    // return ((high - MIN_HIGH_SURROGATE) << 10)
    //         + (low - MIN_LOW_SURROGATE)
    //         + MIN_SUPPLEMENTARY_CODE_POINT;
    return (toInt() shl 10) + low.toInt() + (MIN_SUPPLEMENTARY_CODE_POINT
            - (Char.Companion.MIN_HIGH_SURROGATE.toInt() shl 10)
            - Char.Companion.MIN_LOW_SURROGATE.toInt())
}

/**
 * Determines if the given `char` value is a
 * [
 * Unicode high-surrogate code unit](http://www.unicode.org/glossary/#high_surrogate_code_unit)
 * (also known as *leading-surrogate code unit*).
 *
 *
 * Such values do not represent characters by themselves,
 * but are used in the representation of
 * [supplementary characters](#supplementary)
 * in the UTF-16 encoding.
 *
 * @param  ch the `char` value to be tested.
 * @return `true` if the `char` value is between
 * [.MIN_HIGH_SURROGATE] and
 * [.MAX_HIGH_SURROGATE] inclusive;
 * `false` otherwise.
 * @see Character.isLowSurrogate
 * @see Character.UnicodeBlock.of
 * @since  1.5
 */
fun isHighSurrogate(ch: Char): Boolean {
    return ch >= Char.Companion.MIN_HIGH_SURROGATE && ch < Char.Companion.MAX_HIGH_SURROGATE + 1
}

/**
 * Determines if the given `char` value is a
 * [
 * Unicode low-surrogate code unit](http://www.unicode.org/glossary/#low_surrogate_code_unit)
 * (also known as *trailing-surrogate code unit*).
 *
 *
 * Such values do not represent characters by themselves,
 * but are used in the representation of
 * [supplementary characters](#supplementary)
 * in the UTF-16 encoding.
 *
 * @param  ch the `char` value to be tested.
 * @return `true` if the `char` value is between
 * [.MIN_LOW_SURROGATE] and
 * [.MAX_LOW_SURROGATE] inclusive;
 * `false` otherwise.
 * @see Character.isHighSurrogate
 */
fun isLowSurrogate(ch: Char): Boolean {
    return ch >= Char.Companion.MIN_LOW_SURROGATE && ch < Char.Companion.MAX_LOW_SURROGATE + 1
}

/**
 * The minimum value of a
 * [
 * Unicode supplementary code point](http://www.unicode.org/glossary/#supplementary_code_point), constant `U+10000`.
 *
 * @since 1.5
 */
const val MIN_SUPPLEMENTARY_CODE_POINT = 0x010000
