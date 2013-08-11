/*
 *  AgendaSESC - Android App which shows the program and courses of SESC SP
 *  Copyright (C) 2013 Steffen Retzlaff
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 *
 */

package br.org.sescsp.agendasesc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.json.JSONArray;

import br.org.sescsp.infosesc.R;


public class AgendaSESC {
	
	private static final long MS_DAY = 1000*60*60*24; //milliseconds of a day
    private static final String DATE_FORMAT = "yyyy-M-d";

    public static final int TYPE_PROGRAM = 0;
    public static final int TYPE_COURSE = 1;
    
    public static final int ITEMS_DATES = 0;
    public static final int ITEMS_CATEGORY = 1;
    public static final int ITEMS_FACILITY = 2;
 
    public static final int[] CATEGORY_IDS = {42,4,43,46,44,60,56,57,6,53,50,8,52,54,55,49,47,45,58};
    public static final String[] CATEGORY_NAMES = {"Artes Visuais", "Cinema e vídeo",
       "Circo", "Literatura", "Artemídia e Cultura Digital", "Artes Manuais", "Alimentação", "Saúde",
       "Esporte e Atividade Física", "Meio Ambiente", "Ações para a Cidadania", "Crianças", "Jovens",
       "Idosos", "Intergerações", "Teatro", "Música", "Dança", "Empresas"}; 
    
    // Unidades Grande São Paulo (for now)
    public static final int[] FACILITY_IDS = {25,72,1,601,2,3,4,5,6,7,69,10,11,38,26,37,12,65,13};
        public static final String[] FACILITY_NAMES = {"Belenzinho","Bom Retiro","Carmo",
    	"Centro de Pesquisa e Formação","CineSesc","Consolação","Interlagos","Ipiranga","Itaquera",
    	"Odontologia","Osasco","Pinheiros","Pompeia","Santana","Santo Amaro","Santo André",
    	"São Caetano","TV","Vila Mariana"};
     
    public static final int[] CATEGORY_DRAWABLES = {R.drawable.ic_category_artesvisuais, R.drawable.ic_category_cinema, 
    	R.drawable.ic_category_circo, R.drawable.ic_category_literatura, R.drawable.ic_category_artemidia, R.drawable.ic_category_artesmanuais,
    	R.drawable.ic_category_alimentacao, R.drawable.ic_category_saude, R.drawable.ic_category_esporte, R.drawable.ic_category_meio_ambiente, 
    	R.drawable.ic_category_acoes_cidadania, R.drawable.ic_category_criancas, R.drawable.ic_category_jovens, R.drawable.ic_category_idosos,
    	R.drawable.ic_category_intergeracoes, R.drawable.ic_category_teatro, R.drawable.ic_category_musica, R.drawable.ic_category_danca, R.drawable.ic_category_empresas}; 
    
    public boolean[] categoriesSelected;
    public boolean[] facilitiesSelected;
    //private int categoriesSelectedCnt;
	    
    private int currentItemPointer;
    
    public Calendar agendaCalendar;
    public Calendar today;
    public int programType = TYPE_PROGRAM;
    public int groupBy = ITEMS_DATES;
    
    public String searchTerm = "";
    
    private SimpleDateFormat dateFormat;
     
    // private int[] facilities; 

    private static final String urlDomain = "http://www.sescsp.org.br/";
    private static final String urlComplement = "/ajax/filtro.action?";
    
    
    
    
	public AgendaSESC(Calendar agendaCalendar) {
		initiateAgendaSESC(agendaCalendar);
	}
	
	public AgendaSESC() {
		initiateAgendaSESC(null);
   	}
	
