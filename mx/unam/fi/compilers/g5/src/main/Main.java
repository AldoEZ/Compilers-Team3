package mx.unam.fi.compilers.g5.team03;

import mx.unam.fi.compilers.g5.team03.*;
import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        String lineInput = "";
        int cntTokens = 1;
        System.out.println("Input:");
        
        try {
            BufferedReader br = new BufferedReader(new FileReader("../../doc/test/test.c"));
            
            lineInput = br.readLine();
            while(lineInput != null) {
                System.out.println(lineInput);
                lineInput = br.readLine();
            }
            System.out.println("\nTokens Clasification:");
            
            System.out.println("#\tTYPE\t\tLEXEME");
            List<Token> fileTokens = lexer.tokenizeFile("../../doc/test/test.c");
            int countTokens = fileTokens.size();
            for (Token token : fileTokens) {
                System.out.println(cntTokens + "\t" + token);
                cntTokens++;
            }
            
            System.out.println("----------------------------------------------------");
            System.out.println("Total tokens: " + countTokens);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
