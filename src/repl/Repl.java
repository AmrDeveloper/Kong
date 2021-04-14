package repl;

import lexer.Lexer;
import token.Token;
import token.TokenType;

import java.io.InputStream;
import java.io.PrintStream;
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

                Token token = lexer.nextToken();
                while (token.getTokenType() != TokenType.EOF) {
                    output.println(token);
                    token = lexer.nextToken();
                }
            }
        }
    }
}
