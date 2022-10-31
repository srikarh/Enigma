package enigma;

import static enigma.EnigmaException.*;

/**
 * Superclass that represents a rotor in the enigma machine.
 *
 * @author Srikar Hanumanula
 */
class Rotor {

    /**
     * A rotor named NAME whose permutation is given by PERM.
     */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
        _notches = "";
        _ringstellung = 0;
    }

    /**
     * Return my name.
     */
    String name() {
        return _name;
    }

    /**
     * Return my alphabet.
     */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /**
     * Return my permutation.
     */
    Permutation permutation() {
        return _permutation;
    }

    /**
     * Return the size of my alphabet.
     */
    int size() {
        return _permutation.size();
    }

    /**
     * Return true iff I have a ratchet and can move.
     */
    boolean rotates() {
        return false;
    }

    /**
     * Return true iff I reflect.
     */
    boolean reflecting() {
        return false;
    }

    /**
     * Return my current setting.
     */
    int setting() {
        return _permutation.wrap(_setting - _ringstellung);
    }

    /**
     * Set setting() to POSN.
     */
    void set(int posn) {
        _setting = posn;
    }

    /**
     * Set ring to C.
     */
    void ringSet(char c) {
        if (alphabet().toInt(c) == -1) {
            throw error("Invalid characters used");
        }
        _ringstellung = alphabet().toInt(c);
    }

    /**
     * Set setting() to character CPOSN.
     */
    void set(char cposn) {
        if (alphabet().toInt(cposn) == -1) {
            throw error("Invalid characters used");
        }
        _setting = alphabet().toInt(cposn);
    }

    /**
     * Return the conversion of P (an integer in the range 0..size()-1)
     * according to my permutation.
     */
    int convertForward(int p) {
        int result = _permutation.permute(p + setting());
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        return _permutation.wrap(result - setting());
    }

    /**
     * Return the conversion of E (an integer in the range 0..size()-1)
     * according to the inverse of my permutation.
     */
    int convertBackward(int e) {
        int result = _permutation.invert(e + setting());
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        return _permutation.wrap(result - setting());
    }

    /**
     * Returns the positions of the notches, as a string giving the letters
     * on the ring at which they occur.
     */
    String notches() {
        return _notches;
    }

    /**
     * Returns true iff I am positioned to allow the rotor to my left
     * to advance.
     */
    boolean atNotch() {
        if (_notches.contains(alphabet().toChar(_permutation.wrap(_setting))
                + "")) {
            return true;
        }
        return false;
    }

    /**
     * Advance me one position, if possible. By default, does nothing.
     */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /**
     * My name.
     */
    private final String _name;

    /**
     * The permutation implemented by this rotor in its 0 position.
     */
    private Permutation _permutation;
    /**
     * Current setting of rotor.
     */
    protected int _setting;
    /**
     * Notches of rotor.
     */
    protected String _notches;
    /**
     * Ringstellung of rotor.
     */
    private int _ringstellung;
}
