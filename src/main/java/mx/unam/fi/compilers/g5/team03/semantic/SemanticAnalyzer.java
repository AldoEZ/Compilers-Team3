package mx.unam.fi.compilers.g5.team03.semantic;

import mx.unam.fi.compilers.g5.team03.parser.ParseTreeNode;
import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    /**
     * symbolTable -> semantic symbol table
     * errors -> semantic errors found during analysis
     * currentFunctionType -> return type of the function currently being analyzed
     */
    private final SymbolTable symbolTable;
    private final List<String> errors;
    private String currentFunctionType;

    public SemanticAnalyzer() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
        this.currentFunctionType = null;
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public boolean analyze(ParseTreeNode root) {
        errors.clear();
        currentFunctionType = null;

        collectFunctions(root);

        if (!symbolTable.hasMainFunction()) {
            errors.add("Semantic error: main function was not defined.");
        }

        analyzeNode(root);

        return errors.isEmpty();
    }

    private void collectFunctions(ParseTreeNode node) {
        if (node == null) {
            return;
        }

        if (node.getSymbol().equals("FunctionDef")) {
            String returnType = readTypeNode(node.getChildren().get(0));
            String functionName = readIdNode(node.getChildren().get(1));

            try {
                symbolTable.insertFunction(functionName, returnType, 0);
            } catch (IllegalStateException e) {
                errors.add(e.getMessage());
            }
        }

        for (ParseTreeNode child : node.getChildren()) {
            collectFunctions(child);
        }
    }

    private void analyzeNode(ParseTreeNode node) {
        if (node == null) {
            return;
        }

        switch (node.getSymbol()) {
            case "Program'" -> analyzeNode(node.getChildren().get(0));

            case "Program" -> {
                for (ParseTreeNode child : node.getChildren()) {
                    analyzeNode(child);
                }
            }

            case "FunctionDef" -> analyzeFunctionDef(node);

            case "Block" -> analyzeBlock(node);

            case "StmtList" -> {
                for (ParseTreeNode child : node.getChildren()) {
                    analyzeNode(child);
                }
            }

            case "Stmt" -> analyzeNode(node.getChildren().get(0));

            case "DeclStmt" -> analyzeDeclStmt(node);

            case "AssignStmt" -> analyzeAssignStmt(node);

            case "IfStmt" -> analyzeIfStmt(node);

            case "PostfixStmt" -> analyzePostfixStmt(node);

            case "CallStmt" -> analyzeCallStmt(node);

            case "ReturnStmt" -> analyzeReturnStmt(node);

            default -> {
                for (ParseTreeNode child : node.getChildren()) {
                    analyzeNode(child);
                }
            }
        }
    }

    private void analyzeFunctionDef(ParseTreeNode node) {
        String previousFunctionType = currentFunctionType;
        currentFunctionType = readTypeNode(node.getChildren().get(0));

        ParseTreeNode blockNode = node.getChildren().get(4);
        analyzeNode(blockNode);

        currentFunctionType = previousFunctionType;
    }

    private void analyzeBlock(ParseTreeNode node) {
        symbolTable.pushScope();

        if (node.getChildren().size() == 3) {
            analyzeNode(node.getChildren().get(1));
        }

        symbolTable.popScope();
    }

    private void analyzeDeclStmt(ParseTreeNode node) {
        String declaredType = readTypeNode(node.getChildren().get(0));

        if (declaredType.equals("void")) {
            errors.add("Semantic error: variables cannot be declared with type void.");
            return;
        }

        ParseTreeNode initDeclListNode = node.getChildren().get(1);
        analyzeInitDeclList(initDeclListNode, declaredType);
    }

    private void analyzeInitDeclList(ParseTreeNode node, String declaredType) {
        if (node.getChildren().size() == 1) {
            analyzeInitDecl(node.getChildren().get(0), declaredType);
        } else {
            analyzeInitDeclList(node.getChildren().get(0), declaredType);
            analyzeInitDecl(node.getChildren().get(2), declaredType);
        }
    }

    private void analyzeInitDecl(ParseTreeNode node, String declaredType) {
        if (node.getChildren().size() == 1) {
            String name = readIdNode(node.getChildren().get(0));

            try {
                symbolTable.insertVariable(name, declaredType);
            } catch (IllegalStateException e) {
                errors.add(e.getMessage());
            }
        } else {
            String name = readIdNode(node.getChildren().get(0));
            ParseTreeNode exprNode = node.getChildren().get(2);

            try {
                symbolTable.insertVariable(name, declaredType);
            } catch (IllegalStateException e) {
                errors.add(e.getMessage());
            }

            String exprType = inferType(exprNode);
            if (!compatible(declaredType, exprType)) {
                errors.add(
                    "Semantic error: incompatible initialization for variable '"
                    + name + "'. Expected " + declaredType + " but found " + exprType + "."
                );
            }
        }
    }

    private void analyzeAssignStmt(ParseTreeNode node) {
        ParseTreeNode lvalueNode = node.getChildren().get(0);
        ParseTreeNode exprNode = node.getChildren().get(2);

        String leftType = inferType(lvalueNode);
        String exprType = inferType(exprNode);

        if (!compatible(leftType, exprType)) {
            errors.add(
                "Semantic error: incompatible assignment. Left side is "
                + leftType + " but expression is " + exprType + "."
            );
        }
    }

    private void analyzeIfStmt(ParseTreeNode node) {
        ParseTreeNode conditionNode = node.getChildren().get(2);
        String conditionType = inferConditionType(conditionNode);

        if (!conditionType.equals("bool")) {
            errors.add("Semantic error: invalid if condition.");
        }

        analyzeNode(node.getChildren().get(4));

        if (node.getChildren().size() == 7) {
            analyzeNode(node.getChildren().get(6));
        }
    }

    private void analyzePostfixStmt(ParseTreeNode node) {
        String name = readIdNode(node.getChildren().get(0));
        VariableSymbol variable = symbolTable.lookupVariable(name);

        if (variable == null) {
            errors.add("Semantic error: variable not declared -> " + name);
            return;
        }

        if (!numeric(variable.getType())) {
            errors.add(
                "Semantic error: postfix operator applied to non-numeric variable -> " + name
            );
        }
    }

    private void analyzeCallStmt(ParseTreeNode node) {
        String functionName = readIdNode(node.getChildren().get(0));

        if (functionName.equals("printf")) {
            if (node.getChildren().size() == 5) {
                ParseTreeNode argListNode = node.getChildren().get(2);
                List<String> argTypes = collectArgTypes(argListNode);

                if (argTypes.isEmpty()) {
                    errors.add("Semantic error: printf requires at least one argument.");
                } else if (!argTypes.get(0).equals("string")) {
                    errors.add(
                        "Semantic error: first printf argument must be a string literal."
                    );
                }
            }
            return;
        }

        FunctionSymbol function = symbolTable.lookupFunction(functionName);

        if (function == null) {
            errors.add("Semantic error: function not defined -> " + functionName);
            return;
        }

        if (node.getChildren().size() == 5) {
            errors.add(
                "Semantic error: function '" + functionName
                + "' does not accept arguments in this subset."
            );
        }
    }

    private void analyzeReturnStmt(ParseTreeNode node) {
        ParseTreeNode exprNode = node.getChildren().get(1);
        String exprType = inferType(exprNode);

        if (currentFunctionType == null) {
            errors.add("Semantic error: return statement outside a function.");
            return;
        }

        if (currentFunctionType.equals("void")) {
            errors.add("Semantic error: void function cannot return an expression.");
            return;
        }

        if (!compatible(currentFunctionType, exprType)) {
            errors.add(
                "Semantic error: incompatible return type. Expected "
                + currentFunctionType + " but found " + exprType + "."
            );
        }
    }

    private List<String> collectArgTypes(ParseTreeNode node) {
        List<String> types = new ArrayList<>();

        if (node.getChildren().size() == 1) {
            types.add(inferType(node.getChildren().get(0)));
        } else {
            types.addAll(collectArgTypes(node.getChildren().get(0)));
            types.add(inferType(node.getChildren().get(2)));
        }

        return types;
    }

    private String inferConditionType(ParseTreeNode node) {
        if (node.getChildren().size() == 1) {
            String exprType = inferType(node.getChildren().get(0));

            if (!numeric(exprType)) {
                errors.add("Semantic error: invalid condition expression.");
            }

            return "bool";
        }

        String leftType = inferType(node.getChildren().get(0));
        String rightType = inferType(node.getChildren().get(2));

        if (!numeric(leftType) || !numeric(rightType)) {
            errors.add("Semantic error: invalid relational comparison.");
        }

        return "bool";
    }

    private String inferType(ParseTreeNode node) {
        return switch (node.getSymbol()) {
            case "LValue" -> inferType(node.getChildren().get(0));

            case "Expr" -> {
                if (node.getChildren().size() == 1) {
                    yield inferType(node.getChildren().get(0));
                }

                String leftType = inferType(node.getChildren().get(0));
                String rightType = inferType(node.getChildren().get(2));

                if (!numeric(leftType) || !numeric(rightType)) {
                    errors.add("Semantic error: invalid additive expression.");
                }

                yield "int";
            }

            case "Term" -> {
                if (node.getChildren().size() == 1) {
                    yield inferType(node.getChildren().get(0));
                }

                String leftType = inferType(node.getChildren().get(0));
                String rightType = inferType(node.getChildren().get(2));

                if (!numeric(leftType) || !numeric(rightType)) {
                    errors.add("Semantic error: invalid multiplicative expression.");
                }

                yield "int";
            }

            case "Unary" -> {
                if (node.getChildren().size() == 1) {
                    yield inferType(node.getChildren().get(0));
                }

                String op = readOperator(node.getChildren().get(0));
                String operandType = inferType(node.getChildren().get(1));

                if (op.equals("+") || op.equals("-")) {
                    if (!numeric(operandType)) {
                        errors.add("Semantic error: invalid unary operator " + op + ".");
                    }
                    yield operandType;
                }

                if (op.equals("!")) {
                    if (!numeric(operandType)) {
                        errors.add("Semantic error: invalid unary operator !.");
                    }
                    yield "bool";
                }

                errors.add("Semantic error: operator " + op + " is not supported in this subset.");
                yield "error";
            }

            case "PostfixExpr" -> {
                if (node.getChildren().size() == 1) {
                    yield inferType(node.getChildren().get(0));
                }

                String name = readIdNode(node.getChildren().get(0));
                VariableSymbol variable = symbolTable.lookupVariable(name);

                if (variable == null) {
                    errors.add("Semantic error: variable not declared -> " + name);
                    yield "error";
                }

                if (!numeric(variable.getType())) {
                    errors.add(
                        "Semantic error: postfix operator applied to non-numeric variable -> " + name
                    );
                }

                yield variable.getType();
            }

            case "Primary" -> {
                if (node.getChildren().size() == 3) {
                    yield inferType(node.getChildren().get(1));
                }

                ParseTreeNode child = node.getChildren().get(0);

                if (child.getSymbol().equals("id")) {
                    String name = readIdNode(child);
                    VariableSymbol variable = symbolTable.lookupVariable(name);

                    if (variable == null) {
                        errors.add("Semantic error: variable not declared -> " + name);
                        yield "error";
                    }

                    yield variable.getType();
                }

                if (child.getSymbol().equals("constant")) {
                    yield "int";
                }

                if (child.getSymbol().equals("literal")) {
                    String lexeme = child.getLexeme();

                    if (lexeme != null && lexeme.length() >= 2) {
                        if (lexeme.startsWith("'") && lexeme.endsWith("'")) {
                            yield "char";
                        }
                        if (lexeme.startsWith("\"") && lexeme.endsWith("\"")) {
                            yield "string";
                        }
                    }

                    yield "literal";
                }

                yield "error";
            }

            case "id" -> {
                String name = readIdNode(node);
                VariableSymbol variable = symbolTable.lookupVariable(name);

                if (variable == null) {
                    errors.add("Semantic error: variable not declared -> " + name);
                    yield "error";
                }

                yield variable.getType();
            }

            default -> {
                if (node.getChildren().isEmpty()) {
                    yield "error";
                }

                if (node.getChildren().size() == 1) {
                    yield inferType(node.getChildren().get(0));
                }

                yield "error";
            }
        };
    }

    private String readTypeNode(ParseTreeNode typeNode) {
        ParseTreeNode terminal = typeNode.getChildren().get(0);

        if (terminal.getLexeme() != null) {
            return terminal.getLexeme();
        }

        return terminal.getSymbol();
    }

    private String readIdNode(ParseTreeNode idNode) {
        if (idNode.getLexeme() != null) {
            return idNode.getLexeme();
        }
        return idNode.getSymbol();
    }

    private String readOperator(ParseTreeNode opNode) {
        ParseTreeNode terminal = opNode.getChildren().get(0);

        if (terminal.getLexeme() != null) {
            return terminal.getLexeme();
        }

        return terminal.getSymbol();
    }

    private boolean numeric(String type) {
        return type.equals("int") || type.equals("char");
    }

    private boolean compatible(String expected, String found) {
        if (expected.equals("int") && found.equals("int")) return true;
        if (expected.equals("int") && found.equals("char")) return true;
        if (expected.equals("char") && found.equals("int")) return true;
        if (expected.equals("char") && found.equals("char")) return true;
        return false;
    }

    public void printErrors() {
        if (errors.isEmpty()) {
            System.out.println("SDT Verified!");
            return;
        }

        System.out.println("=== SEMANTIC ERRORS ===");
        for (String error : errors) {
            System.out.println(error);
        }
    }
}