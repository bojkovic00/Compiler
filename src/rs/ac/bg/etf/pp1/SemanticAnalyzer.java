package rs.ac.bg.etf.pp1;

import org.apache.log4j.*;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.concepts.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class SemanticAnalyzer extends  VisitorAdaptor  {
	
	boolean uglasteNasao = false;
	boolean uglasteNasaoPrvi = false;
	
	List<Obj> listaKonstanti = new ArrayList<>();
	List<Obj> listaPromenljivih = new ArrayList<>();
		
	
	int nVars = 0;
	int level=0;
	
	Obj currMethod = null;
	int brArg=0;
	int newBio=0;
	
	List<Obj> fje = new ArrayList<>();
	List<Struct> params = new ArrayList<>();
	
	public ArrayList<Integer> whileBreakLst = new ArrayList<Integer>();
	public ArrayList<Integer> whileContinueLst = new ArrayList<Integer>();
	
	////////////// GRESKE /////////////////
	Logger log = Logger.getLogger(getClass());
	boolean errorDetected = false;
	public boolean passed() {
		return !errorDetected;
	}
	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder();
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" Pretraga na ").append(line);
		msg.append(message);
		log.info(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder();
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" Pretraga na ").append(line);
		msg.append(message);
		log.info(msg.toString());
	}
	
	////////////////////////// PROGRAM ///////////////////////
	public void visit(ImePrograma imePrograma) {
		imePrograma.obj = Tabela.insert(Obj.Prog, imePrograma.getPName(), Tabela.noType);
		Tabela.openScope();
	}
	
	public void visit(Program program) {
		nVars = Tabela.currentScope.getnVars();
		Tabela.chainLocalSymbols(program.getImePrograma().obj);
		Tabela.closeScope();
	}
	////////////////////////////  KONSTANTE   /////////////////////////////////////

	public int saveNumConst;
	public int saveBoolConst;
	public char saveCharConst;
	
	public void visit(VrsteKonstNUM numKonst) {
		
		if(zapamtistruct==Tabela.intType) {
			numKonst.struct =Tabela.intType;	
			saveNumConst=numKonst.getIntVrednost();
		}else {
				report_error(" Greska: ne poklapaju se tipovi! ",numKonst);
		}
		
		
	}
	
	public void visit(VrsteKonstCHAR charKonst) {
		
		if(zapamtistruct==Tabela.charType) {
			charKonst.struct =Tabela.charType;
			saveCharConst = charKonst.getCharVrednost().charAt(1);
		}else {			
			report_error(" Greska: ne poklapaju se tipovi! ",charKonst);
		}
		
		
	}
	//VrsteKonst d;
	//IZRACUNAVANJE PREBACI TAMO GDE SE UBACUJE U LISTU
	public void visit(VrsteKonstBOOL boolKonst) {
		if(zapamtistruct==Tabela.boolType) {
			boolKonst.struct =Tabela.boolType;		
			if(boolKonst.getBoolVrednost().equals("true") ){
				saveBoolConst=1;
			}else {
				saveBoolConst=0;
			}			
		}else {		
			report_error(" Greska: ne poklapaju se tipovi! ",boolKonst);
		}
		
	}

	
	public void visit(NemaListaKonst nemaKonst) {
		nemaKonst.struct =Tabela.noType;
		
	}
	
	public void visit(ListaKonstI listaKonst) {
	
		//report_info("lista obilazi",listaKonst);
		
		if (Tabela.find(listaKonst.getNovaKonst()) != Tabela.noObj && level == Tabela.find(listaKonst.getNovaKonst()).getLevel()) {
			report_error(" Greska: dva puta deklarisana promenljiva! " + listaKonst.getNovaKonst(), listaKonst);
			return;
		}
		
		//tip konstante koja se dodaje
		listaKonst.struct=listaKonst.getVrsteKonst().struct;
		
		
		if(listaKonst.struct.equals(Tabela.intType)) {
			//TRENUTNI SCOPE JE NULA
			listaKonstanti.add(new Obj(Obj.Con, listaKonst.getNovaKonst(), listaKonst.struct, saveNumConst,level));
		}
		else if(listaKonst.struct.equals(Tabela.charType)) {
			listaKonstanti.add(new Obj(Obj.Con, listaKonst.getNovaKonst(), listaKonst.struct, saveCharConst,level));
			
		}
		else if(listaKonst.struct.equals(Tabela.boolType)) {
			listaKonstanti.add(new Obj(Obj.Con, listaKonst.getNovaKonst(), listaKonst.struct, saveBoolConst,level));
		}
		
	}
	
	public void visit(DeklKonstI deklKonst) {
			
		if (Tabela.find(deklKonst.getPrvaKonst().getImeProm()) != Tabela.noObj && level == Tabela.find(deklKonst.getPrvaKonst().getImeProm()).getLevel()) {
			report_error(" Greska: dva puta deklarisana promenljiva! " + deklKonst.getPrvaKonst().getImeProm(), deklKonst);
			return;
		}
		
		deklKonst.struct=deklKonst.getPrvaKonst().getVrsteKonst().struct;		 
		
		Obj constNode;
		if(deklKonst.struct==null) {return;}
		else
		if(deklKonst.struct.equals(Tabela.intType)) {	
			constNode= Tabela.insert(Obj.Con, deklKonst.getPrvaKonst().getImeProm(), deklKonst.struct);
			constNode.setAdr(saveNumConstPrva);
			constNode.setLevel(level);
			report_info( " (" + deklKonst.getPrvaKonst().getImeProm() +"), Nadjeno Con "+deklKonst.getPrvaKonst().getImeProm()+": "
					+ispisCvora(deklKonst.struct)+", "+constNode.getAdr()
					+", "+constNode.getLevel(),deklKonst);
		}
		else if(deklKonst.struct.equals(Tabela.charType)) {
			constNode= Tabela.insert(Obj.Con, deklKonst.getPrvaKonst().getImeProm(), deklKonst.struct);
			constNode.setAdr(saveCharConstPrva);
			constNode.setLevel(level);
			report_info( " (" + deklKonst.getPrvaKonst().getImeProm() +"), Nadjeno Con "+deklKonst.getPrvaKonst().getImeProm()+": "
					+ispisCvora(deklKonst.struct)+", "+constNode.getAdr()
					+", "+constNode.getLevel(),deklKonst);
		}
		else if(deklKonst.struct.equals(Tabela.boolType)) {	
			constNode= Tabela.insert(Obj.Con, deklKonst.getPrvaKonst().getImeProm(), deklKonst.struct);
			constNode.setAdr(saveBoolConstPrva);
			constNode.setLevel(level);
			report_info( " (" + deklKonst.getPrvaKonst().getImeProm() +"), Nadjeno Con "+deklKonst.getPrvaKonst().getImeProm()+": "
					+ispisCvora(deklKonst.struct)+", "+constNode.getAdr()
					+", "+constNode.getLevel(),deklKonst);
		}
		
	
		

		if (listaKonstanti.size() > 0) {
		
			for (int i = 0; i < listaKonstanti.size(); i++) {
					constNode= Tabela.insert(Obj.Con, listaKonstanti.get(i).getName(), listaKonstanti.get(i).getType());
					constNode.setAdr(listaKonstanti.get(i).getAdr());
					constNode.setLevel(listaKonstanti.get(i).getLevel());
					
					report_info( " (" + listaKonstanti.get(i).getName() +"), Nadjeno Con "+listaKonstanti.get(i).getName()+": "
							+ispisCvora(listaKonstanti.get(i).getType())+", "+constNode.getAdr()
							+", "+constNode.getLevel(),deklKonst);
				}
			}
		while(listaKonstanti.size()>0) {
			listaKonstanti.remove(0);	
		}
		
	}
	
	public int saveNumConstPrva;
	public int saveBoolConstPrva;
	public char saveCharConstPrva;
	
	public void visit(PrvaKonst prva) {
		
	
		
		prva.struct = prva.getVrsteKonst().struct;
		if(prva.struct==null) {return;}
		
		if (prva.struct.equals(Tabela.charType)) {
			saveCharConstPrva = saveCharConst;
		} else if (prva.struct.equals(Tabela.intType)) {
			saveNumConstPrva = saveNumConst;		
		} else if (prva.struct.equals(Tabela.boolType)) {
			saveBoolConstPrva = saveBoolConst;
		}

	}
	
	///////////////////////////////////  PROMENLJIVE  //////////////////////////////////////	
	public void visit(ListaPromNizI listaProm) {
		
		if (Tabela.find(listaProm.getProm()) != Tabela.noObj && level == Tabela.find(listaProm.getProm()).getLevel()) {
			report_error(" Greska: dva puta deklarisana promenljiva! " + listaProm.getProm(), listaProm );
			return;
		}
		
		if (uglasteNasao == false) {
			listaPromenljivih.add(new Obj(Obj.Var, listaProm.getProm(), zapamtistruct));			
		} else {						
			Struct s =  new Struct(Struct.Array);
			s.setElementType(zapamtistruct);			
			listaPromenljivih.add(new Obj(Obj.Var, listaProm.getProm(),s));
			}
		
		uglasteNasao = false;
	
	}
	
	
	public void visit(DeklPromI deklProm) {
		
		
		if (Tabela.find(deklProm.getImeProm()) != Tabela.noObj && level == Tabela.find(deklProm.getImeProm()).getLevel()) {
			report_error(" Greska: dva puta deklarisana promenljiva! " + deklProm.getImeProm(), deklProm );
			return;
		}		
		Obj constNode;
			
		if (uglasteNasaoPrvi == false) {
			constNode=Tabela.insert(Obj.Var, deklProm.getImeProm(), deklProm.getType().struct);	
			
			report_info( " (" + deklProm.getImeProm() +"), Nadjeno Var "+deklProm.getImeProm()+": "
					+ispisCvora(deklProm.getType().struct)+", "+constNode.getAdr()
					+", "+constNode.getLevel(),deklProm);
			
		} else {
			Struct s =  new Struct(Struct.Array);
			s.setElementType(deklProm.getType().struct);
			constNode=Tabela.insert(Obj.Var, deklProm.getImeProm(),s);
			
			report_info( " (" + deklProm.getImeProm() +"), Nadjeno Var "+deklProm.getImeProm()+": "
					+ispisCvora(s)+", "+constNode.getAdr()
					+", "+constNode.getLevel(),deklProm);
			
			}
		
		
		if (listaPromenljivih.size() > 0) {
			
			for (int i = 0; i < listaPromenljivih.size(); i++) {			
					constNode= Tabela.insert(Obj.Var, listaPromenljivih.get(i).getName(), listaPromenljivih.get(i).getType());
					
					report_info( " (" + listaPromenljivih.get(i).getName() +"), Nadjeno Var "+listaPromenljivih.get(i).getName()+": "
							+ispisCvora(listaPromenljivih.get(i).getType())+", "+constNode.getAdr()
							+", "+constNode.getLevel(),deklProm);
				}
			}
		
		while(listaPromenljivih.size()>0) {
			listaPromenljivih.remove(0);	
		}
			
		uglasteNasaoPrvi=false;
	}
	


	public void visit(UglasteZagrade type) {
		
		if(type.getParent().getClass()==DeklPromI.class ||
			type.getParent().getClass()==ArgumentiI.class) {	
			uglasteNasaoPrvi=true;
			return;
		}else {
		uglasteNasao=true;
		}
		
	}
	
	////////////////////////////////////// TIP /////////////////////////////////////
	Struct  zapamtistruct= null;
	public void visit(Type type) {
		Obj typeNode = Tabela.find(type.getTypeName());
		
		if (typeNode == Tabela.noObj) {
			report_error(" Greska:ne postoji tip! ", type);
			type.struct = Tabela.noType;
		} else {	
			type.struct = typeNode.getType();
			zapamtistruct = typeNode.getType();
		}	
	}
	
	