	private void initiateAgendaSESC(Calendar agendaCalendar) {
		// plausibility AgendaSESC class check
		if      (CATEGORY_DRAWABLES.length != CATEGORY_IDS.length) {
			throw new RuntimeException("Unequal category array lengths!"+
					" CATEGORY_DRAWABLES: "+CATEGORY_DRAWABLES.length+
					" CATEGORY_IDS: "      +CATEGORY_IDS.length);
		} 
		else if (CATEGORY_DRAWABLES.length != CATEGORY_NAMES.length) {
			throw new RuntimeException("Unequal category array lengths!"+
					" CATEGORY_DRAWABLES: "+CATEGORY_DRAWABLES.length+
					" CATEGORY_NAMES: "    +CATEGORY_NAMES.length);
		} 
		else if (CATEGORY_IDS.length != CATEGORY_NAMES.length) {
			throw new RuntimeException("Unequal category array lengths!"+
					" CATEGORY_IDS: "      +CATEGORY_IDS.length+
					" CATEGORY_NAMES: "    +CATEGORY_NAMES.length);
		}
		
		// all categories and facilities to be selected		
	    categoriesSelected = new boolean[CATEGORY_IDS.length];
	    facilitiesSelected = new boolean[FACILITY_IDS.length];
		Arrays.fill(categoriesSelected, Boolean.TRUE);
		Arrays.fill(facilitiesSelected, Boolean.FALSE); // if all are false, all will be shown
		
	    currentItemPointer = 0;
				
		dateFormat = new SimpleDateFormat(DATE_FORMAT);
		today = Calendar.getInstance();
    	if(agendaCalendar == null)       this.agendaCalendar = Calendar.getInstance();
    	else                         this.agendaCalendar = (Calendar) agendaCalendar.clone();
    	//if(categoriesSelected.length == 0) this.categoriesSelected = CATEGORY_IDS;
    	//else                         this.categoriesSelected = categoriesSelected;
    	this.agendaCalendar.setLenient(true);
    	currentItemPointer = 0;
		
	}
	
	public AgendaSESC copyInstance() {
		AgendaSESC newAgenda = new AgendaSESC(agendaCalendar);
		newAgenda.programType = programType;
	    newAgenda.searchTerm = searchTerm;
	    newAgenda.groupBy = groupBy;
	    newAgenda.currentItemPointer = currentItemPointer;
	    newAgenda.categoriesSelected = categoriesSelected;
	    newAgenda.facilitiesSelected = facilitiesSelected;	    
	    //newAgenda.categoriesSelectedCnt = categoriesSelectedCnt;
	    return newAgenda;
	}
	
	public boolean nextDay() {
			agendaCalendar.set(Calendar.DAY_OF_MONTH, agendaCalendar.get(Calendar.DAY_OF_MONTH)+1);
			return true;
	}
	
	public boolean prevDay() {
			//agendaCalendar.setTimeInMillis(agendaCalendar.getTimeInMillis()-MS_DAY);
			agendaCalendar.set(Calendar.DAY_OF_MONTH, agendaCalendar.get(Calendar.DAY_OF_MONTH)-1);
        	return true;
	}
	
	public void nextItem() {
		switch (groupBy) {
		case ITEMS_DATES:
			nextDay();
			break;
		case ITEMS_CATEGORY:
			int i = 0;
			do {
				currentItemPointer++; 
				if (currentItemPointer >= CATEGORY_IDS.length) 
					currentItemPointer = 0;
				i++;
			} while (!categoriesSelected[currentItemPointer] && i < CATEGORY_IDS.length);

			break;
		case ITEMS_FACILITY:
			
			break;

		default:
			break;
		}
	}

	public void prevItem() {
		switch (groupBy) {
		case ITEMS_DATES:
			prevDay();
			break;
		case ITEMS_CATEGORY:
			int i = 0;
			do {
				currentItemPointer--; 
				if (currentItemPointer < 0) 
					currentItemPointer = CATEGORY_IDS.length - 1;
				i++;
			} while (!categoriesSelected[currentItemPointer] && i < CATEGORY_IDS.length);

			break;
		case ITEMS_FACILITY:
			
			break;

		default:
			break;
		}
	}
	
	public void setGroupBy(int newGroupBy) {
		this.groupBy = newGroupBy;
	}

	public int getGroupBy() {
		return groupBy;
	}
	
