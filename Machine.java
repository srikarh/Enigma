package enigma;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import static enigma.EnigmaException.*;

/**
 * Class that represents a complete enigma machine.
 *
 * @author Srikar Hanumanula
 */
class Machine {

    /**
     * A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     * and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     * available rotors.
     */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _numPawls = pawls;
        _allRotors = allRotors.stream().toList();
    }

    /**
     * Return the number of rotor slots I have.
     */
    int numRotors() {
        return _numRotors;
    }

    /**
     * Return the number pawls (and thus rotating rotors) I have.
     */
    int numPawls() {
        return _numPawls;
    }

    /**
     * Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     * #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     * undefined results.
     */
    Rotor getRotor(int k) {
        return _myRotors.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Set my rotor slots to the rotors named ROTORS from my set of
     * available rotors (ROTORS[0] names the reflector).
     * Initially, all rotors are set at their 0 setting.
     */
    void insertRotors(String[] rotors) {
        _myRotors = new ArrayList<>();
        int rotating = 0;
        for (String rotor : rotors) {
            for (Rotor i : _allRotors) {
                if (i.name().equals(rotor)) {
                    _myRotors.add(i);
                    if (i.rotates()) {
                        rotating += 1;
                    }
                    break;
                }
            }
        }
        if (rotating != _numPawls) {
            throw error("Wrong number of moving rotors");
        }
        if (!_myRotors.get(0).reflecting()) {
            throw error("First rotor isn't reflecting");
        }
        if ((new HashSet<Rotor>(_myRotors)).size() != _myRotors.size()) {
            throw error("Duplicate rotor names");
        }
    }

    /**
     * Set my rotors according to SETTING, which must be a string of
     * numRotors()-1 characters in my alphabet. The first letter refers
     * to the leftmost rotor setting (not counting the reflector).
     */
    void setRotors(String setting) {
        for (int i = 0; i < numRotors() - 1; i++) {
            _myRotors.get(i + 1).set(setting.charAt(i));
        }
    }

    /**
     * Return the current plugboard's permutation.
     */
    Permutation plugboard() {
        return _plugboard;
    }

    /**
     * Set the plugboard to PLUGBOARD.
     */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /**
     * Returns the result of converting the input character C (as an
     * index in the range 0..alphabet size - 1), after first advancing
     * the machine.
     */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /**
     * Advance all rotors to their next position.
     */
    private void advanceRotors() {
        HashSet<Rotor> advance = new HashSet<>();
        advance.add(_myRotors.get(_myRotors.size() - 1));
        for (int i = _myRotors.size() - 1; i >= 0; i--) {
            if (_myRotors.get(i).atNotch() && _myRotors.get(i - 1).rotates()) {
                advance.add(_myRotors.get(i - 1));
                advance.add(_myRotors.get(i));
            }
        }
        for (Rotor i : advance) {
            i.advance();
        }
    }

    /**
     * Return the result of applying the rotors to the character C (as an
     * index in the range 0..alphabet size - 1).
     */
    private int applyRotors(int c) {
        int result = c;
        for (int i = _myRotors.size() - 1; i >= 0; i--) {
            result = _myRotors.get(i).convertForward(result);
        }
        for (int i = 1; i < _myRotors.size(); i++) {
            result = _myRotors.get(i).convertBackward(result);
        }
        return result;
    }

    /**
     * Returns the encoding/decoding of MSG, updating the state of
     * the rotors accordingly.
     */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == ' ' || c == '\n') {
                result += c;
            } else {
                result += _alphabet.toChar(convert(_alphabet.toInt(c)));
            }
        }
        return result;
    }

    /**
     * Common alphabet of my rotors.
     */
    private final Alphabet _alphabet;
    /**
     * number of total rotors.
     */
    private int _numRotors;
    /**
     * number of total pawls.
     */
    private int _numPawls;
    /**
     * List of all rotors.
     */
    private final List<Rotor> _allRotors;
    /**
     * List of rotors used in machine.
     */
    private ArrayList<Rotor> _myRotors;
    /**
     * Plugboard of machine.
     */
    private Permutation _plugboard;
}
