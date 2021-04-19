import repl.Repl;

public class Kong {

    public static void main(String[] args) {
        String name = System.getProperty("user.name");
        System.out.printf("Hello %s! This is the Monkey programming language!\n", name);
        System.out.println("Feel free to type in commands\n");
        Repl.start(System.in, System.out);
    }
}