//////////////////////////////////////////  FJA ZA ISPIS STRUCTA ////////////////////////////////////////////////////
	
	String ispisCvora(Struct structToVisit) {
		
		StringBuilder msg = new StringBuilder();
	
		switch (structToVisit.getKind()) {
		case Struct.None:
			msg.append("notype");
			break;
		case Struct.Int:
			msg.append("int");
			break;
		case Struct.Bool:
			msg.append("bool");
			break;
		case Struct.Char:
			msg.append("char");
			break;
		case Struct.Array:
			msg.append("Arr of ");
			
			switch (structToVisit.getElemType().getKind()) {
			case Struct.None:
				msg.append("notype");
				break;
			case Struct.Int:
				msg.append("int");
				break;
			case Struct.Bool:
				msg.append("bool");
				break;
			case Struct.Char:
				msg.append("char");
				break;
			case Struct.Class:
				msg.append("Class");
				break;
			}
			break;
		
		}
	
		return msg.toString();
	}
	
/////////////////////////////////////////  METODE   ///////////////////////////////////////////////////////////
	
	public void visit(PovratniVoid methodVoidName) {
		currMethod = Tabela.insert(Obj.Meth, methodVoidName.getImeFje(), Tabela.noType);
		methodVoidName.obj = currMethod;
		Tabela.openScope();
		level++;		
//		report_info(" Obradjuje se funkcija " + methodVoidName.getImeFje(), methodVoidName);
	}
	
	public void visit(PovratniTip povr) {
		currMethod = Tabela.insert(Obj.Meth, povr.getImeFje(), povr.getType().struct);
		povr.obj = currMethod;
		level++;
		Tabela.openScope();
		
		//report_info("Obradjuje se funkcija " + povr.getImeFje(), povr);
	
	}
	
	
	public void visit(NovaVoidMetoda povr) {
		Tabela.chainLocalSymbols(currMethod);
		Tabela.closeScope();
		level--;
		currMethod = null;
	}
	
	public void visit(NovaMetodaI povr) {
		Tabela.chainLocalSymbols(currMethod);
		Tabela.closeScope();
		level--;
		currMethod = null;
	}
	
	public void visit(ListaPromNizTipI listaProm) {
		if (Tabela.find(listaProm.getImeArg()) != Tabela.noObj && level == Tabela.find(listaProm.getImeArg()).getLevel()) {
			report_error(" Greska: dva puta deklarisana promenljiva! " + listaProm.getImeArg(), listaProm );
			return;
		}
		
		if (uglasteNasao == false) {
			listaPromenljivih.add(new Obj(Obj.Var, listaProm.getImeArg(), zapamtistruct));		
			brArg++;
			report_info(" nadjena prom u lst arg ",listaProm);
		} else {						
			Struct s =  new Struct(Struct.Array);
			s.setElementType(zapamtistruct);			
			listaPromenljivih.add(new Obj(Obj.Var, listaProm.getImeArg(),s));
			brArg++;
			}
		
		uglasteNasao = false;
		
	}
		
	public void visit(NemaListaPromNizTip deklProm) {
		
	}
	public void visit(ArgumentiI deklProm) {
	
		if (Tabela.find(deklProm.getImeArg()) != Tabela.noObj && level == Tabela.find(deklProm.getImeArg()).getLevel()) {
			report_error(" Greska: dva puta deklarisana promenljiva! " + deklProm.getImeArg(), deklProm );
			return;
		}
		
		Obj constNode;
		brArg++;
		
		if (uglasteNasaoPrvi == false) {
			constNode=Tabela.insert(Obj.Var, deklProm.getImeArg(), deklProm.getType().struct);	
			
			report_info( " (" + deklProm.getImeArg() +"), Nadjeno Var "+deklProm.getImeArg()+": "
					+ispisCvora(deklProm.getType().struct)+", "+constNode.getAdr()
					+", "+constNode.getLevel(),deklProm);
			
		} else {
			Struct s =  new Struct(Struct.Array);
			s.setElementType(deklProm.getType().struct);
			constNode=Tabela.insert(Obj.Var, deklProm.getImeArg(),s);
			
			report_info( " (" + deklProm.getImeArg() +"), Nadjeno Var "+deklProm.getImeArg()+": "
					+ispisCvora(s)+", "+constNode.getAdr()
					+", "+constNode.getLevel(),deklProm);
			
			}
		
		uglasteNasaoPrvi=false;
		
		currMethod.setLevel(brArg);
		brArg=0;
		if (listaPromenljivih.size() > 0) {			
			for (int i = 0; i < listaPromenljivih.size(); i++) {
					constNode= Tabela.insert(Obj.Var, listaPromenljivih.get(i).getName(), listaPromenljivih.get(i).getType());
					
					report_info( " (" + listaPromenljivih.get(i).getName() +"), Nadjeno Var "+listaPromenljivih.get(i).getName()+": "
							+ispisCvora(listaPromenljivih.get(i).getType())+", "+constNode.getAdr()
							+", "+constNode.getLevel(),deklProm);
				}
			}
		
		while(listaPromenljivih.size()>0) {
			listaPromenljivih.remove(0);	
		}
				
	}

	
