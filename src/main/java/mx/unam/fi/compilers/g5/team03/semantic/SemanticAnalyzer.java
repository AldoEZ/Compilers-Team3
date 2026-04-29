package mx.unam.fi.compilers.g5.team03.semantic;

import mx.unam.fi.compilers.g5.team03.parser.ParseTreeNode;
import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    /**
     * symbolTable -> symbol table used during semantic analysis
     * errors -> semantic errors found
     * functionType -> return type of current function
     */
    private final SymbolTable symbolTable;
    private final List<String> errors;
    private String functionType;
    
    public SemanticAnalyzer() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
        this.functionType = null;
    }
    
    public boolean analyze(ParseTreeNode root) {
        errors.clear();
        functionType = null;
        
        collectFunctions(root);
        
        if(!symbolTable.hasMainFunction())
            errors.add("Semantic error: main function was not defined.");
        
        analyzeNode(root);
        return errors.isEmpty();
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
    
    public void printErrors() {
        if(errors.isEmpty()) {
            System.out.println("SDT Verified!");
            return;
        }
        
        System.out.println("=== SEMANTIC ERRORS ===");
        for (String error : errors) {
            System.out.println(error);
        }
    }
    
    private void collectFunctions(ParseTreeNode node) {
        if(node == null) return;
        
        if(node.getSymbol().equals("FunctionDef")) {
            String returnType = RaedType(node.getChildren().get(0));
            String functionName = readLexeme(node.getChildren().get(1));
            
            try {
                symbolTable.insertFunction(functionName, returnType, 0);
            } catch (RuntimeException e) {
                errors.add(e.getMessage());
            }
        }
        for(ParseTreeNode child : node.getChildren())
            collectFunctions(child);
    }
    
    private void analyzeNode(ParseTreeNode node) {
        if(node == null) return;
        
        String symbol = node.getSymbol();
        
        switch(symbol) {
            case "Program'":
            case "Program":
            case "StmtList":
            case "Stmt":
                for(ParseTreeNode child : node.getChildren())
                    analyzeNode(child);
                break;
            
            case "FunctionDef":
                analyzeFunctionDef(node);
                break;
            
            case "Block":
                analyzeBlock(node);
                break;
            
            case "DeclStmt":
                analizeDeclStmt(node);
                break;
            
            case "AssignStmt":
                analyzeAssignStmt(node);
                break;
            
            case "IfStmt":
                analyzeIfStmt(node);
                break;
            
            case "PostfixStmt":
                analyzePostfixStmt(node);
                break;
            
            case "CallStmt":
                analyzeCallStmt(node);
                break;
            
            case "ReturnStmt":
                analyzeReturnStmt(node);
                break;
            
            default:
                for(ParseTreeNode child : node.getChildren())
                    analyzeNode(child);
                break;
        }
    }
    
    private void analyzeFunctionDef(ParseTreeNode node) {
        String previousFunctionType = functionType;
        functionType = RaedType(node.getChildren().get(0));
        
        ParseTreeNode blockNode = node.getChildren().get(4);
        analyzeNode(blockNode);
        
        functionType = previousFunctionType;
    }
    
    private void analyzeBlock(ParseTreeNode node) {
        symbolTable.pushScope();
        
        if(node.getChildren().size() == 3)
            analyzeNode(node.getChildren().get(1));
        
        symbolTable.popScope();
    }
    
    private void analizeDeclStmt(ParseTreeNode node) {
        String declaredType = RaedType(node.getChildren().get(0));
        
        if(declaredType.equals("void")) {
            errors.add("Semantic error: variables cannot be declared with type void.");
            return;
        }
        
        ParseTreeNode initDeclListNode = node.getChildren().get(1);
        analyzeInitDeclList(initDeclListNode, declaredType);
    }
    
    private void analyzeInitDeclList(ParseTreeNode node, String declaredType) {
        if(node.getChildren().size() == 1) {
            analyzeInitDecl(node.getChildren().get(0), declaredType);
        } else {
            analyzeInitDeclList(node.getChildren().get(0), declaredType);
            analyzeInitDecl(node.getChildren().get(2), declaredType);
        }
    }
    
    private void analyzeInitDecl(ParseTreeNode node, String declaredType) {
        if(node.getChildren().size() == 1) {
            String name = readLexeme(node.getChildren().get(0));
            
            if(symbolTable.existsFunction(name)) {
                errors.add("Semantic error: variable name conflicts with function name -> " + name);
                return;
            }
            
            try {
                symbolTable.insertVariable(name, declaredType);
            } catch (RuntimeException e) {
                errors.add(e.getMessage());
            }
        } else {
            String name = readLexeme(node.getChildren().get(0));
            ParseTreeNode exprNode = node.getChildren().get(2);
            
            if(symbolTable.existsFunction(name)) {
                errors.add("Semantic error: variable name conflicts with function name -> " + name);
                return;
            }
            
            try {
                symbolTable.insertVariable(name, declaredType);
            } catch (RuntimeException e) {
                errors.add(e.getMessage());
            }
            
            String exprType = inferType(exprNode);
            
            if(!compatible(declaredType, exprType)) {
                errors.add(
                    "Semantic error: incompatible initialization for variable '"
                    + name + "'. Expected " + declaredType + " but found " + exprType + "."
                );
            }
        }
    }
    
    private void analyzeAssignStmt(ParseTreeNode node) {
        String leftType = inferType(node.getChildren().get(0));
        String rightType = inferType(node.getChildren().get(2));
        
        if(!compatible(leftType, rightType)) {
            errors.add(
                "Semantic error: incompatible assignment. Left side is "
                + leftType + " but expression is " + rightType + "."
            );
        }
    }
    
    private void analyzeIfStmt(ParseTreeNode node) {
        String conditionType = infierConditionType(node.getChildren().get(2));
        
        if(!conditionType.equals("bool"))
            errors.add("Semantic error: invalid if condition.");
        
        analyzeNode(node.getChildren().get(4));
        
        if(node.getChildren().size() == 7)
            analyzeNode(node.getChildren().get(6));
    }
    
    private void analyzePostfixStmt(ParseTreeNode node) {
        String name = readLexeme(node.getChildren().get(0));
        VariableSymbol variable = symbolTable.lookupVariable(name);
        
        if(variable == null) {
            errors.add("Semantic error: variable not declared -> " + name);
            return;
        }
        
        if(!numeric(variable.getType())) {
            errors.add(
                "Semantic error: postfix operator applied to non-numeric variable -> " + name
            );
        }
    }
    
    private void analyzeCallStmt(ParseTreeNode node) {
        String functionName = readLexeme(node.getChildren().get(0));
        
        if(functionName.equals("printf")) {
            if(node.getChildren().size() == 4) {
                errors.add("Semantic error: printf requires at least one argument.");
                return;
            }
            
            List<String> argTypes = colectArgTypes(node.getChildren().get(2));
            
            if(argTypes.isEmpty()) {
                errors.add("Semantic error: printf requires at least one argument.");
            } else if(!argTypes.get(0).equals("string")) {
                errors.add("Semantic error: the first argument of printf must be a string.");
            }
            return;
        }
        
        FunctionSymbol function = symbolTable.lookupFunction(functionName);
        
        if(function == null) {
            errors.add("Semantic error: function not defined -> " + functionName);
            return;
        }
        
        if(node.getChildren().size() == 5) {
            errors.add(
                "Semantic error: function '" + functionName
                + "' does not accept arguments in this subset."
            );
        }
    }
    
    private void analyzeReturnStmt(ParseTreeNode node) {
        if(functionType == null) {
            errors.add("Semantic error: return statement outside a function.");
            return;
        }
        
        if(functionType.equals("void")) {
            errors.add("Semantic error: a void function cannot return an expression.");
            return;
        }
        
        String exprType = inferType(node.getChildren().get(1));
        
        if(!compatible(functionType, exprType)) {
            errors.add(
                "Semantic error: incompatible return type. Expected "
                + functionType + " but found " + exprType + "."
            );
        }
    }
    
    private List<String> colectArgTypes(ParseTreeNode node) {
        List<String> types = new ArrayList<>();
        
        if(node.getChildren().size() == 1) {
            types.add(inferType(node.getChildren().get(0)));
        } else {
            types.addAll(colectArgTypes(node.getChildren().get(0)));
            types.add(inferType(node.getChildren().get(2)));
        }
        return types;
    }
    
    private String infierConditionType(ParseTreeNode node) {
        if(node.getChildren().size() == 1) {
            String exprType = inferType(node.getChildren().get(0));
            
            if (!numeric(exprType))
                errors.add("Semantic error: invalid condition expression.");
            
            return "bool";
        }
        String leftType = inferType(node.getChildren().get(0));
        String rightType = inferType(node.getChildren().get(2));
        
        if (!numeric(leftType) || !numeric(rightType))
            errors.add("Semantic error: invalid relational comparison.");
        
        return "bool";
    }
    
    private String inferType(ParseTreeNode node) {
        String symbol = node.getSymbol();
        
        if(symbol.equals("LValue")) return inferType(node.getChildren().get(0));
        
        if(symbol.equals("Expr")) {
            if(node.getChildren().size() == 1)
                return inferType(node.getChildren().get(0));
            
            String leftType = inferType(node.getChildren().get(0));
            String rightType = inferType(node.getChildren().get(2));
            
            if (!numeric(leftType) || !numeric(rightType))
                errors.add("Semantic error: invalid additive expression.");
            
            return "int";
        }
        
        if(symbol.equals("Term")) {
            if(node.getChildren().size() == 1)
                return inferType(node.getChildren().get(0));
            
            String leftType = inferType(node.getChildren().get(0));
            String rightType = inferType(node.getChildren().get(2));
            
            if(!numeric(leftType) || !numeric(rightType))
                errors.add("Semantic error: invalid multiplicative expression.");
            
            return "int";
        }
        
        if(symbol.equals("Unary")) {
            if(node.getChildren().size() == 1)
                return inferType(node.getChildren().get(0));
            
            String op = readOpertor(node.getChildren().get(0));
            String operandType = inferType(node.getChildren().get(1));
            
            if(op.equals("+") || op.equals("-")) {
                if(!numeric(operandType))
                    errors.add("Semantic error: invalid unary operator " + op + ".");
                
                return operandType;
            }
            
            if(op.equals("!")) {
                if(!numeric(operandType))
                    errors.add("Semantic error: invalid unary operator !.");
                
                return "bool";
            }
            errors.add("Semantic error: operator " + op + " is not supported in this subset.");
            return "error";
        }
        
        if(symbol.equals("PostfixExpr")) {
            if(node.getChildren().size() == 1)
                return inferType(node.getChildren().get(0));
            
            String name = readLexeme(node.getChildren().get(0));
            VariableSymbol variable = symbolTable.lookupVariable(name);
            
            if(variable == null) {
                errors.add("Semantic error: variable not declared -> " + name);
                return "error";
            }
            
            if(!numeric(variable.getType())) {
                errors.add(
                    "Semantic error: postfix operator applied to non-numeric variable -> " + name
                );
            }
            return variable.getType();
        }
        
        if(symbol.equals("Primary")) {
            if(node.getChildren().size() == 3)
                return inferType(node.getChildren().get(1));
            
            ParseTreeNode child = node.getChildren().get(0);
            
            if(child.getSymbol().equals("id")) {
                String name = readLexeme(child);
                VariableSymbol variable = symbolTable.lookupVariable(name);
                
                if(variable == null) {
                    errors.add("Semantic error: variable not declared -> " + name);
                    return "error";
                }
                return variable.getType();
            }
            
            if(child.getSymbol().equals("constant"))
                return "int";
            
            if(child.getSymbol().equals("literal")) {
                String lexeme = child.getLexeme();
                
                if(lexeme != null && lexeme.length() >= 2) {
                    if(lexeme.startsWith("'") && lexeme.endsWith("'"))
                        return "char";
                    
                    if (lexeme.startsWith("\"") && lexeme.endsWith("\""))
                        return "string";
                }
                return "literal";
            }
        }
        
        if(symbol.equals("id")) {
            String name = readLexeme(node);
            VariableSymbol variable = symbolTable.lookupVariable(name);
            
            if(variable == null) {
                errors.add("Semantic error: variable not declared -> " + name);
                return "error";
            }
            return variable.getType();
        }
        
        if(symbol.equals("constant"))
            return "int";
        
        if(symbol.equals("literal")) {
            String lexeme = node.getLexeme();
            
            if(lexeme != null && lexeme.length() >= 2) {
                if(lexeme.startsWith("'") && lexeme.endsWith("'"))
                    return "char";
                
                if(lexeme.startsWith("\"") && lexeme.endsWith("\""))
                    return "string";
            }
            return "literal";
        }
        
        if(node.getChildren().size() == 1)
            return inferType(node.getChildren().get(0));
        
        return "error";
    }
    
    private String RaedType(ParseTreeNode typeNode) {
        ParseTreeNode terminal = typeNode.getChildren().get(0);
        
        if(terminal.getLexeme() != null)
            return terminal.getLexeme();
        
        return terminal.getSymbol();
    }
    
    private String readLexeme(ParseTreeNode node) {
        if(node.getLexeme() != null)
            return node.getLexeme();
        
        if(!node.getChildren().isEmpty())
            return readLexeme(node.getChildren().get(0));
        
        return node.getSymbol();
    }
    
    private String readOpertor(ParseTreeNode opNode) {
        ParseTreeNode terminal = opNode.getChildren().get(0);
        
        if(terminal.getLexeme() != null)
            return terminal.getLexeme();
        
        return terminal.getSymbol();
    }
    
    private boolean numeric(String type) {
        return type.equals("int") || type.equals("char");
    }
    
    private boolean compatible(String expected, String found) {
        if(expected.equals("int") && found.equals("int")) return true;
        if(expected.equals("int") && found.equals("char")) return true;
        if(expected.equals("char") && found.equals("int")) return true;
        if(expected.equals("char") && found.equals("char")) return true;
        return false;
    }
}
