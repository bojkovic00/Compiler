package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.ast.Program;

import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;


public class Compiler {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		Logger log = Logger.getLogger(Compiler.class);
		if (args.length < 2) {
			log.error("Not enough arguments supplied! Usage: MJParser <source-file> <obj-file> ");
			return;
		}
		
		File sourceCode = new File(args[0]);
		if (!sourceCode.exists()) {
			log.error("Source file [" + sourceCode.getAbsolutePath() + "] not found!");
			return;
		}
			 
		log.info("Compiling source file: " + sourceCode.getAbsolutePath());
		
		try (BufferedReader br = new BufferedReader(new FileReader(sourceCode))) {
			Yylex lexer = new Yylex(br);
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        
	      Program prog=(Program) (s.value);
	      
	      Tabela.init();
	      
	      log.info("========================");
	      log.info(prog.toString(""));
	      log.info("========================");
	      
	   // ispis prepoznatih programskih konstrukcija
	   			SemanticAnalyzer sa = new SemanticAnalyzer();
	   			prog.traverseBottomUp(sa);
	       
	   			Tabela.dump();
	   			
	   			
	   			if( sa.passed() && !p.errorDetected){
					File objFile = new File(""+args[1]);
					if(objFile.exists()) objFile.delete();
					
					CodeGenerator codeGenerator = new CodeGenerator();
					prog.traverseBottomUp(codeGenerator);
					Code.dataSize = sa.nVars;
					Code.mainPc = codeGenerator.getMainPc();
					Code.write(new FileOutputStream(objFile));
					
					if(codeGenerator.postojiMain())
					{
						log.info("Parsiranje uspesno zavrseno!");
					}
					else
					{
						log.info("NE POSTOJI MAIN FUNKCIJA!");
					}
					
					
				}else{
					log.error("Parsiranje NIJE uspesno zavrseno!");
				}
	   			
	   			
	   			
	   			
		}
	}
}