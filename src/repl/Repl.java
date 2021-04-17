package repl;

import ast.Program;
import lexer.Lexer;
import parser.Parser;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class Repl {

    private static final String PROMPT = ">> ";

    public static void start(InputStream input, PrintStream output) {
        Scanner scanner = new Scanner(input);
        while (true) {
            output.print(PROMPT);
            if(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Lexer lexer = new Lexer(line);
                Parser parser = new Parser(lexer);
                Program program = parser.parseProgram();

                List<String> errors = parser.getErrors();
                if(!errors.isEmpty()) {
                    printParserErrors(output, errors);
                    continue;
                }

                output.print(program.toString());
                output.print("\n");
            }
        }
    }

    private static void printParserErrors(PrintStream output, List<String> messages) {
        output.println("Woops! We ran into some monkey business here!");
        output.println(" parser errors:");
        for(String message : messages) {
            output.println("\t" + message);
        }
    }
}
