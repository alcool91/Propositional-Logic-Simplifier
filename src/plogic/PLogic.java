package plogic;
import java.awt.Desktop;
import java.io.*;
import java.util.*;

public class PLogic {
    public static String convertToLaTeX(String s) {
       //Replace unicode characters with latex equivalents
       String a = s;
       a = a.replace("\u21D2", "$\\implies$ ");
       a = a.replace("\u2227", "$\\land$ ");
       a = a.replace("\u2228", "$\\lor$ ");
       a = a.replace("\u00ac", "$\\lnot$ ");
       a = a.replace("\u2295", "$\\oplus$ ");
       a = a.replace("\u00AC", "$\\lnot$ ");
       a = a.replace("\u21D4", "$\\Leftrightarrow$ ");
       return a;
    }
    public static void writeFile(String FileName, String[][] TT,
            int numRows, int numCols, 
            String func, int numProps, String cnf, String simp) 
            throws FileNotFoundException {
        //Write LaTeX file 
        File F = new File(FileName);
        PrintStream Out = new PrintStream(F);
        final String PREAMBLE = "\\documentclass{amsart}\n"+""
                + "\\usepackage[utf8]{inputenc}\n\n"+"\\title{Truth Table}\n"+
                "\\author{Allen Williams' Truth Table Generator }\n"+
                "\\date{\\today}\n\n"+"\\usepackage{booktabs}\n"+
                "\\usepackage{dcolumn}\n\\usepackage{amsthm}\n"+
                "\\usepackage{amsmath}\n\\usepackage{amssymb}\n"+
                "\\usepackage{float}\n\\usepackage[pdftex]{graphicx}\n"+
                "\\usepackage{scrextend}\n\n"+"\\makeatletter\n"+
                "\\newcommand{\\thickhline}{%\n"+
                "    \\noalign {\\ifnum 0=`}\\fi \\hrule height 1pt\n"+
                "    \\futurelet \\reserved@a \\@xhline\n}\n"+
                "\\newcolumntype{\"}{@{\\hskip\\tabcolsep\\vrule width\n"+
                "1pt\\hskip\\tabcolsep}}\n\\makeatother\n\n\\begin{document}\n"+
                "\n\\maketitle\n\n\\begin{table}[H]\n    \\caption{Truth Table"+
                " for "+func + " }\n\\centering\n\\resizebox*{\\textwidth}{!}{"+
                "\\begin{tabular}{";
        final String END = "\\end{document}";
        Out.print(PREAMBLE);
        Out.print("c\"");
        for(int i = 0; i < numCols-1; i++) {
            Out.print("c");
            if(i<numCols-2) Out.print("|");
        }
        Out.print("}\n");
        for(int i = 0; i < numRows; i++) {
            for(int j = 0; j < numCols; j++) {
                Out.print(TT[i][j]);
                if (j!=numCols-1) Out.print(" & ");
            } if(i==0) Out.print(" \\\\ \\thickhline\n");
            else Out.print(" \\\\\n");
            
        }
        Out.print("\\end{tabular}}\n\\end{table}\n\n");
        if (!cnf.equals(""))
            Out.print(func + " has disjunctive normal form " + cnf+"\\\\\n");
        if(func.equals(simp)) {
            Out.print("\\\\"+func + " is probably already simplified");
        }
        else Out.print("\\\\"+func + " can be simplified to " + simp);
        Out.print(END);
        Out.close();
    }
    public static ArrayList<Integer> diff(String a, String b) {
        //Given two strings of equal length find the indices where they differ
        ArrayList<Integer> differences = new ArrayList<>();
        for(int i = 0; i < a.length(); i++) {
            if (a.charAt(i)!=b.charAt(i)) {
                differences.add(i);
            }
        } return differences;
    }
    public static int countOnes(String a) {
        //Given a string return the number of ones in that string
        int ones = 0;
        for(int i = 0; i < a.length(); i++) {
            if(a.charAt(i)=='1') ones++;
        } return ones;
    }
    public static String quineMcCluskey(String[][] tt, int numRows, int numCols,
            int numProps, String cnf) {
        //Simplification algorithm for boolean expression
        if (cnf.equals("")) { return "F"; }
        String[] minterms = cnf.split("\u2228");
        ArrayList<Minterm>[][] groups = new ArrayList[numProps+1][numProps+1];
        ArrayList<Minterm> primes = new ArrayList<>();
        for(int i = 0; i <= numProps; i++) {
            for(int j = 0; j <= numProps; j++) {
                groups[i][j] = new ArrayList<>();
            }
        }
        for(int i = 0; i < minterms.length; i++) {
            String[] a = minterms[i].split("\u2227");
            StringBuilder b = new StringBuilder();
            for (String j: a) {
                j=j.replace("(","");
                j=j.replace(")","");
                if (j.length()==1) {
                    b.append("1");
                } else b.append("0");
            } minterms[i]=b.toString();
        }
        //group minterms by number of ones
        int step = 0;
        for (int i = 0; i < minterms.length; i++) {
            int ones = countOnes(minterms[i]);
            groups[ones][step].add(new Minterm(minterms[i]));
        }
        //Find minterms that differ in only one place from each other and
        //combine them, using X to mark the spot where they differ
        for(int j = 0; j < groups[0].length; j++) {
            for(int i = 0; i < groups.length; i++){
                for(Minterm k: groups[i][j]) {
                    if(!(i==groups.length-1))
                    for(Minterm l: groups[i+1][j]) { 
                        if (diff(k.term,l.term).size() == 1) {
                            if (((k.term.contains("X")) && (l.term.charAt(k.term.indexOf('X'))!='X')) ||
                                    ((l.term.contains("X")) && (k.term.charAt(l.term.indexOf('X'))!='X'))) {
                                continue;
                            }
                            String term;
                            StringBuilder newTerm;
                            term = k.term;
                            newTerm = new StringBuilder(term);
                            newTerm.setCharAt(diff(k.term,l.term).get(0),'X');
                            //when two minterms are combined, create a new 
                            //minterm which has both of the previous minterms
                            //values
                            Minterm t = new Minterm(newTerm.toString());
                            for (int v:k.value) {
                                t.value.add(v);
                            }
                            for (int v:l.value) {
                                t.value.add(v);
                            }
                            groups[i][j+1].add(t);
                            //check the minterms so they cannot be prime
                            //implicants
                            k.checked = true;
                            l.checked = true;
                        }
                    }
                } 
            }
        }
        //Add unchecked minterms above to the list of prime implicants
        for(int i = 0; i < groups.length; i++) {
            for(int j = 0; j < groups[i].length; j++){
                for(Minterm k: groups[i][j]) {
                    if(!k.checked) {
                        primes.add(k);
                    }
                }
            }
        }
        //Create minterm table describing which minterms cover which values
        ArrayList<Minterm> eprimes = new ArrayList<>();
        String[][] tab = new String[primes.size()][minterms.length];
        for(int p = 0; p < primes.size(); p++) {
            for(int s = 0; s < minterms.length; s++) {
                if (primes.get(p).value.contains(new Minterm(minterms[s]).value.get(0))) {
                    tab[p][s]="T";
                } else tab[p][s]="F";
            }
        }
        //Narrow primes down to essential primes
        for(int i = 0; i < minterms.length; i++) {
            boolean one = false;
            boolean onlyOne = true;
            int index = -1;
            for(int j = 0; j < primes.size(); j++) {
                if (tab[j][i].equals("T")){
                    if (one) onlyOne = false;
                    one = true;
                    index = j;
                }
            }if((one) && (onlyOne)) {
                //If only one minterm covers any value, that minterm is
                //essential
                    eprimes.add(primes.get(index));
                    for(int j=0;j<minterms.length;j++) {
                        tab[index][j] = "X";
                    }
            }
            
        }
        //find and eliminate rows that are dominated by other rows.
        for (int i = 0; i < primes.size(); i++) {
            for (int j = 0; j < primes.size(); j++) {
                if (i!=j) {
                    int counti=0, countj=0, counts=0;
                    for(int k = 0; k < minterms.length; k++) {
                        if (tab[i][k].equals("T")) counti++;
                        if (tab[j][k].equals("T")) countj++;
                        if ((tab[i][k].equals("T")) && (tab[j][k].equals("T"))) {
                            counts++;
                        }
                    }
                    if ((counti == counts) || (countj == counts)) {
                        if (counti > countj) {
                            for(int k = 0; k < minterms.length; k++) {
                                tab[j][k]="X";
                            }
                        } else if (countj>counti) {
                            for(int k = 0; k < minterms.length; k++) {
                                tab[i][k]="X";
                            }
                        } else if (counti == countj) {
                            for(int k = 0; k < minterms.length; k++) {
                                tab[i][k]="X";
                            }
                        }
                    }
                }
            }
        }
        //Add all essential prime implicants to a list
        for(int i = 0; i < primes.size(); i++) {
            boolean added=false;
            for(int j = 0; j < minterms.length; j++)
            if(!(tab[i][j].equals("X"))) {
                if(!added) {
                    eprimes.add(primes.get(i));
                    added=true;
                }
            }
        }
        StringBuilder toReturn = new StringBuilder();
        //Print out the essential prime implicants
        //Convert from string of 1s, 0s, and Xs back into propositions
        for(int p = 0; p < eprimes.size(); p++) {
            char[] a = eprimes.get(p).term.toCharArray();
            int sizea=0;
            for (char ch: a) {
                if(ch!='X') sizea++;
            }
            if ((sizea > 1) && (eprimes.size()>1)) toReturn.append("(");
            for(int i = 0; i < a.length; i++) {
                if (a[i] == '1') toReturn.append(tt[0][i]);
                else if (a[i] == '0') toReturn.append("\u00ac"+tt[0][i]);
                else if (a[i]=='X') continue;
                if (i!=a.length-1) toReturn.append("\u2227");
            } if (!(toReturn.length()==0) && (toReturn.charAt(toReturn.length()-1)=='\u2227'))
                toReturn.setLength(toReturn.length()-1);
            if((sizea > 1) && (eprimes.size()>1)) toReturn.append(")");
            if(p!=eprimes.size()-1) toReturn.append("\u2228");
        }
        if (toReturn.length()==0) return("T");
        return toReturn.toString();
    }
        