/////////////////////////////////////////  DESIGNATOR STATEMENT  ///////////////////////////////////////////////////////////\\
	
	public void visit(DesigDodelaVred dodela) {
		
		if(dodela.getDesignator().obj.getKind() != 1 ) {
			report_error(" Greska: Sa leve strane dodele nije promenljiva ili niz! ", dodela);
			
		}
		else if (newBio == 1 && dodela.getDesignator().obj.getType().getKind() != 3) {
			report_error(" Greska: niz se ne nalazi sa leve strane!",	dodela);
			
		}
		else if(dodela.getIzraz().struct==null) {
			//report_error(" Greska: Tipovi podataka prilikom dodele nisu kompatibilni! ",dodela);
			return;}
		else if (!(dodela.getIzraz().struct.assignableTo(dodela.getDesignator().obj.getType())
				|| (newBio == 1 && dodela.getDesignator().obj.getType().getKind() == 3
				&& dodela.getIzraz().struct == dodela.getDesignator().obj.getType().getElemType()))){
			
			report_error(" Greska: Tipovi podataka prilikom dodele nisu kompatibilni! ",dodela);

		}
			
		newBio = 0;
	}
	
	
	public void visit(DesigPlusPlus dodela) {
		if (!(dodela.getDesignator().obj.getKind() == 1)) {
			report_error(" Greska : Sa leve strane dodele kod operatora ++ se mora naci promenljiva ili element niza! ", dodela);
		} else if (!dodela.getDesignator().obj.getType().equals(Tabela.intType)) {
			report_error(" Greska : Pre operatora ++ se mora nalaziti tip int ", dodela);
		}
		
	}

	
	public void visit(DesigMinusMinus dodela) {	
		if (!(dodela.getDesignator().obj.getKind() == 1)) {
			report_error(" Greska : Sa leve strane dodele kod operatora -- se mora naci promenljiva ili element niza! ", dodela);
		} else if (!dodela.getDesignator().obj.getType().equals(Tabela.intType)) {
			report_error(" Greska : Pre operatora -- se mora nalaziti tip int ", dodela);
		}
		
	}

	
	public void visit(DesigFjaSaArg fja) {
		if (fja.getDesignator().obj.getKind() != 3) {
			report_error("Greska :Zadato ime nije globalna funkcija programa! ", fja);
		}
		
	}
			
		public void visit(FactorNumKonst num) {
			num.struct = Tabela.intType;
		}

		public void visit(FactorCharKonst num) {
			num.struct = Tabela.charType;
		}

		public void visit(FactorBoolKonst num) {
			num.struct = Tabela.boolType;
		}
		
		
		public void visit(FactorNovSimbol novi) {
			newBio = 1;		
			if (novi.getIzraz().struct.equals(Tabela.intType)) {
				novi.struct = novi.getType().struct;	
			}else {
			report_error("Greska: tip izraza kod new mora biti int! ", novi);
			}
			
		}
		
		public void visit(FactorDesignator fd) {
			fd.struct = fd.getDesignator().obj.getType();		
		}
		
		public void visit(ActParameters par) {
			params.add(par.getIzraz().struct);		
		}
		
		public void visit(ActPararameter par) {
			params.add(par.getIzraz().struct);
		}	
		
		public void visit(FactorDesignatorPars act) {
			
			if(act.getDesignator().obj.getKind()!=3) {
				report_error(" Designator mora biti globalna funkcija glavnog programa",act);	
			}
					
			if (fje.size() > 0) {
				
				//report_info("FUNKCIJE SIZE "+funkcije.size(),null);
				
				Obj fja = fje.remove(fje.size() - 1);
				
				act.struct=fja.getType();
				
				report_info(" Funkcija " + fja.getName()+" sa "+fja.getLevel() +" parametara, a prosledjeno je " + params.size() ,act);
				
				if (fja.getName().equals("ord")) {
					if (params.get(params.size() - 1).getKind() != 2) {
						report_error("Parametar funkcije odr mora biti tipa char ", null);
						return;
					}

				}
				if (fja.getName().equals("chr")) {
					if (params.get(params.size() - 1).getKind() != 1) {
						report_error("Parametar funkcije chr mora biti tipa int ", null);
						return;
					}

				}
				if (fja.getName().equals("len")) {
					if (params.get(params.size() - 1).getKind() != 3) {
						report_error("Parametar funkcije len mora biti tipa array ", null);
						return;
					}

				}
				
				
				if(params.size() >0 && fja.getLevel()==0) {
					report_error(" Funkcija " + fja.getName() + " nema parametre! Greska ", act);
				} 
				else if (params.size()<fja.getLevel()) {
					report_error(" Funkciji " + fja.getName() + " nisu prosledjeni svi parametri! Greska ", act);
				} 
				else if (params.size()>fja.getLevel()) {
					report_error(" Funkciji " + fja.getName() + " je prosledjeno visak parametara! Greska ", act);
				}
				else if(params.size()==fja.getLevel()) {

					if ( params.size() > 0) {
				
						Iterator<Obj> parametri = fja.getLocalSymbols().iterator();
						
						int i = params.size();
					
							while (i != 0 && parametri.hasNext()) {

								Obj param = parametri.next();
					
								if (param.getType().getKind() != params.get(params.size() - i).getKind()) {
									report_error("Parametar " + (params.size() - i + 1) + "  koji se prosledjuje funkciji "
											+ fja.getName() + " je pogresnog tipa ", act);
								}

								i--;
							}
							
							
							
							
					}
				}	
			}
		
			while( params.size()>0) {
				params.remove(0);
			}
		}
			
