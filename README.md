# Compilers-Team3

Compilers-Team3 is a Java project managed with **Maven**.

The project currently includes:

- Lexical analysis
- Syntax analysis with a **Bottom-Up LALR(1)** parser
- Semantic analysis / SDT verification
- A **JavaFX** desktop interface
- Graphical parse tree visualization
- Light / dark theme support

## Main Components

### Lexer
The lexer tokenizes the supported subset of C and generates the token stream used by the parser.

### Parser
The parser is implemented as a **Bottom-Up LALR(1)** parser. It:

- reads the grammar from `doc/grammar/grammar.txt`
- builds the LR(1) collection
- merges states into an LALR(1) collection
- constructs the `ACTION / GOTO` table
- parses the token stream
- builds the parse tree

### Semantic Analysis
After parsing, the project validates semantic rules over the parse tree.

The semantic rules are documented in:

```text
doc/semantic/semantic_rules.txt
```

### JavaFX Interface
The application includes a JavaFX UI with:

- a code editor
- lexer output
- parser output
- semantic output
- parse tree visualization
- theme toggle

## Input and Generated Files

The application works with these files:

```text
doc/test/test.c
doc/tokens/tokens.txt
```

The editor input can be synchronized with `test.c`, and the generated tokens can be written to `tokens.txt`.

## Build and Execution

This project is managed with **Maven**.

### Compile
```bash
mvn compile
```

### Run in development
```bash
mvn javafx:run
```

### Package
```bash
mvn clean package
```

### Run the packaged fat JAR
```bash
java -jar target/Compilers-Team3.jar
```

## Expected Behavior

For a valid input, the application should report:

```text
Parsing Success!
SDT Verified!
```

For invalid input, the application reports syntax or semantic errors in the corresponding output panels.

## Notes

- The grammar is externalized in `doc/grammar/grammar.txt`
- Semantic rules are externalized in `doc/semantic/semantic_rules.txt`
- The project uses Maven for compilation, packaging, and execution
- The current UI is JavaFX-based
