package rs.ac.bg.etf.pp1;

import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


import rs.ac.bg.etf.pp1.ast.*;


public class CodeGenerator extends VisitorAdaptor {
//izraz.getFactor().traverseBottomUp(this) ;
	public ArrayList<Integer> saveList = new ArrayList<Integer>();
	public ArrayList<Integer> haveList = new ArrayList<Integer>();
	

	public ArrayList<Integer> whileStart = new ArrayList<Integer>();
	
	List<Obj> listaProm = new ArrayList<>();
	List<Integer> listaDes=new ArrayList<>();
	//0 obicna 1 niz 2 noObj
	
	public ArrayList<Integer> condPC = new ArrayList<Integer>();
	public ArrayList<Integer> lastCondPC = new ArrayList<Integer>();
	
	public ArrayList<Integer> izaElse=new ArrayList<Integer>();
	
	public ArrayList<Integer> breakFix = new ArrayList<Integer>();
	public ArrayList<Integer> breakLocation = new ArrayList<Integer>();
	

	
	

	private int mainPc;
	
	public int getMainPc() {
		return mainPc;
	}
	
	boolean definisanMain=true;
	public boolean postojiMain() {
		return definisanMain;
	}
	
	public void visit(ImePrograma pn) {
		
		// ord
		Obj objekat = Tab.find("ord");
		objekat.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1);
		Code.put(1);
		Code.loadConst(0);
		Code.put(Code.load_n);
		Code.put(Code.add);
		Code.put(Code.exit);
		Code.put(Code.return_);
		
