package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/**
 * Enigma simulator.
 *
 * @author Srikar Hanumanula
 */
public final class Main {

    /**
     * Process a sequence of encryptions and decryptions, as
     * specified by ARGS, where 1 <= ARGS.length <= 3.
     * ARGS[0] is the name of a configuration file.
     * ARGS[1] is optional; when present, it names an input file
     * containing messages.  Otherwise, input comes from the standard
     * input.  ARGS[2] is optional; when present, it names an output
     * file for processed messages.  Otherwise, output goes to the
     * standard output. Exits normally if there are no errors in the input;
     * otherwise with code 1.
     */
    public static void main(String... args) {
        try {
            CommandArgs options =
                    new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                        + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();

            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /**
     * Open the necessary files for non-option arguments ARGS (see comment
     * on main).
     */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /**
     * Return a Scanner reading from the file named NAME.
     */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Return a PrintStream writing to the file named NAME.
     */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Configure an Enigma machine from the contents of configuration
     * file _config and apply it to the messages in _input, sending the
     * results to _output.
     */
    private void process() {
        String nextLine;
        Machine m = readConfig();
        while (_input.hasNextLine()) {
            nextLine = _input.nextLine();
            while (nextLine.equals("")) {
                _output.print("\n");
                nextLine = _input.nextLine();
            }
            if (nextLine.charAt(0) != '*') {
                throw error("No settings line");
            }
            setUp(m, nextLine);
            while (_input.hasNextLine() && !_input.hasNext("\\*")) {
                nextLine = _input.nextLine();
                if (nextLine.equals("\n")) {
                    break;
                }
                printMessageLine(m.convert(nextLine));
            }
        }
    }

    /**
     * Return an Enigma machine configured from the contents of configuration
     * file _config.
     */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.nextLine().strip());
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            ArrayList<Rotor> allRotors = new ArrayList<>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /**
     * Return a rotor, reading its description from _config.
     */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String type = _config.next();
            String cycles = _config.nextLine();
            while (_config.hasNext("\\(.+")) {
                cycles += _config.nextLine();
            }
            if (!cycles.matches(" *(\\(.+\\) *)*")) {
                throw error("bad cycle format");
            }
            Rotor rotor;
            Permutation permutation = new Permutation(cycles, _alphabet);
            if (type.charAt(0) == 'M') {
                String notches = type.substring(1);
                rotor = new MovingRotor(name, permutation, notches);
            } else if (type.charAt(0) == 'R') {
                rotor = new Reflector(name, permutation);
            } else if (type.charAt(0) == 'N') {
                rotor = new FixedRotor(name, permutation);
            } else {
                throw error("invalid motor type");
            }
            return rotor;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /**
     * Set M according to the specification given on SETTINGS,
     * which must have the format specified in the assignment.
     */
    private void setUp(Machine M, String settings) {
        String cycles = "";
        if (settings.indexOf('(') != -1) {
            cycles = settings.substring(settings.indexOf('('));
            settings = settings.substring(0, settings.indexOf('('));
        }
        String rotors = settings.substring(settings.indexOf("*") + 1);
        M.setPlugboard(new Permutation(cycles, _alphabet));
        String[] rotorInfo = rotors.strip().split("\\s+|\\t+");
        String ringstellung = "";
        String initial = "";
        int index = 1;
        if (rotorInfo.length - 2 == M.numRotors()) {
            ringstellung = rotorInfo[rotorInfo.length - 1];
            index = 2;
        } else if (rotorInfo.length - 1 != M.numRotors()) {
            throw error("Wrong number of rotors");
        }
        initial = rotorInfo[rotorInfo.length - index];
        if (initial.length() != rotorInfo.length - index - 1) {
            throw error("Inconsistent number of rotors");
        }
        M.insertRotors(Arrays.copyOfRange(rotorInfo, 0,
                rotorInfo.length - index));
        for (int i = 0; i < initial.length(); i++) {
            M.getRotor(i + 1).set(initial.charAt(i));
        }
        for (int i = 0; i < ringstellung.length(); i++) {
            M.getRotor(i + 1).ringSet(ringstellung.charAt(i));
        }
    }

    /**
     * Return true iff verbose option specified.
     */
    static boolean verbose() {
        return _verbose;
    }

    /**
     * Print MSG in groups of five (except that the last group may
     * have fewer letters).
     */
    private void printMessageLine(String msg) {
        msg = msg.replaceAll(" ", "");
        for (int i = 5; i < msg.length(); i += 6) {
            msg = msg.substring(0, i) + " " + msg.substring(i);
        }
        _output.print(msg + '\n');
    }

    /**
     * Alphabet used in this machine.
     */
    private Alphabet _alphabet;

    /**
     * Source of input messages.
     */
    private Scanner _input;

    /**
     * Source of machine configuration.
     */
    private Scanner _config;

    /**
     * File for encoded/decoded messages.
     */
    private PrintStream _output;

    /**
     * True if --verbose specified.
     */
    private static boolean _verbose;
}
