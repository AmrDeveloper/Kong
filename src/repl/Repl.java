package repl;

import ast.Program;
import evaluator.Evaluator;
import lexer.Lexer;
import object.Environment;
import object.KongObject;
import parser.Parser;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class Repl {

    private static final String PROMPT = ">> ";

    public static void start(InputStream input, PrintStream output) {
        Scanner scanner = new Scanner(input);
        Environment environment = new Environment();

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

                Evaluator evaluator = new Evaluator(environment);
                KongObject evaluated = evaluator.visit(program);
                if(evaluated != null) {
                    output.print(evaluated.inspect());
                    output.print("\n");
                }
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