		// chr
		Obj objekat2 = Tab.find("chr");
		objekat2.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1);
		Code.put(1);
		Code.loadConst(0);
		Code.put(Code.load_n);
		Code.put(Code.add);
		Code.put(Code.exit);
		Code.put(Code.return_);
		
		// len
		Obj objekat3 = Tab.find("len");
		objekat3.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1);
		Code.put(1);
		
		// Code.loadConst(0);
		Code.put(Code.load_n);
		Code.put(Code.arraylength);
		Code.put(Code.exit);
		Code.put(Code.return_);
	
	}
	
	
	boolean zadataJeSirinPrinta=false;
	int zadataSirinaPrinta=0;
	
	public void visit(ListaNumKonstantiI lst) {
		zadataJeSirinPrinta = true;
		zadataSirinaPrinta = lst.getSirina();
	}
	
	public void visit(StatementPrint printStmt) {
		
		if (printStmt.getIzraz().struct == Tab.charType) {
			
			if (zadataJeSirinPrinta == false) {
				Code.loadConst(1);
			} else {
				Code.loadConst(zadataSirinaPrinta);
				zadataJeSirinPrinta = false;
				zadataSirinaPrinta = 0;
			}
			Code.put(Code.bprint);
			
		} else {	
			if (zadataJeSirinPrinta == false) {
				Code.loadConst(5);
			} else {
				Code.loadConst(zadataSirinaPrinta);
				zadataJeSirinPrinta = false;
				zadataSirinaPrinta = 0;
			}
			Code.put(Code.print);
			
		}

	}
	
	
	
	public void visit(FactorNumKonst cd) {	
		Obj con = Tabela.insert(Obj.Con, "$", cd.struct);
		con.setLevel(0);
		con.setAdr(cd.getBroj());
		Code.load(con);
	}

	public void visit(FactorCharKonst fcc) {
		Obj con = Tabela.insert(Obj.Con, "$", fcc.struct);
		con.setLevel(0);
		con.setAdr(fcc.getKarakter().charAt(1));
		Code.load(con);
	}

	public void visit(FactorBoolKonst fbc) {
		Obj con = Tabela.insert(Obj.Con, "$", fbc.struct);
		con.setLevel(0);
		if (fbc.getVrednost().equals("true")) {
			con.setAdr(1);
		} else {
			con.setAdr(0);
		}
		Code.load(con);
	}
	
	
	public void visit(NovaMetodaI met) {
		povratakIzFje();
	}
	
	public void visit(NovaVoidMetoda met) {
		povratakIzFje();
	}

	public void visit(StatementReturnIzraz returnf) {
		povratakIzFje();
	}
	
	public void visit(StatementReturn returnf) {
		povratakIzFje();
	}
	
	public void povratakIzFje() {
		Code.put(Code.exit);
		Code.put(Code.return_);

	}
	
	int adrrrr;
	public void visit(PovratniTip ime) {
		//ulazak u metodu, prebrojavanje argumenata i lokalnih promenljivih
		ime.obj.setAdr(Code.pc);
		
		Code.put(Code.enter);
		Code.put(ime.obj.getLevel());
		Code.put(ime.obj.getLocalSymbols().size());
		
		
		adrrrr=ime.obj.getLocalSymbols().size()+1;

	}

	public void visit(PovratniVoid ime) {
		if ("main".equalsIgnoreCase(ime.getImeFje())) {
			definisanMain = true;
			mainPc = Code.pc;
		}

		ime.obj.setAdr(Code.pc);


		Code.put(Code.enter);
		Code.put(ime.obj.getLevel());
		Code.put(ime.obj.getLocalSymbols().size());
		
		
		adrrrr=ime.obj.getLocalSymbols().size()+1;
	}

	int saberiOduzmi;
	List<Integer> sabOduz=new ArrayList<>();
	//0 saberi 1 oduzmi
	
	public void visit(AddOperatoriPlus addOp) {
		
		saberiOduzmi=0;
		sabOduz.add(saberiOduzmi);
	}
	
	public void visit(AddOperatoriMinus addOp) {
		
		saberiOduzmi=1;
		sabOduz.add(saberiOduzmi);
	}
	
	
	public void visit(ListaAddopTermI addOp) {
		
		if (sabOduz.get(sabOduz.size()-1)==0)
		{
			//System.out.println("ADD U ADDOP");
			Code.put(Code.add);
		
		}else {
			Code.put(Code.sub);
		}
		
		sabOduz.remove(sabOduz.size()-1);
	}
	
	public void visit(IzdvojiMinusI term) {
		Obj con = Tabela.insert(Obj.Con, "$", Tabela.intType);
		con.setLevel(0);
		con.setAdr(0);
		Code.load(con);
	}
	
	public void visit(TermI term) {
		if (term.getParent().getClass() == IzrazSaSub.class) {
			Code.put(Code.sub);
		}
	}

	
	
	int putPodProc;
	List<Integer> putPodProcList=new ArrayList<>();
	//0 mul  1 div  2 rem
 	public void visit(MulOperatoriPuta mulOp) {
		putPodProc=0;
		putPodProcList.add(putPodProc);
	}
	
	public void visit(MulOperatoriPodeljeno divOp) {
		putPodProc=1;
		putPodProcList.add(putPodProc);
	}
	
	public void visit(MulOperatoriProcenat remOp) {
		putPodProc=2;
		putPodProcList.add(putPodProc);
	}
	
	public void visit(ListaMulopFactorI remOp) {		
		if (putPodProcList.get(putPodProcList.size()-1)==0) {
			Code.put(Code.mul);
			}
		else if (putPodProc==1) {
			Code.put(Code.div);
		}
		else { 
			Code.put(Code.rem);
		}
		
		putPodProcList.remove(putPodProcList.size()-1);
	}
	
	
	
	
	public void visit(DesigDodelaVred dodela) {
		
		if (dodela.getDesignator().getClass() == DesignatorIzraz.class) {
			
			if (dodela.getDesignator().obj.getType() == Tab.charType
					|| (dodela.getDesignator().obj.getType().getKind() == 3
							&& dodela.getDesignator().obj.getType().getElemType() == Tab.charType)) {
				Code.put(Code.bastore);
			} else {
				Code.put(Code.astore);
			}
			
		} else if(dodela.getDesignator().getClass() == DesignatorIdent.class) {
	
		Code.store(dodela.getDesignator().obj);
		}
		
	}

	Logger log = Logger.getLogger(getClass());
	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder();
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" Pretraga na ").append(line);
		msg.append(message);
		log.info(msg.toString());
	}
	
	
	public void nadjiMax( ) {
		int ucit=0;
		int adrVanUcit=0;
		int drugoPop=0;
		int kraj=0;
		/////////////////////// nadji max
		Code.load(niz); 
		Code.put(Code.arraylength);
		
		ucit=Code.pc;
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.put(Code.dup);
		
		//radi se 3 puta, za 1 manje od duzine niza
		Code.loadConst(1);
		
		Code.put(Code.jcc+Code.lt);
		kraj=(Code.pc);
		Code.put2(0);
		
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		
		//LOGIKA ->5 7 3 5 9 || posl pretposl INDEX drugi prvi
		////////////////////////////////////////
		Code.put(Code.dup2);
		drugoPop=(Code.pc+1);
		Code.putFalseJump(Code.le, 0);
		
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.fixup(drugoPop);
		Code.put(Code.pop);
		
		//-> 5 7 3 9  ||posl pretposl   INDEX IZGLASANI
		////////////////////////////////////////
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		//-> 5 7 3 9  ||posl pretposl  IZGLASANI INDEX
		//kada kreces u novu iteraciju index mora da bude na vrhu niza
		Code.putJump(ucit);
		
		Code.fixup(kraj);
		Code.put(Code.pop);
		
	}
	public void fjaMax( ) {
		 // | (FindMax)   MAXSIMB Designator TACZAR
		 
		int ucit=0;
		int adrVanUcit=0;
		int drugoPop=0;
		int kraj=0;
		
		//int brElem=0;
	//	int i=-1;
		//public void visit(FindMax izraz) {
			
			//Obj niz= izraz.getDesignator().obj;
			
			
			/* cisto ucitavanje elemenata niza 
			 * u zavisnosti od toga da li smo za ident zabranili da se ucita ako je get parent find max
			 * treba dodati na pocetku Code.load(niz); ako smo zabranili !!!!!!!!!!!*/
			Code.load(niz); 
			Code.put(Code.arraylength);
			
			
			ucit=Code.pc;
			Code.loadConst(1);
			Code.put(Code.sub);
			
			Code.put(Code.dup);
			Code.loadConst(0);
			
			
			Code.put(Code.jcc+Code.lt);
			adrVanUcit=(Code.pc);
			Code.put2(0);
			
			
			Code.put(Code.dup2);
			Code.put(Code.aload);
			
			Code.put(Code.dup_x2);
			Code.put(Code.pop);
			
			Code.putJump(ucit);
			
			Code.fixup(adrVanUcit);
			
			Code.put(Code.pop);
			Code.put(Code.pop);
					
		
	//}
		
	}
	
	public void visit(FactorNovSimbol novi) {
		if (novi.getType().struct == Tab.intType || novi.getType().struct == Tabela.boolType) {
			Code.put(Code.newarray);
			Code.put(1);
		} else if (novi.getType().struct == Tab.charType) {
			Code.put(Code.newarray);
			Code.put(0);

		}
	}
	
	public void visit(DesignatorIdent desig) {
		
		if((desig.getParent().getClass()!=DesigDodelaVred.class) 
				&& (desig.getParent().getClass()!=FactorDesignatorPars.class)
				
				&& (desig.getParent().getClass()!=StatementRead.class)
				
				&&(desig.getParent().getClass()!=MozdaDesignatorI.class)
				&&(desig.getParent().getClass()!=DesignatorStatementD.class)
				&&(desig.getParent().getClass()!=Zavrsni.class	)
				&&(desig.getParent().getClass()!=DesignatorForI.class	)
				) {
			
			Code.load(desig.obj);

		}
		
		if(desig.getParent().getClass()==MozdaDesignatorI.class) {
			listaDes.add(0);			
			listaProm.add(desig.obj);
		}
		

	}

	
	
	public void visit(DesignatorIzraz desig) {
		
		if((desig.getParent().getClass()!=DesigDodelaVred.class)&&
				(desig.getParent().getClass()!=StatementRead.class)&&
				(desig.getParent().getClass()!=StatementForeach.class)&&
				
				(desig.getParent().getClass()!=DesignatorStatementD.class)&&
				(desig.getParent().getClass()!=MozdaDesignatorI.class)
				
				) {
		
			if (desig.obj.getType() == Tab.charType ) {
			
				Code.put(Code.baload);
			}else {
				
				Code.put(Code.aload);
			}	
		}else if(desig.getParent().getClass()==StatementForeach.class) {
			
			
			
		}
		
		
		if(desig.getParent().getClass()==MozdaDesignatorI.class) {
			listaDes.add(1);		
			listaProm.add(desig.obj);
				}
		
	}
	
	public void visit(ImeNiza desig) {
	
		//	report_info("Ime nizaaaa  "+desig.getDesName()+desig.obj.getKind()+desig.obj.getLevel(),desig);
		Code.load(desig.obj);
	}
	
	

	public void visit(FactorDesignatorPars funcCall) {		
		
		int offset = funcCall.getDesignator().obj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		
	}
		
	
	////////////////////////////////////[,a,]=niz /////////////////////////////////////////
	Obj niz;
	public void visit(NemaMozdaDesignator md) {		
		listaProm.add(Tab.noObj);
		listaDes.add(2);
	}
	
	
	public void visit(Zavrsni md) {
		niz=md.getDesignator().obj;
	
	}
	
	
	int preskociTrap=0;
	
	public void visit(DesignatorStatementD desigSTMT) {
	
	Obj obj=niz;
	
	int brElemSaLeve=listaDes.size();
	Code.loadConst(brElemSaLeve);
	Code.load(obj);
	Code.put(Code.arraylength);
	
	Code.put(Code.jcc+Code.le);
	preskociTrap=Code.pc;
	Code.put2(0);
	
	Code.put(Code.trap);
	Code.put(1);
	
	Code.fixup(preskociTrap);
	for(Integer i=listaDes.size()-1;i>=0;i--) {
		if(listaDes.get(i)==0) {
			//ucita elem niza sa desne strane
			
			Code.load(obj);
			
			//indeks
			Obj con = Tabela.insert(Obj.Con, "$", Tab.intType);
			con.setLevel(0);
			con.setAdr(i);
			Code.load(con);
			
			
			//Code.loadConst(i);
			Code.put(Code.aload);
			
			
			Code.store(listaProm.get(i));
			
		
		}else if(listaDes.get(i)==1) {
		
		
			//ucita elem niza sa desne strane
			Code.load(obj);
			
			//indeks
			Obj con = Tabela.insert(Obj.Con, "$", Tab.intType);
			con.setLevel(0);
			con.setAdr(i);
			Code.load(con);
			
			Code.put(Code.aload);	
			Code.put(Code.astore);
		
		
		
		}else if(listaDes.get(i)==2) {
		
		//nista se ne radi
		}
		
		
	}	
	
		listaProm.clear();
		listaDes.clear();
		
	
	}

