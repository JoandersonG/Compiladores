import java.io.*;
import java.util.*;

public class MainCompilador {
    private static Map<String, String> errosTabela;
    public static void main(String[] args) {

        //é necessário um arquivo de entrada com o programa a ser compilado
        try {
            String path = "/home/joanderson/Coding/Compilador/src/inRobot.txt";

            List<Token> tokenTable = analiseLexica(path);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static List<Token> analiseLexica(String path) throws IOException {
        char[] line;

        LinhaAutomato[] autReconhecedorCadeias = new LinhaAutomato[5];
        autReconhecedorCadeias[0] = new LinhaAutomato(1, 2, 4);
        autReconhecedorCadeias[1] = new LinhaAutomato(1, 3, 4);
        autReconhecedorCadeias[2] = new LinhaAutomato(4, 2, 4);
        autReconhecedorCadeias[3] = new LinhaAutomato(3, 3, 4);
        autReconhecedorCadeias[4] = new LinhaAutomato(4, 4, 4);//4 é estado de erro

        // Inicializando tabela de palavras reservadas
        TabelaReservada tabelaReservada = new TabelaReservada();
        Map<String, String> palavrasReservadas = tabelaReservada.getPalavrasReservadas();

        // Inicializando tabela de descrições dos erros
        TabelaErros tabelaErros = new TabelaErros();
        errosTabela = tabelaErros.getErrors();

        // Inicializando lista de erros
        List<Erro> listError = new ArrayList<Erro>();

        // Inicializando lista de tokens resultantes
        List<Token> tokenList = new ArrayList<Token>();

        // Inicializando tabela de símbolos
        Map<String, TabelaSimbolosEntry> tabelaSimbolos = new HashMap<String, TabelaSimbolosEntry>();

        int estadoAtual, estadoAnterior;
        int linha = 0;
        int coluna = 0;
        StringBuilder lexema = new StringBuilder();

        Scanner sc = new Scanner(new File(path));
        while (sc.hasNextLine()) {
            linha++;
            line = sc.nextLine().toCharArray();
            //começo a procurar por tokens
            estadoAtual = 0;
            estadoAnterior = 0;

            for (int i = 0; i <= line.length; i++) {
                // Se ler um espaço em branco atualiza o contador de coluna
                if (lexema.toString().compareTo("") == 0)
                    coluna = i + 1;

                if (i == line.length) { /*se estou no fim da linha*/
                    if (estadoAtual == 4) {//estado de erro
                        switch (estadoAnterior) {
                            // Caso o último estado anterior tenha sido o 1 é um identificador mal formado
                            case 1: {
                                Object erroObj = errosTabela.get("<id_mal_formado>");
                                Erro erro = new Erro("<id_mal_formado>", erroObj.toString()
                                        .replace("{lexema}", getLexemaOriginal(path,linha,coluna))
                                        .replace("{linha}", String.valueOf(linha))
                                        .replace("{coluna}", String.valueOf(coluna))
                                );
                                listError.add(erro);
                                break;
                            }
                            // Caso o último estado anterior tenha sido o 2 é um número mal formado
                            case 2: {
                                Object erroObj = errosTabela.get("<nmr_mal_formado>");
                                Erro erro = new Erro("<nmr_mal_formado>", erroObj.toString()
                                        .replace("{lexema}", getLexemaOriginal(path,linha,coluna))
                                        .replace("{linha}", String.valueOf(linha))
                                        .replace("{coluna}", String.valueOf(coluna))
                                );
                                listError.add(erro);
                                break;
                            }
                            // É um símbolo mal formado caso contrário
                            default: {
                                Object erroObj = errosTabela.get("<simb_not_id>");
                                Erro erro = new Erro("<simb_not_id>", erroObj.toString()
                                        .replace("{lexema}", getLexemaOriginal(path,linha,coluna))
                                        .replace("{linha}", String.valueOf(linha))
                                        .replace("{coluna}", String.valueOf(coluna))
                                );
                                listError.add(erro);
                            }
                        }
                    } else {
                        // Verifica se o identificador é uma palavra reservada
                        if (palavrasReservadas.containsKey(lexema.toString())) {
                            Token token = new Token(lexema.toString(), palavrasReservadas.get(lexema.toString()), linha, coluna);
                            tokenList.add(token);
                        }
                        // Verifica se é um número
                        else if (lexema.toString().matches("\\d+")) {
                            Token token = new Token(lexema.toString(), "<numero, " + lexema.toString() + ">", linha, coluna);
                            tokenList.add(token);
                        }
                        // Caso contrário é um identificador
                        else {
                            String tokenString = "<identificador, " + lexema.toString() + ">";
                            if (tokenString.matches("<identificador, >")) {
                                continue;//token vazio (apenas com quebras) não inserido
                            }
                            Token token = new Token(lexema.toString(), tokenString, linha, coluna);
                            tokenList.add(token);

                            // Verifica se já existe na tabela de símbolos e insere caso contrário
                            if (!tabelaSimbolos.containsKey(tokenString)) {
                                TabelaSimbolosEntry newEntry = new TabelaSimbolosEntry(tokenString, lexema.toString());
                                tabelaSimbolos.put(tokenString, newEntry);
                            }
                        }
                    }

                    estadoAtual = 0;
                    lexema = new StringBuilder();//resetei o lexema
                    break;
                }
                char c = line[i];
                if (c == '#') {
                    if (estadoAtual == 0) {
                        break;//começo de linha tem comentário
                    }
                    if (estadoAtual == 4) {//estado de erro
                        switch (estadoAnterior) {
                            // Caso o último estado anterior tenha sido o 1 é um identificador mal formado
                            case 1: {
                                Object erroObj = errosTabela.get("<id_mal_formado>");

                                Erro erro = new Erro("<id_mal_formado>", erroObj.toString()
                                        .replace("{lexema}", getLexemaOriginal(path,linha,coluna))
                                        .replace("{linha}", String.valueOf(linha))
                                        .replace("{coluna}", String.valueOf(coluna))
                                );
                                listError.add(erro);
                                break;
                            }
                            // Caso o último estado anterior tenha sido o 2 é um número mal formado
                            case 2: {
                                Object erroObj = errosTabela.get("<nmr_mal_formado>");
                                Erro erro = new Erro("<nmr_mal_formado>", erroObj.toString()
                                        .replace("{lexema}", getLexemaOriginal(path,linha,coluna))
                                        .replace("{linha}", String.valueOf(linha))
                                        .replace("{coluna}", String.valueOf(coluna))
                                );
                                listError.add(erro);
                                break;
                            }
                            // É um símbolo mal formado caso contrário
                            default: {
                                Object erroObj = errosTabela.get("<simb_not_id>");
                                Erro erro = new Erro("<simb_not_id>", erroObj.toString()
                                        .replace("{lexema}", getLexemaOriginal(path,linha,coluna))
                                        .replace("{linha}", String.valueOf(linha))
                                        .replace("{coluna}", String.valueOf(coluna))
                                );
                                listError.add(erro);
                            }
                        }
                    } else {
                        // Verifica se o identificador é uma palavra reservada
                        if (palavrasReservadas.containsKey(lexema.toString())) {
                            Token token = new Token(lexema.toString(), palavrasReservadas.get(lexema.toString()), linha, coluna);
                            tokenList.add(token);
                        }
                        // Verifica se é um número
                        else if (lexema.toString().matches("\\d+")) {
                            Token token = new Token(lexema.toString(), "<numero, " + lexema.toString() + ">", linha, coluna);
                            tokenList.add(token);
                        }
                        // Caso contrário é um identificador
                        else {
                            String tokenString = "<identificador, " + lexema.toString() + ">";
                            Token token = new Token(lexema.toString(), tokenString, linha, coluna);
                            tokenList.add(token);

                            // Verifica se já existe na tabela de símbolos e insere caso contrário
                            if (!tabelaSimbolos.containsKey(tokenString)) {
                                TabelaSimbolosEntry newEntry = new TabelaSimbolosEntry(tokenString, lexema.toString());
                                tabelaSimbolos.put(tokenString, newEntry);
                            }
                        }
                    }

                    estadoAtual = 0;
                    lexema = new StringBuilder();//resetei o lexema
                    break; // ignora restante da linha em caso de comentário
                }

                if (c <= 32) {
                    if (estadoAtual == 0) continue;
                    if (estadoAtual == 4) {//estado de erro
                        switch (estadoAnterior) {
                            // Caso o último estado anterior tenha sido o 1 é um identificador mal formado
                            case 1: {
                                Object erroObj = errosTabela.get("<id_mal_formado>");
                                Erro erro = new Erro("<id_mal_formado>", erroObj.toString()
                                        .replace("{lexema}", getLexemaOriginal(path,linha,coluna))
                                        .replace("{linha}", String.valueOf(linha))
                                        .replace("{coluna}", String.valueOf(coluna))
                                );
                                listError.add(erro);
                                break;
                            }
                            // Caso o último estado anterior tenha sido o 2 é um número mal formado
                            case 2: {
                                Object erroObj = errosTabela.get("<nmr_mal_formado>");
                                Erro erro = new Erro("<nmr_mal_formado>", erroObj.toString()
                                        .replace("{lexema}", getLexemaOriginal(path,linha,coluna))
                                        .replace("{linha}", String.valueOf(linha))
                                        .replace("{coluna}", String.valueOf(coluna))
                                );
                                listError.add(erro);
                                break;
                            }
                            // É um símbolo mal formado caso contrário
                            default: {



                                Object erroObj = errosTabela.get("<simb_not_id>");
                                Erro erro = new Erro("<simb_not_id>", erroObj.toString()
                                        .replace("{lexema}", getLexemaOriginal(path,linha,coluna))
                                        .replace("{linha}", String.valueOf(linha))
                                        .replace("{coluna}", String.valueOf(coluna))
                                );
                                listError.add(erro);
                            }
                        }
                    } else {
                        // Verifica se o identificador é uma palavra reservada
                        if (palavrasReservadas.containsKey(lexema.toString())) {
                            Token token = new Token(lexema.toString(), palavrasReservadas.get(lexema.toString()), linha, coluna);
                            tokenList.add(token);
                        }
                        // Verifica se é um número
                        else if (lexema.toString().matches("\\d+")) {
                            Token token = new Token(lexema.toString(), "<numero, " + lexema.toString() + ">", linha, coluna);
                            tokenList.add(token);
                        }
                        // Caso contrário é um identificador
                        else {
                            String tokenString = "<identificador, " + lexema.toString() + ">";
                            Token token = new Token(lexema.toString(), tokenString, linha, coluna);
                            tokenList.add(token);

                            // Verifica se já existe na tabela de símbolos e insere caso contrário
                            if (!tabelaSimbolos.containsKey(tokenString)) {
                                TabelaSimbolosEntry newEntry = new TabelaSimbolosEntry(tokenString, lexema.toString());
                                tabelaSimbolos.put(tokenString, newEntry);
                            }
                        }
                    }

                    estadoAtual = 0;
                    lexema = new StringBuilder();//resetei o lexema
                    continue;//finaliza a palavra atual
                }

                if (97 <= c && c <= 122) {//letra minuscula
                    c -= 32;//converto em maiúscula
                }

                lexema.append(c);

                //Autômato reconhecedor de cadeias:
                if (estadoAtual != 4)
                    estadoAnterior = estadoAtual;

                if (65 <= c && c <= 90) {//letra
                    switch (estadoAtual) {
                        case 0://caso eu esteja no estado 0 e leia uma letra,
                            estadoAtual = 1;//muda para o estado 1;
                            break;
                        case 2:
                            estadoAtual = 4;
                            break;
                        case 1:
                        case 3:
                            //não muda de estado
                            break;
                        case 4:
                            //estado de erro. Não muda de estado
                            break;
                    }
                } else if (48 <= c && c <= 57) {//número
                    switch (estadoAtual) {
                        case 0://caso eu esteja no estado 0 e leia uma letra,
                            estadoAtual = 2;//muda para o estado 1;
                            break;
                        case 1:
                            estadoAtual = 3;
                            break;
                        case 2:
                        case 3:
                            //não muda de estado
                            break;
                        case 4:
                            //estado de erro. Não muda de estado
                            break;
                    }
                } else {//é outro símbolo
                    estadoAnterior = 4;
                    estadoAtual = 4;//erro
                    continue;
                }
            }
        }

        sc.close();

        if (!listError.isEmpty()) {
            System.out.println("ERROS");
            for (Erro erro : listError) {
                System.out.println("Code: " + erro.getCodigo() + " Erro: " + erro.getMensagem());
            }
        }

        if (!tokenList.isEmpty()) {
            System.out.println();
            System.out.println("TOKENS");
            for (Token token : tokenList) {
                System.out.println("Lexema: " + token.getLexema()
                        + " Token: " + token.getToken()
                        + " Linha: " + token.getLinha()
                        + " Coluna: " + token.getColuna());
            }
        }

        if (!tabelaSimbolos.isEmpty()) {
            System.out.println();
            System.out.println("SIMBOLOS");
            for (String token : tabelaSimbolos.keySet()) {
                System.out.println("Lexema: " + tabelaSimbolos.get(token).getLexema() + " Token: " + tabelaSimbolos.get(token).getToken());
            }
        }

        return tokenList;
    }

    private static String getLexemaOriginal(String path, int linha, int coluna) throws FileNotFoundException {
        Scanner sc2 = new Scanner(new File(path));
        int cont = 1;
        String aux = sc2.nextLine();
        while (cont != linha){
            aux = sc2.nextLine();
            cont++;
        }//saio na linha certa
        cont = 1;
        char[] auxToChar = aux.toCharArray();
        //char[] lexemaOriginal = new char[];
        StringBuilder sbAux = new StringBuilder();
        for (int j = coluna-1; j < auxToChar.length; j++) {
            sbAux.append(String.valueOf(auxToChar[j]));
            if (auxToChar[j] <= 32) break;//quebra de texto
        }
        sc2.close();
        return sbAux.toString();
    }


}