////////////////////////////////////  [,a,]=niz /////////////////////////////////////////
		
		
		List<Obj> listaDesignatora = new ArrayList<>();
		public void visit(DesignatorStatementD statement) {
			
			Obj objDes = statement.getPreZavrsni().getZavrsni().getDesignator().obj;
			
			if(!(objDes.getKind() == 1 && objDes.getType().getKind() == 3 )){
				report_error(" Greska: sa desne strane dodele mora biti niz", statement);
			}

			for(int i=0;i<listaDesignatora.size();i++) {
				
				if(!(listaDesignatora.get(i).getType().compatibleWith(objDes.getType().getElemType()))) {
					report_error(" Greska kod prom "+listaDesignatora.get(i).getName()+ " tipovi nisu kompatibilni!", statement);
				}
			}
			listaDesignatora.clear();
			
		}
			

		
		public void visit(MozdaDesignatorI mozDes) {
			//DA LI TREBA BITI STRUCT ILI OBJ ILI TAKO NES
			if(mozDes.getDesignator().obj.getKind() != 1 ) {
				report_error(" Greska: Sa leve strane dodele nije promenljiva ili niz! ", mozDes);
				
			}
			listaDesignatora.add(mozDes.getDesignator().obj);	
		}	
		
///////////////////////////////////////// IZRAZ ///////////////////////////////////////////////////////////
	
		public void visit(DesignatorIzraz novi) {
			
			Obj obj = Tabela.find(novi.getImeNiza().getDesName());
			
			if (obj == Tabela.noObj) {
				report_error(" Greska: " + novi.getImeNiza().getDesName() + " nije deklarisano! ", novi);
			} 
			else if (obj.getType().getKind() == 3) {
					
					novi.obj = new Obj(obj.getKind(), obj.getName(), obj.getType().getElemType(), obj.getAdr(),
							obj.getLevel());
					
					report_info( " (" + novi.getImeNiza().getDesName() +"), Nadjeno Var "+novi.getImeNiza().getDesName()+": "
							+ispisCvora(obj.getType())+", "+ novi.obj.getAdr()
							+", "+novi.obj.getLevel(),  novi);

			} else {
				report_error(" Greska: " +  novi.getImeNiza().getDesName() + " nije niz! ", novi);
			}
			
			}
		
		
		
		
		public void visit(DesignatorIdent designator) {
			
			
			Obj obj = Tabela.find(designator.getDesName());

			if (obj == Tabela.noObj) {
				report_error(" Greska: " + designator.getDesName()
						+ " nije deklarisano! ", designator);
			} else {

				designator.obj = obj;
				
				if (obj.getKind() == Obj.Meth) {
					fje.add(obj);
				}
				
				if(obj.getKind() == Obj.Meth) {
					
					report_info( " (" + designator.getDesName() +"), Nadjeno Meth "+designator.getDesName()+": "
							+ispisCvora(designator.obj.getType())+", "+ designator.obj.getAdr()
							+", "+designator.obj.getLevel(),  designator);
				}else if(obj.getKind() == Obj.Var)  {
					
					report_info( " (" + designator.getDesName() +"), Nadjeno Var "+designator.getDesName()+": "
							+ispisCvora(designator.obj.getType())+", "+ designator.obj.getAdr()
							+", "+designator.obj.getLevel(),  designator);
				}else if(obj.getKind() == Obj.Con)  {
					
					report_info( " (" + designator.getDesName() +"), Nadjeno Con "+designator.getDesName()+": "
							+ispisCvora(designator.obj.getType())+", "+ designator.obj.getAdr()
							+", "+designator.obj.getLevel(),  designator);
				}
				
				
				
			}

		}

		
		
		public void visit(IzrazBezSub izraz) {
			izraz.struct = izraz.getTerm().struct;
	
			if (izraz.struct != null) {
				
				if(izraz.getListaAddopTerm().obj==null) {return;}
				
				else if (izraz.getListaAddopTerm().obj.equals(Tabela.noObj)) {
					izraz.struct = izraz.getTerm().struct;
					
				} else {
					
					if (izraz.getTerm().struct.equals(Tabela.intType) || (izraz.getTerm().struct.getKind() == 3
							&& izraz.getTerm().struct.getElemType().equals(Tabela.intType))) {
						
						izraz.struct = izraz.getTerm().struct;
						
					} else
						report_error("Greska na liniji " + izraz.getLine() + " : Tip mora biti int! ", null);
				
				}
				
				
			}
			
		}
		
		
		
		public void visit(IzrazSaSub izraz) {
			
			izraz.struct = izraz.getTerm().struct;

			if (izraz.getTerm().struct.equals(Tabela.intType) || (izraz.getTerm().struct.getKind() == 3
					&& izraz.getTerm().struct.getElemType().equals(Tabela.intType))) {
				
				izraz.struct = Tabela.intType;
			} else
				report_error(" Greska: Tip nakon minusa mora biti int ", izraz);
		
		}
		
		
		public void visit(ListaAddopTermI lista) {

			if (!(lista.getTerm().struct.equals(Tabela.intType))) {
				report_error(" Greska: Tip prilikom operacija sabiranja i oduzimanja mora biti int! ", lista);
			}else {
				lista.obj = new Obj(Obj.Var, " ", Tabela.intType);
			}
						
		}
		
	public void visit(NemaListaAddopTermI lista) {
		lista.obj = Tabela.noObj;
	}
		
		
		
	public void visit(ListaMulopFactorI mul) {	
		
		if (!mul.getFactor().struct.equals(Tabela.intType)) {
			report_error(" Greska: Tip kojim mnozi nije int ", mul);
		}else {	
			mul.obj = new Obj(Obj.Var, "", Tabela.intType);
		}	
		
	}
	
	
	public void visit(NemaListaMulopFactor mul) {
		mul.obj = Tabela.noObj;
	}		

	public void visit(TermI term) {
		if(term.getListaMulopFactor().obj==null) {
			return;
		}
		else
		if (term.getListaMulopFactor().obj.equals(Tabela.noObj)) {
			
			term.struct = term.getFactor().struct;
			
		} else {
			if (term.getFactor().struct.getKind() == 3 && term.getFactor().struct.getElemType().equals(Tabela.intType)) {
				
				term.struct = term.getFactor().struct;
			} else if (term.getFactor().struct.equals(Tabela.intType)) {
				
				term.struct = term.getFactor().struct;
			} else if (term.getFactor().struct.getKind() == 3
					&& !term.getFactor().struct.getElemType().equals(Tabela.intType)) {
				
				report_error( " Greska : factor za operazije mnozenja i deljenja mora biti tipa int "
						, term);
			}

		}

	}
		
		