///////////////////////////////////////// if else /////////////////////////////////////////////////////////////////////////////
	int relOp=0;
	public void visit(RelopJednakoJednako jj) {
		relOp=0;
	}

	public void visit(RelopNijeJednako nj) {
		relOp=1;
	}
	
	public void visit(RelopManje m) {
		relOp=2;
	}
	
	public void visit(RelopManjeJednako mj) {
		relOp=3;
	}
	
	public void visit(RelopVece v) {
		relOp=4;
	}
	
	public void visit(RelopVeceJednako vj) {
		relOp=5;
	}
	

	int prviUslov=1;
	
	public void visit(ListaRelopI listaRelop) {
		condPC.add(Code.pc + 1);
		Code.putFalseJump(relOp, 0);
	}
	
	public void visit(NemaListaRelop nema) {
		//if(nesto) -> jmp ako nesto nije jednako 0
		Code.loadConst(0);
		condPC.add(Code.pc + 1);
		Code.putFalseJump(Code.ne, 0);
		
		/*
		Code.loadConst(1);
		pcuslovi.add(Code.pc + 1);
		Code.putFalseJump(Code.eq, 0);
		*/
	}
	
	
	
	public void visit(UslovI uslov) {
		//report_info("USLOV "+(Code.pc + 1),null);
		
		prviUslov=1;
	
		//ako je uslov ispunjen  poslednji u nizu &&, ili || skace na then granu	
		for (int i = 0; i < lastCondPC.size(); i++) {
			Code.buf[lastCondPC.get(i) - 1] =(byte) (Code.inverse[ Code.buf[ lastCondPC.get(i) - 1 ] - 43] + 43);	
			Code.fixup(lastCondPC.get(i));
		}
		
		lastCondPC.clear();
	}

	
	
	public void visit(CondTermI contTERM) {
		
		//report_info("TERM "+(Code.pc + 1),null);
		
		if(prviUslov==1) {
			prviUslov=0;
			
			//report_info(" PRVI USLOV KOME STA PRIPADA "+saveList.size(),null);
			/*for (int i = 0; i <haveList.size() ; i++) {
				report_info(haveList.get(i)+" ",null);
			}*/
		//	report_info("prvo udje u  PRVI uslov za ispitivanje",null);
		}
		else {
	
		//	report_info(" NIJE P KOME STA PRIPADA ",null);
			/*for (int i = 0; i <haveList.size() ; i++) {
				report_info(haveList.get(i)+" ",null);
			}*/
				
			if (haveList.size() == 1) {
				
				saveList.clear();	
				haveList.clear();
				
			}else {
				//VIDI POSLE
				//komestapripada.get-> od poslednjeg nivoa
			//	report_info(" POSLEDNJA LISTA "+saveList.size(),null);
				
				int prvi=haveList.get(haveList.size() - 2);
				int posl=haveList.get(haveList.size() - 1);
				int broj=posl-prvi;
				while(broj>0) {
					saveList.remove(saveList.size()-1);
					broj--;
				}
				
				
			//	report_info(" POSLEDNJA LISTA izbaceno "+saveList.size(),null);
			
				//izbaci poslednji nivo
				haveList.remove(haveList.size()-1);
			}
			
			
		}
		
		
		for (int i = 0; i < condPC.size(); i++) {
			//FIXUP zavrsen niz && uslova, ako neki od njih nije ispunjen prelazi se na ovo * mesto 
			//npr if( 9<9 && 0<0)*  
			//npr if( 9<9 && 0<0 ||* 0<9 && 0>8 || 5<=8) ispituje se odmah sledeci uslov ako prvi nije ispunjen
			Code.fixup(condPC.get(i));
			saveList.add(condPC.get(i));
		}
		
		lastCondPC.add(condPC.get(condPC.size() - 1));
		condPC.clear();
		
		haveList.add(saveList.size());
		
	}
	


	public void visit(PocetakElseI elsePoc) {
		// ELSE STATEMENT
		//ELSE pocetak == kraj THEN grane if a  if(usl){ ...}*else{ } , umesto * se umetne jmp da bi se preskocio else
		
		//samo se ovo razlikuje u odnosu na if
		izaElse.add(Code.pc + 1);	
		Code.putJump(0);
				
		//report_info("U ELSE "+haveList.size(),null);

		if (haveList.size() == 1) {
			
			int posl=haveList.get(haveList.size() - 1);
			
			for (int i =  0; i < posl; i++) {			
				Code.fixup(saveList.get(i));
			}
			
			Code.buf[saveList.get(posl - 1) - 1] = (byte) (Code.inverse[ Code.buf[ saveList.get(posl - 1) - 1 ] - 43] + 43);
			
			saveList.clear();
			haveList.clear();
						
		}
		else {
			
			int prvi=haveList.get(haveList.size() - 2);
			int posl=haveList.get(haveList.size() - 1);
		
			//AKO BAS POSLEDNJI uslov JESTE ISPUNJEN samo ce nastaviti dalje, tako da je to ok
			//AKO BAS POSLEDNJI uslov NIJE ISPUNJEN (inverse) skace iza tela if a gde se trenutno nalazimo -> A TO JE ELSE
			for (int i = prvi; i < posl ; i++) {				
				Code.fixup(saveList.get(i));
			}
			
			Code.buf[saveList.get(posl - 1) - 1] = (byte) (Code.inverse[ Code.buf[ saveList.get(posl - 1) - 1 ] - 43] + 43);
				
			int broj=posl-prvi;
			while(broj>0) {
				saveList.remove(saveList.size()-1);
				broj--;
			}

			haveList.remove(haveList.size() - 1);
		}
		
		
		
	}
	
	
	public void visit(StatementIfElse elseSTMT) {
		//ZAVRSEN ELSE, prepravi jmp skok iza poslednjeg else
		
		if (izaElse.size() != 0) {
			Code.fixup(izaElse.get(izaElse.size()-1));
			izaElse.remove(izaElse.size()-1);
		}
	}

	public void visit(StatementIf ifSTMT) {
		
	//	report_info(" USAO U IF STATEMENT! ",null);
		
		if (haveList.size() == 1) {
			
			int posl=haveList.get(haveList.size() - 1);
			
			for (int i =  0; i < posl; i++) {			
				Code.fixup(saveList.get(i));
			}
			
			Code.buf[saveList.get(posl - 1) - 1] = (byte) (Code.inverse[ Code.buf[ saveList.get(posl - 1) - 1 ] - 43] + 43);
			
			saveList.clear();
			haveList.clear();
						
		}
		else {
			
			int prvi=haveList.get(haveList.size() - 2);
			int posl=haveList.get(haveList.size() - 1);
		
			//AKO BAS POSLEDNJI uslov NIJE ISPUNJEN (inverse) skace iza tela if a gde se trenutno nalazimo
			for (int i = prvi; i < posl ; i++) {				
				Code.fixup(saveList.get(i));
			}
			
			Code.buf[saveList.get(posl - 1) - 1] = (byte) (Code.inverse[ Code.buf[ saveList.get(posl - 1) - 1 ] - 43] + 43);
				
			int broj=posl-prvi;
			while(broj>0) {
				saveList.remove(saveList.size()-1);
				broj--;
			}

			haveList.remove(haveList.size() - 1);
		}
		
		


	
	}
	
	public void visit(PocetakWhileI poceetak) {
		//report_info("PC ULAZ WHILE PROVERA USLOVA "+Code.pc,null);		
		whileStart.add(Code.pc);
		breakLocation.add(0);		
	}
		
	public void visit(StatementBreak breakSTMT) {	
		breakFix.add(Code.pc + 1);
		breakLocation.set(breakLocation.size() - 1, breakLocation.get(breakLocation.size() - 1) + 1);
		Code.putJump(0);
	}
	
	public void visit(StatementContinue contSTMT) {
		//vrati na proveru uslova
		Code.putJump(whileStart.get(whileStart.size() - 1));
	}
	
	public void visit(StatementWhile whileSTMT) {
		
		//vrati na proveru uslova
		Code.putJump(whileStart.get(whileStart.size() - 1));	
		whileStart.remove(whileStart.size() - 1);
	
		
		//ovo je na kraju tela while-a i na tu adresu se fix uje poslednji break  RAZMISLI JOSSSSSS
		for (int i = breakLocation.get(breakLocation.size() - 1); i > 0; i--) {
			Code.fixup(breakFix.get(breakFix.size() - 1));
			breakFix.remove(breakFix.size() - 1);
		}
		breakLocation.remove(breakLocation.size() - 1);
	
	
		if (haveList.size() == 1) {
			
			int posl=haveList.get(haveList.size() - 1);
			
			for (int i =  0; i < posl; i++) {			
				Code.fixup(saveList.get(i));
			}
			
			Code.buf[saveList.get(posl - 1) - 1] = (byte) (Code.inverse[ Code.buf[ saveList.get(posl - 1) - 1 ] - 43] + 43);
			
			saveList.clear();
			haveList.clear();
				
		}
		else {
			
			int prvi=haveList.get(haveList.size() - 2);
			int posl=haveList.get(haveList.size() - 1);
		
			//AKO BAS POSLEDNJI uslov NIJE ISPUNJEN (inverse) skace iza tela while-a gde se trenutno nalazimo
			for (int i = prvi; i < posl ; i++) {				
				Code.fixup(saveList.get(i));
			}
			
			Code.buf[saveList.get(posl - 1) - 1] = (byte) (Code.inverse[ Code.buf[ saveList.get(posl - 1) - 1 ] - 43] + 43);
				
			int broj=posl-prvi;
			while(broj>0) {
				saveList.remove(saveList.size()-1);
				broj--;
			}

			haveList.remove(haveList.size() - 1);
		
		}
			
			
		
		
	}

