import repl.Repl;

public class Kong {

    public static void main(String[] args) {
        System.out.println("Hello %s! This is the Monkey programming language!\n");
        System.out.println("Feel free to type in commands\n");
        Repl.start(System.in, System.out);
    }
}
