package com.jackcompiler.parser;

import com.jackcompiler.codegen.SymbolTable;
import com.jackcompiler.codegen.SymbolTable.Kind;
import com.jackcompiler.codegen.SymbolTable.Symbol;
import com.jackcompiler.codegen.VMWriter;
import com.jackcompiler.codegen.VMWriter.Command;
import com.jackcompiler.codegen.VMWriter.Segment;
import com.jackcompiler.lexer.Scanner;
import com.jackcompiler.lexer.Token;
import com.jackcompiler.lexer.TokenType;
import com.jackcompiler.xml.XmlParseWriter;

public class CompilationEngine {

    private final Scanner scanner;
    private final XmlParseWriter xml;
    private final SymbolTable symbolTable;
    private final VMWriter vmWriter;

    private Token atual;
    private Token proximo;
    private String className;

    private int ifLabelNum = 0;
    private int whileLabelNum = 0;
    private boolean gerandoVm = false;

    public CompilationEngine(Scanner scanner) {
        this.scanner = scanner;
        this.xml = new XmlParseWriter();
        this.symbolTable = new SymbolTable();
        this.vmWriter = new VMWriter();
        this.atual = scanner.nextToken();
        this.proximo = scanner.nextToken();
    }

    private void advance() {
        atual = proximo;
        proximo = scanner.nextToken();
    }

    private Token peek() {
        return atual;
    }

    private boolean match(TokenType tipo) {
        return peek().getType() == tipo;
    }

    private Token consume(TokenType tipo) {
        if (!match(tipo)) {
            throw erro("Esperado " + tipo);
        }
        Token consumido = atual;
        xml.escreverToken(atual);
        advance();
        return consumido;
    }

    private IllegalArgumentException erro(String mensagem) {
        return new IllegalArgumentException(
            "Erro sintatico na linha " + atual.getLine()
            + ", coluna " + atual.getColumn()
            + ": " + mensagem
            + ". Encontrado: " + atual.getType()
            + " ('" + atual.getLexeme() + "')"
        );
    }

    public String compile() {
        compileClass();
        if (!match(TokenType.EOF)) {
            throw erro("Esperado fim do arquivo");
        }
        return xml.getXml();
    }

    // class: 'class' className '{' classVarDec* subroutineDec* '}'
    private void compileClass() {
        xml.abrir("class");
        consume(TokenType.CLASS);
        className = consume(TokenType.IDENT).getLexeme();
        consume(TokenType.LBRACE);

        while (match(TokenType.STATIC) || match(TokenType.FIELD)) {
            compileClassVarDec();
        }
        while (match(TokenType.CONSTRUCTOR)
                || match(TokenType.FUNCTION)
                || match(TokenType.METHOD)) {
            compileSubroutineDec();
        }

        consume(TokenType.RBRACE);
        xml.fechar("class");
    }