//////////////////////////////////////// CONDITION //////////////////////////
	String relOp="";
	public void visit(RelopJednakoJednako lt) {
		relOp="==";
	}
	public void visit(RelopNijeJednako lt) {
		relOp="!=";
	}
	public void visit(RelopVece lt) {
		relOp=">";
	}
	public void visit(RelopVeceJednako lt) {
		relOp=">=";
	}
	public void visit(RelopManje lt) {
		relOp="<";
	}
	public void visit(RelopManjeJednako lt) {
		relOp="<=";
	}
	
	
	
		
	public void visit(ListaRelopI lt) {
		
		lt.struct = lt.getIzraz().struct;
		
	}
	
	public void visit(CondFactI cf) {
		Struct te = cf.getListaRelop().struct;
		Struct t = cf.getIzraz().struct;

		
		if(te == null) {
			if(t.equals(Tabela.boolType)) {	
				cf.struct = Tabela.boolType;
			}
			else {
				report_error(" Greska: Tip uslova mora biti boolean! ", cf);
				}
			
		}else {
			if(te.getKind()==3 && t.getKind()==3 ) {
				if(!(relOp.equals("==")||relOp.equals("!="))){
					report_error(" Greska: uz promenljive tipa niz mogu se koristiti samo ==  !=  ", cf);
				}	
			}
			if((t.equals(Tabela.intType) || t.equals(Tabela.charType)) && te.equals(t)) {
				cf.struct = Tabela.boolType;
			}else {
				report_error(" Greska: Tip koji se porede se ne podudaraju! ", cf);
			}

		}
			
	}
	
	//////////////////////////////// STATEMENT //////////////////////////
	public void visit(PocetakWhileI pocWhile) {
		whileBreakLst.add(1);
		whileContinueLst.add(1);
	}
	
	public void visit(StatementWhile statemWhile) {
		whileBreakLst.remove(whileBreakLst.size() - 1);
		whileContinueLst.remove(whileContinueLst.size() - 1);
	}
	
	public void visit(StatementBreak sb) {
		if (whileBreakLst.size() == 0) {
			report_error("BREAK stoji van WHILE petlje " + sb.getLine(), null);
		}else {
			report_info(" Nadjen ispravan BREAK! " , sb);
		}
	}
		
	public void visit(StatementContinue sc) {
		if (whileContinueLst.size() == 0) {
			report_error("CONTINUE stoji van WHILE petlje " + sc.getLine(), null);
		}else {
			report_info(" Nadjen ispravan CONTINUE! " , sc);
		}
	}
		
	public void visit(StatementRead sread) {
		
		if (sread.getDesignator().obj.getKind() == Obj.Var) {

			if (sread.getDesignator().obj.getType() == Tabela.intType ||
				sread.getDesignator().obj.getType() == Tabela.charType ||
				sread.getDesignator().obj.getType() == Tabela.boolType)
				report_info(" Nadjen ispravan READ " , sread);
			
			else {
				report_error(" Nadjen ispravan READ tip moze biti int/char/bool", sread);
			}
			
		} 
		else if (sread.getDesignator().obj.getKind() == Obj.Meth) {
			report_error("Zabranjeno je ucitavati u funkciju!", sread);
		} 
		else if (sread.getDesignator().obj.getKind() == Obj.Con) {
			report_error("Zabranjeno je ucitavati u konstantu!", sread);
		}else {
			report_error("Zabranjeno je ucitavanje!", sread);
		}

	}
		
	public void visit(StatementPrint sprint) {
	
		if (sprint.getIzraz().struct == Tabela.intType || 
			sprint.getIzraz().struct == Tabela.charType || 
			sprint.getIzraz().struct == Tabela.boolType) {
			
			 report_info(" Nadjen ispravan PRINT!" , sprint);
		} else {
			report_error(" Nadjen neispravan PRINT!", sprint);
		}

	}
		
	public void visit(StatementReturnIzraz sr) {
		
		if(currMethod == null) {
			report_error(" Nadjen neispravan RETURN, ne sme se nalaziti izvan metode ", sr);
		}else if (sr.getIzraz().struct.equals(currMethod.getType())) {
			report_info(" Nadjen ispravan RETURN! " , sr);
		
		} else {
			report_error(" Nadjen neispravan RETURN, tipovi se ne poklapaju ", sr);
		
		}
	}
		
	
	public void visit(StatementReturn sr) {
		if(currMethod == null) {
			report_error(" Nadjen neispravan RETURN, ne sme se nalaziti izvan metode ", sr);
		}else if (currMethod.getType() == Tabela.noType){
			report_info(" Nadjen ispravan RETURN! " , sr);			
		}else {
			report_error(" Nadjen neispravan RETURN, tekuca metoda mora biti void! ", sr);		
		}
		
		
	}
	
	Obj foreachObj;
	public void visit(DesignatorForI sr) {	
		foreachObj = sr.getDesignator().obj;
		if(!(foreachObj.getKind() == 1 && foreachObj.getType().getKind() == 3 )){
			report_error(" Greska: sa desne strane dodele mora biti niz", sr);
		}
	}
	
	
	public void visit(StatementForeach sr) {
	Obj ident=Tabela.find(sr.getForIdent().getIter());
	
		if(!(ident.getType().compatibleWith(foreachObj.getType().getElemType()))) {
			report_error(" Greska mora biti niz sa leve strane!", sr);
		}
		
	}
	
	
	public void visit(ImeNiza elem) {
		elem.obj = Tabela.find(elem.getDesName());
	}
	
	
	public void visit(ForIdent sr) {
		Obj obj = Tabela.find(sr.getIter());
		
		report_info( " (" + sr.getIter() +"), Nadjeno Var "+sr.getIter()+": "
				+ispisCvora(obj.getType())+", "+ obj.getAdr()
				+", "+obj.getLevel(),  sr);
		
		sr.obj = Tabela.find(sr.getIter());
	
	}
	
	public void visit(ListaMetodDeklaracijaI sr) {
		Obj obj = Tabela.find("main");
		if(obj== Tabela.noObj) {
			report_error("Greska: Nema main-a! ",null);
			
		}else {
			if(obj.getLevel()!=0) {
				report_error("Greska: Main ne sme imati argumente! ",null);
			}else {
			//report_info("IMA MAINA nema arg ",null);
			}
		}
		
	}
	
	public void visit(FactorIzraz izr) {
		izr.struct = izr.getIzraz().struct;
	}

	
}
