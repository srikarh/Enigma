package enigma;

import java.util.HashMap;

/**
 * Represents a permutation of a range of integers starting at 0 corresponding
 * to the characters of an alphabet.
 *
 * @author Srikar Hanumanula
 */
class Permutation {

    /**
     * Set this Permutation to that specified by CYCLES, a string in the
     * form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     * is interpreted as a permutation in cycle notation.  Characters in the
     * alphabet that are not included in any cycle map to themselves.
     * Whitespace is ignored.
     */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        char last = 0;
        char first = 0;
        for (String i : cycles.split("")) {
            if (i.equals(" ") || i.equals("")) {
                continue;
            }
            if (first == 1) {
                first = i.charAt(0);
                last = first;
                continue;
            }
            if (i.equals("(")) {
                first = 1;
            } else if (i.equals(")")) {
                if (last != first) {
                    _cycles.put(last, first);
                }
                last = 0;
            } else {
                if (last != i.charAt(0)) {
                    _cycles.put(last, i.charAt(0));
                }
                last = i.charAt(0);
            }
        }
        _cycles.forEach((key, value) -> _reversedCycles.put(value, key));
    }


    /**
     * Return the value of P modulo the size of this permutation.
     */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /**
     * Returns the size of the alphabet I permute.
     */
    int size() {
        return _alphabet.size();
    }

    /**
     * Return the result of applying this permutation to P modulo the
     * alphabet size.
     */
    int permute(int p) {
        char l = _alphabet.toChar(wrap(p));
        if (_cycles.containsKey(l)) {
            return _alphabet.toInt(_cycles.get(l));
        }
        return _alphabet.toInt(l);

    }

    /**
     * Return the result of applying the inverse of this permutation
     * to  C modulo the alphabet size.
     */
    int invert(int c) {
        char l = _alphabet.toChar(wrap(c));
        if (_reversedCycles.containsKey(l)) {
            return _alphabet.toInt(_reversedCycles.get(l));
        }
        return _alphabet.toInt(l);
    }

    /**
     * Return the result of applying this permutation to the index of P
     * in ALPHABET, and converting the result to a character of ALPHABET.
     */
    char permute(char p) {
        if (_cycles.containsKey(p)) {
            return _cycles.get(p);
        }
        return p;
    }

    /**
     * Return the result of applying the inverse of this permutation to C.
     */
    char invert(char c) {
        if (_reversedCycles.containsKey(c)) {
            return _reversedCycles.get(c);
        }
        return c;
    }

    /**
     * Return the alphabet used to initialize this Permutation.
     */
    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Return true iff this permutation is a derangement (i.e., a
     * permutation for which no value maps to itself).
     */
    boolean derangement() {
        return _cycles.size() == _alphabet.size();
    }

    /**
     * Alphabet of this permutation.
     */
    private Alphabet _alphabet;
    /**
     * Mapping of cycles.
     */
    private HashMap<Character, Character> _cycles = new HashMap<>();
    /**
     * Reveresed mapping of cycles.
     */
    private HashMap<Character, Character> _reversedCycles = new HashMap<>();

}