/////////////////////////////////////////////// READ /////////////////////////////////////////////////////////////////////////

	public void visit(StatementRead readSTMT) {
		
		Struct s = readSTMT.getDesignator().obj.getType();
		if (s == Tabela.charType) {
			Code.put(Code.bread);
		} else {
			Code.put(Code.read);
		}
		
		if (readSTMT.getDesignator().getClass() == DesignatorIzraz.class) {
			
			if (s == Tab.charType || (s.getKind() == 3 && s.getElemType() == Tab.charType)) {
				Code.put(Code.bastore);
			} else {
				Code.put(Code.astore);
			}
			
		} else if(readSTMT.getDesignator().getClass() == DesignatorIdent.class) {
	
		Code.store(readSTMT.getDesignator().obj);
		}
		
	}
	
/////////////////////////////////////////////// FOREACH /////////////////////////////////////////////////////////////////////////

	ArrayList<Integer> foreachAdrese = new ArrayList<>();
	ArrayList<Integer> foreachZagrada = new ArrayList<>();
	
	int nivoFor=0;
	int poslednjaAdrZaDodelu = 50;
	public void visit(PocetakForEach sc) {
		nivoFor++;
	}
	
	
	public void visit(StatementForeach sc) {
		
		Code.putJump(foreachAdrese.get(foreachAdrese.size() - 1));
		Code.fixup(foreachZagrada.get(foreachZagrada.size() - 1));
		foreachAdrese.remove(foreachAdrese.size() - 1);
		foreachZagrada.remove(foreachZagrada.size() - 1);
		nivoFor--;
	}
	//int velTrenForEachNiza=0;
	Obj nizForeach=null;
	
	public void visit(DesignatorForI desig) {
		nizForeach=desig.getDesignator().obj;
	}
	//nizForeach
	public void visit(ForIdent sc) {
		
		Obj gdeSmesta= sc.obj;
		
		//stavljamo trenutni indeks niza na neku adresu u memoriji
		Obj obj1 = Tab.insert(Obj.Var, (" pom za nivo "+nivoFor), Tab.intType);	
		obj1.setAdr(poslednjaAdrZaDodelu);
		poslednjaAdrZaDodelu ++;
		Code.loadConst(0);
		Code.store(obj1);
		
		//mesto odakle krece foreach, i na koje se vraca ako nije kraj niza,zato ga dodamo u adrese
		foreachAdrese.add(Code.pc);
		obj1 = Tab.find(" pom za nivo "+nivoFor);
		
		Code.load(nizForeach); 
		Code.put(Code.arraylength); //duzina niza
		Code.load(obj1);			//trenutni index
		
		//ako su jednaki skace se van foreacha
		foreachZagrada.add(Code.pc + 1);
		Code.putFalseJump(Code.ne, 0);
		
		Code.load(nizForeach);				// adresa niza
		Code.load(obj1);				 	// indeks niza
		Code.put(Code.aload);				// ucita se element
		Code.store(gdeSmesta);				// element se smesti u promenljivu curr 
		
		//uvecaj indeks niza
		Code.load(obj1);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(obj1);
	}
	