    // classVarDec: ('static' | 'field') type varName (',' varName)* ';'
    private void compileClassVarDec() {
        xml.abrir("classVarDec");
        Kind kind;
        if (match(TokenType.STATIC)) {
            consume(TokenType.STATIC);
            kind = Kind.STATIC;
        } else {
            consume(TokenType.FIELD);
            kind = Kind.FIELD;
        }
        String type = consumeType();
        String name = consume(TokenType.IDENT).getLexeme();
        symbolTable.define(name, type, kind);

        while (match(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            name = consume(TokenType.IDENT).getLexeme();
            symbolTable.define(name, type, kind);
        }
        consume(TokenType.SEMICOLON);
        xml.fechar("classVarDec");
    }

    private String consumeType() {
        TokenType tipo = peek().getType();
        if (tipo == TokenType.INT || tipo == TokenType.CHAR
                || tipo == TokenType.BOOLEAN || tipo == TokenType.IDENT) {
            return consume(tipo).getLexeme();
        } else {
            throw erro("Tipo de variavel esperado");
        }
    }

    public String compileVm() {
        gerandoVm = true;
        compile();
        return vmWriter.vmOutput();
    }

    public String getVm() {
        return vmWriter.vmOutput();
    }

    // subroutineDec: ('constructor' | 'function' | 'method')
    // ('void' | type) subroutineName '(' parameterList ')' subroutineBody
    private void compileSubroutineDec() {
        xml.abrir("subroutineDec");
        symbolTable.startSubroutine();

        TokenType subroutineType;
        if (match(TokenType.CONSTRUCTOR)) {
            subroutineType = consume(TokenType.CONSTRUCTOR).getType();
        } else if (match(TokenType.FUNCTION)) {
            subroutineType = consume(TokenType.FUNCTION).getType();
        } else {
            subroutineType = consume(TokenType.METHOD).getType();
            symbolTable.define("this", className, Kind.ARG);
        }

        if (match(TokenType.VOID)) {
            consume(TokenType.VOID);
        } else {
            consumeType();
        }

        String subroutineName = consume(TokenType.IDENT).getLexeme();
        String functionName = className + "." + subroutineName;

        consume(TokenType.LPAREN);
        compileParameterList();
        consume(TokenType.RPAREN);
        compileSubroutineBody(functionName, subroutineType);
        xml.fechar("subroutineDec");
    }

    // parameterList: (type varName (',' type varName)*)?
    private void compileParameterList() {
        xml.abrir("parameterList");
        if (inicioTipo()) {
            String type = consumeType();
            String name = consume(TokenType.IDENT).getLexeme();
            symbolTable.define(name, type, Kind.ARG);
            while (match(TokenType.COMMA)) {
                consume(TokenType.COMMA);
                type = consumeType();
                name = consume(TokenType.IDENT).getLexeme();
                symbolTable.define(name, type, Kind.ARG);
            }
        }
        xml.fechar("parameterList");
    }

    private boolean inicioTipo() {
        return match(TokenType.INT) || match(TokenType.CHAR)
                || match(TokenType.BOOLEAN) || match(TokenType.IDENT);
    }

    // subroutineBody: '{' varDec* statements '}'
    private void compileSubroutineBody(String functionName, TokenType subroutineType) {
        xml.abrir("subroutineBody");
        consume(TokenType.LBRACE);
        while (match(TokenType.VAR)) {
            compileVarDec();
        }

        int nLocals = symbolTable.varCount(Kind.VAR);
        vmWriter.writeFunction(functionName, nLocals);

        if (subroutineType == TokenType.CONSTRUCTOR) {
            vmWriter.writePush(Segment.CONST, symbolTable.varCount(Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(Segment.POINTER, 0);
        } else if (subroutineType == TokenType.METHOD) {
            vmWriter.writePush(Segment.ARG, 0);
            vmWriter.writePop(Segment.POINTER, 0);
        }

        compileStatements();
        consume(TokenType.RBRACE);
        xml.fechar("subroutineBody");
    }

    // varDec: 'var' type varName (',' varName)* ';'
    private void compileVarDec() {
        xml.abrir("varDec");
        consume(TokenType.VAR);
        String type = consumeType();
        String name = consume(TokenType.IDENT).getLexeme();
        symbolTable.define(name, type, Kind.VAR);
        while (match(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            name = consume(TokenType.IDENT).getLexeme();
            symbolTable.define(name, type, Kind.VAR);
        }
        consume(TokenType.SEMICOLON);
        xml.fechar("varDec");
    }

    // statements: statement* (let | if | while | do | return)
    private void compileStatements() {
        xml.abrir("statements");
        while (match(TokenType.LET) || match(TokenType.IF)
                || match(TokenType.WHILE) || match(TokenType.DO)
                || match(TokenType.RETURN)) {
            if (match(TokenType.LET)) {
                compileLet();
            } else if (match(TokenType.IF)) {
                compileIf();
            } else if (match(TokenType.WHILE)) {
                compileWhile();
            } else if (match(TokenType.DO)) {
                compileDo();
            } else {
                compileReturn();
            }
        }
        if (!match(TokenType.RBRACE)) {
            throw erro("Esperado comando ou fechamento do bloco");
        }
        xml.fechar("statements");
    }

    // letStatement: 'let' varName ('[' expression ']')? '=' expression ';'
    private void compileLet() {
        xml.abrir("letStatement");
        consume(TokenType.LET);

        Symbol sym = resolverVariavel(consume(TokenType.IDENT));
        boolean isArray = false;

        if (match(TokenType.LBRACKET)) {
            isArray = true;
            consume(TokenType.LBRACKET);
            // 1. Calcula o índice: i
            compileExpression();
            consume(TokenType.RBRACKET);

            // 2. Coloca a base do array na pilha
            pushVariavel(sym);

            // 3. Soma base + i
            vmWriter.writeArithmetic(Command.ADD);
        }

        consume(TokenType.EQ);
        // Compila o lado direito da expressão
        compileExpression();
        consume(TokenType.SEMICOLON);

        if (isArray) {
            // Se for array: precisamos ancorar a expressão no THAT
            // A pilha tem: [endereço], [valor_da_expressao] (o valor fica no topo)
            // Guardamos o valor temporariamente:
            vmWriter.writePop(Segment.TEMP, 0);
            // Ancoramos o ponteiro THAT no endereço calculado:
            vmWriter.writePop(Segment.POINTER, 1);
            // Puxamos o valor de volta do TEMP e metemos na memória apontada por THAT
            vmWriter.writePush(Segment.TEMP, 0);
            vmWriter.writePop(Segment.THAT, 0);
        } else {
            // Se for variável simples: guardamos no respetivo segmento
            popVariavel(sym);
        }

        xml.fechar("letStatement");
    }

    // ifStatement: 'if' '(' expression ')' '{' statements '}'
    //              ('else' '{' statements '}')?
    private void compileIf() {
        xml.abrir("ifStatement");

        // Captura o número atual do label e incrementa para o próximo uso
        int labelNum = ifLabelNum++;
        String lTrue = "IF_TRUE" + labelNum;
        String lFalse = "IF_FALSE" + labelNum;
        String lEnd = "IF_END" + labelNum;

        consume(TokenType.IF);
        consume(TokenType.LPAREN);
        compileExpression(); // Empilha o resultado da condição (true/false)
        consume(TokenType.RPAREN);

        // Verifica a condição e salta
        vmWriter.writeIf(lTrue);
        vmWriter.writeGoto(lFalse);
        vmWriter.writeLabel(lTrue);

        consume(TokenType.LBRACE);
        compileStatements();
        consume(TokenType.RBRACE);

        if (match(TokenType.ELSE)) {
            // Se o IF verdadeiro rodou, tem de saltar o ELSE
            vmWriter.writeGoto(lEnd);
            vmWriter.writeLabel(lFalse);

            consume(TokenType.ELSE);
            consume(TokenType.LBRACE);
            compileStatements();
            consume(TokenType.RBRACE);

            vmWriter.writeLabel(lEnd);
        } else {
            // Se não houver ELSE, o falso é simplesmente o fim
            vmWriter.writeLabel(lFalse);
        }

        xml.fechar("ifStatement");
    }

    // whileStatement: 'while' '(' expression ')' '{' statements '}'
    private void compileWhile() {
        xml.abrir("whileStatement");

        int labelNum = whileLabelNum++;
        String lExp = "WHILE_EXP" + labelNum;
        String lEnd = "WHILE_END" + labelNum;

        vmWriter.writeLabel(lExp);

        consume(TokenType.WHILE);
        consume(TokenType.LPAREN);
        compileExpression(); // Empilha o resultado da condição
        consume(TokenType.RPAREN);

        // Inverte a condição: se for falso (!condicao), sai do laço
        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(lEnd);

        consume(TokenType.LBRACE);
        compileStatements();
        consume(TokenType.RBRACE);

        // Retorna para reavaliar a expressão
        vmWriter.writeGoto(lExp);
        vmWriter.writeLabel(lEnd);

        xml.fechar("whileStatement");
    }

    // doStatement: 'do' subroutineCall ';'
    private void compileDo() {
        xml.abrir("doStatement");
        consume(TokenType.DO);
        compileSubroutineCall();
        consume(TokenType.SEMICOLON);

        // Remove o valor de retorno (0) do topo da pilha e descarta em TEMP 0
        vmWriter.writePop(Segment.TEMP, 0);

        xml.fechar("doStatement");
    }

    // returnStatement: 'return' expression? ';'
    private void compileReturn() {
        xml.abrir("returnStatement");
        consume(TokenType.RETURN);

        if (!match(TokenType.SEMICOLON)) {
            // Retorna o resultado da expressão
            compileExpression();
        } else {
            // Função void: força o retorno do valor 0
            vmWriter.writePush(Segment.CONST, 0);
        }

        consume(TokenType.SEMICOLON);
        vmWriter.writeReturn(); // Escreve a instrução return na VM
        xml.fechar("returnStatement");
    }

    // expression: term (op term)*
    // Jack nao define precedencia diferente para operadores binarios.
    private void compileExpression() {
        xml.abrir("expression");
        compileTerm();
        while (operadorBinario(peek().getType())) {
            TokenType op = consume(peek().getType()).getType();
            compileTerm();
            compileOperators(op);
        }
        xml.fechar("expression");
    }

    private void compileOperators(TokenType type) {
        if (type == TokenType.ASTERISK) {
            vmWriter.writeCall("Math.multiply", 2);
        } else if (type == TokenType.SLASH) {
            vmWriter.writeCall("Math.divide", 2);
        } else {
            vmWriter.writeArithmetic(typeOperator(type));
        }
    }

    private boolean operadorBinario(TokenType tipo) {
        return switch (tipo) {
            case PLUS, MINUS, ASTERISK, SLASH, AND, OR, LT, GT, EQ -> true;
            default -> false;
        };
    }

    // term: integerConstant | stringConstant | keywordConstant
    //      | varName | varName '[' expression ']'
    //      | subroutineCall | '(' expression ')' | unaryOp term
    private void compileTerm() {
        xml.abrir("term");
        TokenType tipo = peek().getType();
        switch (tipo) {
            case NUMBER -> {
                Token tk = consume(TokenType.NUMBER);
                vmWriter.writePush(Segment.CONST, Integer.parseInt(tk.getLexeme()));
            }
            case STRING -> {
                Token tk = consume(TokenType.STRING);
                String strValue = tk.getLexeme();
                vmWriter.writePush(Segment.CONST, strValue.length());
                vmWriter.writeCall("String.new", 1);
                for (int i = 0; i < strValue.length(); i++) {
                    vmWriter.writePush(Segment.CONST, strValue.charAt(i));
                    vmWriter.writeCall("String.appendChar", 2);
                }
            }
            case TRUE -> {
                consume(TokenType.TRUE);
                vmWriter.writePush(Segment.CONST, 0);
                vmWriter.writeArithmetic(Command.NOT);
            }
            case FALSE, NULL -> {
                consume(tipo);
                vmWriter.writePush(Segment.CONST, 0);
            }
            case THIS -> {
                consume(TokenType.THIS);
                vmWriter.writePush(Segment.POINTER, 0);
            }
            case IDENT -> {
                if (proximo.getType() == TokenType.LPAREN
                        || proximo.getType() == TokenType.DOT) {
                    compileSubroutineCall();
                } else {
                    Symbol sym = resolverVariavel(consume(TokenType.IDENT));
                    if (match(TokenType.LBRACKET)) {
                        consume(TokenType.LBRACKET);
                        compileExpression();
                        pushVariavel(sym);
                        vmWriter.writeArithmetic(Command.ADD);
                        consume(TokenType.RBRACKET);
                        vmWriter.writePop(Segment.POINTER, 1);
                        vmWriter.writePush(Segment.THAT, 0);
                    } else {
                        pushVariavel(sym);
                    }
                }
            }
            case LPAREN -> {
                consume(TokenType.LPAREN);
                compileExpression();
                consume(TokenType.RPAREN);
            }
            case MINUS, NOT -> {
                TokenType op = consume(tipo).getType();
                compileTerm();
                if (op == TokenType.MINUS) {
                    vmWriter.writeArithmetic(Command.NEG);
                } else {
                    vmWriter.writeArithmetic(Command.NOT);
                }
            }
            default -> throw erro("Termo esperado");
        }
        xml.fechar("term");
    }

    // Busca a variavel na tabela de simbolos. Na geracao de VM, uma variavel
    // nao declarada deixaria o codigo incompleto (push/pop faltando), entao o
    // erro e reportado. Na analise sintatica pura (--parse) o VM e descartado
    // e a verificacao nao se aplica.
    private Symbol resolverVariavel(Token token) {
        Symbol sym = symbolTable.resolve(token.getLexeme());
        if (sym == null && gerandoVm) {
            throw new IllegalArgumentException(
                "Erro semantico na linha " + token.getLine()
                + ", coluna " + token.getColumn()
                + ": variavel nao declarada '" + token.getLexeme() + "'"
            );
        }
        return sym;
    }

    private void pushVariavel(Symbol sym) {
        if (sym != null) {
            vmWriter.writePush(kind2Segment(sym.kind()), sym.index());
        }
    }

    private void popVariavel(Symbol sym) {
        if (sym != null) {
            vmWriter.writePop(kind2Segment(sym.kind()), sym.index());
        }
    }

    private Segment kind2Segment(Kind kind) {
        return switch (kind) {
            case STATIC -> Segment.STATIC;
            case FIELD -> Segment.THIS;
            case VAR -> Segment.LOCAL;
            case ARG -> Segment.ARG;
        };
    }

    private Command typeOperator(TokenType type) {
        return switch (type) {
            case PLUS -> Command.ADD;
            case MINUS -> Command.SUB;
            case LT -> Command.LT;
            case GT -> Command.GT;
            case EQ -> Command.EQ;
            case AND -> Command.AND;
            case OR -> Command.OR;
            default -> throw new IllegalArgumentException("Operador invalido: " + type);
        };
    }

    // subroutineCall: subroutineName '(' expressionList ')'
    //               | (className | varName) '.' subroutineName '(' expressionList ')'
    private void compileSubroutineCall() {
        // Guarda o primeiro nome (pode ser o nome da função, o nome do objeto, ou o nome da classe)
        String firstName = consume(TokenType.IDENT).getLexeme();
        String methodName = "";
        int nArgs = 0;
        boolean isMethod = false;

        if (match(TokenType.DOT)) {
            consume(TokenType.DOT);
            String secondName = consume(TokenType.IDENT).getLexeme();

            // Verifica se firstName é um objeto instanciado (está na SymbolTable)
            Symbol sym = symbolTable.resolve(firstName);
            if (sym != null) {
                // É chamada de método num objeto: ex: obj.metodo()
                isMethod = true;
                // Empurra o objeto (o this da sub-rotina chamada)
                vmWriter.writePush(kind2Segment(sym.kind()), sym.index());
                // O nome real da função é TipoDoObjeto.Metodo
                methodName = sym.type() + "." + secondName;
            } else {
                // É chamada de função estática / construtor: ex: Math.multiply()
                methodName = firstName + "." + secondName;
            }
        } else {
            // Chamada de método implícita na mesma classe: ex: metodo()
            isMethod = true;
            // Empurra a instância atual ("this") para o método que vai ser chamado
            vmWriter.writePush(Segment.POINTER, 0);
            methodName = this.className + "." + firstName;
        }

        consume(TokenType.LPAREN);
        nArgs = compileExpressionList();
        consume(TokenType.RPAREN);

        // Se for um método (objeto empurrado antes dos argumentos), aumentamos nArgs em 1
        if (isMethod) {
            nArgs++;
        }

        vmWriter.writeCall(methodName, nArgs);
    }

    // expressionList: (expression (',' expression)*)?
    private int compileExpressionList() {
        xml.abrir("expressionList");
        int nArgs = 0;

        if (!match(TokenType.RPAREN)) {
            compileExpression();
            nArgs++;

            while (match(TokenType.COMMA)) {
                consume(TokenType.COMMA);
                compileExpression();
                nArgs++;
            }
        }
        xml.fechar("expressionList");
        return nArgs;
    }
}