    public static void readFile(String fileName) throws IOException {
        //Open a file with the desktops preferred application for that type
        //of file
        File F = new File(fileName);
        Desktop desktop = null;
        if (desktop.isDesktopSupported()) {
            desktop=Desktop.getDesktop();
        } desktop.open(F);
    }
    public static String getDNF(String[][] tt, int numRows, int numCols, 
            int numProps) {
        //Disjunctive normal form is taken directly from the matrix containing
        //truth table values
        StringBuilder toReturn = new StringBuilder();
        List<String> minterms = new ArrayList<>();
        for(int i = 1; i < numRows; i++) {
            if (tt[i][numCols-1].equals("T")) {
                StringBuilder a = new StringBuilder();
                for(int j = 0; j<numProps; j++) {
                    if(tt[i][j].equals("T")) {a.append(tt[0][j]);}
                    else if (tt[i][j].equals("F")) {a.append("\u00AC"+tt[0][j]);}
                    else System.out.print("Something went wrong finding DNF");
                    if(j<numProps-1) {a.append("\u2227");}
                } minterms.add(a.toString());
            }
        } for(int i = 0; i < minterms.size(); i++) {
            int realLength = 0;
            char[] tmp = minterms.get(i).toCharArray();
            for(char ch: tmp) {
                if(ch!='\u00AC') realLength++;
            }
            if(realLength>1) 
                toReturn.append("("+minterms.get(i)+")");
            else if (realLength==1)
                toReturn.append(minterms.get(i));
            if(i<minterms.size()-1) toReturn.append("\u2228");
        } return toReturn.toString();
    }
    public static boolean isProp(char c) {
        //alphabetic characters allowed as propositions
        return ((0x40 < c) && (0x5B > c) || (0x60 < c) && (0x7B > c));
    }
    public static boolean isBin(char c) {
        //returns true if the character is a unicode character for a binary
        //connective
        return ((c=='\u2227')||(c=='\u2228')||(c=='\u2295')||(c=='\u21D2')  
               || (c=='\u21D4'));
    }
    public static boolean isUn(char c){
        //returns true if the character is a unary connective
        return (c=='\u00AC');
    }
    public static TTBool evalLogic(TTBool p, TTBool q, char op) {
        //Evaluate logical expressions and, or, xor, implies, equivalence
    TTBool result;
    if      (op == '\u2227') result = new TTBool(p.value && q.value);
    else if (op == '\u2228') result = new TTBool(p.value || q.value);
    else if (op == '\u21D2') result = new TTBool(!p.value || q.value);
    else if (op == '\u2295') result = new TTBool((p.value && !q.value)
                                 || (!p.value && q.value));
    else if (op == '\u21D4') result = new TTBool((p.value && q.value) 
                                 || (!p.value && !q.value));
    else throw new IllegalArgumentException("Unknown Binary Operator");
    return result;
    }
    public static TTBool evalLogic(TTBool p, char op) {
        //evaluate negation
        TTBool result;
        if (op == '\u00AC') result = new TTBool(!p.value);
        else throw new IllegalArgumentException("Unknown Unary Operator");
        return result;
    }
    public static String replaceOperators(String s) {
        //Replace allowed keyboard characters with unicode logical operators
       s = s.replace(">", "\u21D2");
       s = s.replace("&", "\u2227");
       s = s.replace("|", "\u2228");
       s = s.replace("!", "\u00AC");
       s = s.replace("*", "\u2295");
       s = s.replace("=", "\u21D4");
       return s;
    }
    public static ArrayList<Character> fillPropositions(String s, 
            ArrayList<Character> propositions) {
        //Take the propositions from user's input and store them
        char t;
        for (int i = 0; i < s.length(); i++) {
           t = s.charAt(i);
           if((0x40 < t) && (0x5B>t)) {
               if(!propositions.contains(t)) propositions.add(t);
           } else if ((0x60 < t) && (0x7B>t)) {
               if(!propositions.contains((char)(t-0x20)))
                       propositions.add((char)(t-0x20));
           }
       } return propositions;
    }
    public static Queue<String> convertToPostfix(String s, Queue<String> valq) {
        //Take an infix expression and return a queue containing a postfix
        //expression
        Stack<Character> tstack = new Stack<Character>();
        for(int i = 0; i < s.length(); i++) {
           char m = s.charAt(i);
           if(m=='(') tstack.push(m);
           else if (isProp(m)) {
               if ((0x40< m) && (0x5B>m)) valq.enqueue(Character.toString(m));
               else valq.enqueue(Character.toString((char)(m-0x20)));
               while(!(tstack.isEmpty()) && (tstack.peek()=='\u00AC')) {
                   valq.enqueue(Character.toString(tstack.pop()));
               }
           }
           else if (m != ')') {
               tstack.push(m);
           }
           else {
               while (tstack.peek()!='(') {
                   valq.enqueue(Character.toString(tstack.pop()));
               } tstack.pop();
               if((!tstack.isEmpty())&&(tstack.peek()=='\u00AC')) {
                   valq.enqueue(Character.toString(tstack.pop()));
               }
           }       
       } while(!tstack.isEmpty()) valq.enqueue(Character.toString(tstack.pop()));
        return valq;
    }
    public static void main(String[] args) throws FileNotFoundException,
            IOException,
            InterruptedException {
       Stack<String> evalStack = new Stack<>();
       Queue<String> valq2 = new Queue<>();
       Queue<String> valq = new Queue<>();
       ArrayList<Character> propositions = new ArrayList<>();
       List<String> steps = new ArrayList<>();
       Scanner stdin = new Scanner(System.in);
       String s, dnf, simp;
       char t;
       System.out.print("Enter Expression (Use \"&\" for \u2227, \"|\" for"+
               " \u2228,\n "+
               " \">\" for \u21D2, \"!\" for \u00AC, \"*\" for \u2295, and \"=\" for "
               + "\u21D4.  \nPlease include all necessary parentheses): ");
       s = stdin.nextLine();
       s = replaceOperators(s);
       fillPropositions(s, propositions);
       //Generate PostFix Queue
       convertToPostfix(s, valq);
       valq2 = valq.Copy();
       //Find intermediate steps
       while(!valq2.isEmpty()) {
           char a;
           a = valq2.dequeue().charAt(0);
           if (isProp(a)) evalStack.push(Character.toString(a));
           else if (isBin(a)) {
               String tempVal1, tempVal2;
               tempVal2 = evalStack.pop();
               tempVal1 = evalStack.pop();
               int l1 = tempVal1.length();
               int l2 = tempVal2.length();
               //Remove "NOT" from length
               if (tempVal1.contains("\u00AC")) {
                   char[] tmp = tempVal1.toCharArray();
                   for(char i: tmp) {
                       if (i=='\u00AC') l1--;
                   }
               } 
               if (tempVal2.contains("\u00AC")) {
                   char[] tmp = tempVal2.toCharArray();
                   for(char i: tmp) {
                       if (i=='\u00AC') l2--;
                   }
               }
               if (!(l1==1) && (l2==1)) { 
                   evalStack.push("("+tempVal1+")"+a+tempVal2);
                   steps.add("("+tempVal1+")"+a+tempVal2);
               }else if (!(l2==1) && (l1==1)){
                   evalStack.push(tempVal1+a+"("+tempVal2+")");
                   steps.add(tempVal1+a+"("+tempVal2+")");
               }else if (!((l1==1)  || (l2==1))) {
                  evalStack.push("("+tempVal1+")"+a+"("+tempVal2+")");
                  steps.add("("+tempVal1+")"+a+"("+tempVal2+")");
               }else {
                   evalStack.push(tempVal1+a+tempVal2);
                   steps.add(tempVal1+a+tempVal2);
               }
           } else if (isUn(a)) {
               String tempVal1;
               tempVal1 = evalStack.pop();
               if (!(tempVal1.length()==1)) {
                   evalStack.push(a+"("+tempVal1+")");
                   steps.add(a+"("+tempVal1+")");
               } else {
                   evalStack.push(a+tempVal1);
                   steps.add(a+tempVal1);
               }
           }
       }
       int n = propositions.size();
       String[][] fileMatrix;
       fileMatrix = new String[(int)Math.pow(2, n)+1][n+steps.size()];
       File textTT = new File("tt.txt");
       PrintStream txtOut = new PrintStream(textTT);
       for (char i: propositions) {
           System.out.print(i + "\t");
           txtOut.print(i+"\t");
       }
       for (String i: steps) {
           System.out.print(i + "\t");
           txtOut.print(i + "\t");
       }
       txtOut.println();
       System.out.println();
       for (int i = 0; i < 90; i++) {
           System.out.print("\u203E");
           txtOut.print("\u203E");
       }
       txtOut.println();
       System.out.println();
       for(int i = 0; i < propositions.size(); i++) {
           fileMatrix[0][i] = Character.toString(propositions.get(i));
       }
       for(int i = 0; i < steps.size(); i++) {
           String a;
           a = steps.get(i);
           a = convertToLaTeX(a);
           fileMatrix[0][n+i] = a;
       }
       for(int i = 1; i <= Math.pow(2,n); i++) {
           Map<String, TTBool> bvals = new HashMap<String, TTBool>();
           TTBool tval;
           //Map propositions to appropriate truth values
           for (int j = 0; j < n; j++) {
               tval = new TTBool(Math.ceil((i/(Math.pow(2, n-j-1)))) % 2 == 0);
               bvals.put(Character.toString(propositions.get(j)), tval);
               System.out.print(tval + "\t");
               txtOut.print(tval + "\t");
               fileMatrix[i][j]=tval.toString();
           }
            int count = 0;
            evalStack.empty();
            valq2 = valq.Copy();
            while(!valq2.isEmpty()) {
               char   a;
               TTBool q;
               a = valq2.dequeue().charAt(0);
               if (isProp(a)) evalStack.push(Character.toString(a));
               else if (isBin(a)) {
                   String tempVal1, tempVal2;
                   tempVal2 = evalStack.pop();
                   tempVal1 = evalStack.pop();
                   q = evalLogic(bvals.get(tempVal1), bvals.get(tempVal2), a);
                   bvals.put(steps.get(count), q);
                   evalStack.push(steps.get(count));
                   count++;
                   System.out.print(q+"\t");
                   txtOut.print(q+"\t");
                   fileMatrix[i][n+count-1] = q.toString();
               }
               else if (isUn(a)) {
                   String tempVal1;
                   tempVal1 = evalStack.pop();
                   q = evalLogic(bvals.get(tempVal1), a);
                   bvals.put(steps.get(count), q);
                   evalStack.push(steps.get(count));
                   count++;
                   System.out.print(q+"\t");
                   txtOut.print(q+"\t");
                   fileMatrix[i][n+count-1] = q.toString();
               }
            } System.out.println();
            txtOut.println();
       }
       dnf = getDNF(fileMatrix, (int)Math.pow(2, n)+1, 
                n+steps.size(),n);
       simp = quineMcCluskey(fileMatrix,(int)Math.pow(2, n)+1,
                n+steps.size(),n,dnf);
       writeFile("tt.tex", fileMatrix,(int)Math.pow(2, n)+1,n+steps.size(),
                    fileMatrix[0][n+steps.size()-1],n, convertToLaTeX(dnf), 
                    convertToLaTeX(simp));     
        System.out.println(dnf);
        txtOut.println("Disjunctive Normal Form : " + dnf);
        System.out.println(simp);
        txtOut.println("Simplified Form : " + simp);
        txtOut.println("\n\nInstall LaTeX for a better experience, or take "+
                "the file \"tt.tex\" in this directory\nto sharelatex.com");
        txtOut.close();
        try {
            //Try to use pdflatex to convert tex file to pdf and open it
            Process proc = 
                Runtime.getRuntime().exec("/Library/TeX/texbin/pdflatex tt.tex");
            BufferedReader Reader = 
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            File log = new File("log.txt");
            PrintStream P = new PrintStream(log);
            while((line=Reader.readLine())!=null) {
                P.println(line+"\n");
            } P.close();
            proc.waitFor();
            readFile("tt.pdf");
        } catch(Exception e) {
            //if the tex file cannot be converted, open txt file
            readFile("tt.txt");
        }
    }
}