/////////////////////////////////////////////// MINUSMINUS /////////////////////////////////////////////////////////////////////////

	public void visit(DesigMinusMinus izraz) {
		
		if (izraz.getDesignator().getClass() == DesignatorIzraz.class) {

			Code.put(Code.dup2);

			if (izraz.getDesignator().obj.getType() == Tab.charType) {
				Code.put(Code.baload);
			} else {
				Code.put(Code.aload);
			}

			Code.put(Code.const_1);
			Code.put(Code.sub);
			if (izraz.getDesignator().obj.getType() == Tab.charType) {
				Code.put(Code.bastore);
			} else {
				Code.put(Code.astore);
			}

		} else {
			//Code.load(izraz.getDesignator().obj);
			Code.put(Code.const_1);
			Code.put(Code.sub);
			Code.store(izraz.getDesignator().obj);

		}
		
	}
	
/////////////////////////////////////////////// PLUSPLUS /////////////////////////////////////////////////////////////////////////

	public void visit(DesigPlusPlus izraz) {
		
		if (izraz.getDesignator().getClass() == DesignatorIzraz.class) {

			Code.put(Code.dup2);

			if (izraz.getDesignator().obj.getType() == Tab.charType) {
				Code.put(Code.baload);
			} else {
				Code.put(Code.aload);
			}

			Code.put(Code.const_1);
			Code.put(Code.add);
			if (izraz.getDesignator().obj.getType() == Tab.charType) {
				Code.put(Code.bastore);
			} else {
				Code.put(Code.astore);
			}

		} else {
			//Code.load(izraz.getDesignator().obj);
			Code.put(Code.const_1);
			Code.put(Code.add);
			Code.store(izraz.getDesignator().obj);

		}
	
	}
	

	
}