	public String getURL() {
		String url = "";
		String filterString = "";

		// show only selected facilities
		for (int i = 0; i < FACILITY_IDS.length; i++) {
			if(facilitiesSelected[i]) filterString += "&unities="+FACILITY_IDS[i];
		}
		
		// show only selected categories
		if (groupBy == ITEMS_CATEGORY) {
				filterString += "&type="+CATEGORY_IDS[currentItemPointer];
		}
		else {
			for (int i = 0; i < CATEGORY_IDS.length; i++) {
				if(categoriesSelected[i]) filterString += "&type="+CATEGORY_IDS[i];
			}	
		}
		
	
		
		try {
			if (programType == TYPE_COURSE) {
				if (searchTerm == "")
					url = urlDomain + "aulas" + urlComplement + filterString + "&dates="
							+ this.getDateString(); //+ "&page=" + this.getPage();
				else
					url = urlDomain + "busca/ajax/aula.action?q="
							+ URLEncoder.encode(searchTerm, "ISO-8859-1");
			} else {
				if (searchTerm == "")
					url = urlDomain + "programacao" + urlComplement + filterString
							+ "&dates=" + this.getDateString(); //+ "&page=" + this.getPage();
				else
					url = urlDomain + "busca/ajax/programacao.action?q="
							+ URLEncoder.encode(searchTerm, "ISO-8859-1");
			}
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

	public boolean setCalendar(Calendar newCalendar) {
			agendaCalendar = newCalendar;
			return true;
    }
	
	public Calendar getCalendar() {
		return agendaCalendar;
	}
	
	public void setSearch(String newSearch) {
		searchTerm = newSearch;
	} 

	public void deleteSearch() {
		searchTerm = "";
	}
	
	
	public void setType(int programType) {
		this.programType = programType;
	}
	
	public boolean setDayDiff(int dayDiffToToday) {
		//long newTimeMs = today.getTimeInMillis()+ dayDiffToToday * MS_DAY;
		agendaCalendar.setTime(today.getTime());
		agendaCalendar.add(Calendar.DAY_OF_MONTH, dayDiffToToday);
		return true;
	}
	
	public int getDayDiff() {
		return (int) ((agendaCalendar.getTimeInMillis() - today.getTimeInMillis()) / MS_DAY);
	}
	
	/* public boolean setDateString(String date) {
			try {
				agendaCalendar = dateFormat.parse(date);
				return true;
			} catch (ParseException e) {
				//e.printStackTrace();
				return false;
			} 
	} */
	
	public String getDateString() {
		return dateFormat.format(agendaCalendar.getTime());
	}
	
	public Date getDate() {
		return agendaCalendar.getTime();
	}

	public void setDate(Date newDate) {
		agendaCalendar.setTime(newDate);
	}
	
	public String getLocaleString() {
		return new SimpleDateFormat("dd/MM/yyyy").format(agendaCalendar.getTime());
	}
	
	public String getItemString() {
		switch (groupBy) {
		case ITEMS_DATES:
			return new SimpleDateFormat("dd/MM/yyyy").format(agendaCalendar.getTime());
		case ITEMS_CATEGORY:
			return CATEGORY_NAMES[currentItemPointer]; 
		case ITEMS_FACILITY:
			return FACILITY_NAMES[currentItemPointer];

		default:
			return "";
		}
	}
	
	public void setCategories(boolean[] categoriesSelected){
		this.categoriesSelected = categoriesSelected;
	}
	
	public void setFacilities(boolean[] facilitiesSelected){
		this.facilitiesSelected = facilitiesSelected;
	}
	
	public void setCategory(int categoryID, boolean categoryIsSelected){
		for (int i = 0; i < CATEGORY_IDS.length; i++) {
			if(CATEGORY_IDS[i] == categoryID) {
				categoriesSelected[i] = categoryIsSelected;
				//categoriesSelectedCnt++;
			}
		}
	}
	
	public void setFacility(int facilityID, boolean facilityIsSelected){
		for (int i = 0; i < CATEGORY_IDS.length; i++) {
			if(FACILITY_IDS[i] == facilityID) {
				facilitiesSelected[i] = facilityIsSelected;
				//categoriesSelectedCnt++;
			}
		}
	}
	
	public boolean setSelectedItem(int selectionID, boolean isSelected, int selectionItem){
		switch (selectionItem) {
		case ITEMS_CATEGORY:
			setCategory(selectionID, isSelected);
			return true;
		case ITEMS_FACILITY:
			setFacility(selectionID, isSelected);	
			return true;
		default:
			// do nothing
			return false;
		}
	}
	
	public boolean[] getCategories(){
		return categoriesSelected;
	}	
	
	public boolean[] getFacilities(){
		return facilitiesSelected;
	}	
	
	public String getSelectionCSV(int selectionItem){
		ArrayList<Integer> selectionList = new ArrayList<Integer>();
		String selectionCSV = "";
		boolean[] selectedItems = {};
		int[] selectionIDs = {};
		
		switch (selectionItem) {
		case ITEMS_CATEGORY:
			selectedItems = categoriesSelected;
			selectionIDs = CATEGORY_IDS;
			break;
		case ITEMS_FACILITY:
			selectedItems = facilitiesSelected;
			selectionIDs = FACILITY_IDS;			
			break;
		default:
			return "";
		}
		
		for (int i = 0; i < selectionIDs.length; i++) {
			if(selectedItems[i]) selectionList.add(selectionIDs[i]);
		}		
		selectionCSV = selectionList.toString();
		// delete all [ and ]
		selectionCSV = selectionCSV.replace("[","").replace("]","");
		
		return selectionCSV;
	}	

	public boolean setSelectionByCSV(String selectionCSV, int selectionItem){
		boolean[] selectedItems = {};
		String[] selectedIDs = selectionCSV.split(", ");

		switch (selectionItem) {
		case ITEMS_CATEGORY:
			selectedItems = categoriesSelected;
			break;
		case ITEMS_FACILITY:
			selectedItems = facilitiesSelected;
			break;
		default:
			return false;
		}
		
		Arrays.fill(selectedItems, Boolean.FALSE);
		try {
			for (int i = 0; i < selectedIDs.length; i++) {
				setSelectedItem(Integer.parseInt(selectedIDs[i]), true, selectionItem);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	
	/*public void addCategory(int addCategoryID){
		for (int i = 0; i < CATEGORY_IDS.length; i++) {
			if(CATEGORY_IDS[i] == addCategoryID && categoriesSelected[i] == false) {
				categoriesSelected[i] = true;
				//categoriesSelectedCnt++;
			}
		}
	} */
	
	/* public void removeCategory(int removeCategoryID) {
		for (int i = 0; i < CATEGORY_IDS.length; i++) {
			if(CATEGORY_IDS[i] == removeCategoryID && categoriesSelected[i] == true) {
				categoriesSelected[i] = false;
				categoriesSelectedCnt--;
			}
		}
	}*/
	
	/*public void toggleCategory(int categoryID) {
		for (int i = 0; i < CATEGORY_IDS.length; i++) {
			if(CATEGORY_IDS[i] == categoryID && categoriesSelected[i]) {
				categoriesSelected[i] = false;
				categoriesSelectedCnt--;
			} else if(CATEGORY_IDS[i] == categoryID && !categoriesSelected[i]) {
				categoriesSelected[i] = true;
				categoriesSelectedCnt++;
			}
		}
	} */
	
	/*public void toggleCategoryByArrayId(int arrayId) {
		categoriesSelected[arrayId] = !categoriesSelected[arrayId];
		/*if(categoriesSelected[arrayId]) {
			categoriesSelected[arrayId] = false;
			categoriesSelectedCnt--;
		} else if(!categoriesSelected[arrayId]) {
			categoriesSelected[arrayId] = true;
			categoriesSelectedCnt++;
		}* /
	}

	public boolean isSelectedCategory(int categoryID) {
		boolean categorySelected = false;
		for (int i = 0; i < CATEGORY_IDS.length; i++) {
			if(CATEGORY_IDS[i] == categoryID && categoriesSelected[i]) categorySelected = true;
		}
		return categorySelected;
	} */
	
	public boolean sameCategoriesSelected(boolean[] compareCategoriesSelected) {
		boolean sameCategoriesSelected = true;
		
		for (int i = 0; i < CATEGORY_IDS.length; i++) {
			if(compareCategoriesSelected[i] != categoriesSelected[i]) sameCategoriesSelected = false;
		}
		return sameCategoriesSelected;
	} 
	
	public boolean sameFacilitiesSelected(boolean[] compareFacilitiesSelected) {
		boolean sameFacilitiesSelected = true;
		
		for (int i = 0; i < CATEGORY_IDS.length; i++) {
			if(compareFacilitiesSelected[i] != facilitiesSelected[i]) sameFacilitiesSelected = false;
		}
		return sameFacilitiesSelected;
	} 

}
