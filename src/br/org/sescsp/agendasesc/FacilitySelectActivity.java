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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import br.org.sescsp.infosesc.R;

import com.androidquery.AQuery;

public class FacilitySelectActivity extends Activity {

    private AQuery a;
    private AgendaSESC agendaSESC;
    private Helper h;
    private ListView listView;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    boolean[] facilitiesSelected = {};

	    setContentView(R.layout.activity_facilityselect);
	     
	    a = new AQuery(this);
	   	    
	    // get variables from calling activity
	 	Intent intent = getIntent();
	 	if(intent != null) {
	 	   facilitiesSelected = intent.getBooleanArrayExtra(PageViewActivity.SEL_MSG);
	 	}
	      
	    listView = a.id(R.id.facilityListView).getListView();
	    a.adapter(new FacilityAdapter(this, android.R.layout.simple_list_item_multiple_choice, AgendaSESC.FACILITY_NAMES)); 	    
	    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	    //listView.setDividerHeight(0);
	 	  
		for(int i = 0; i < facilitiesSelected.length; i++)
		{
			  listView.setItemChecked(i, facilitiesSelected[i]);
	    }
	    
	}
	
	
	@Override
	public void finish() { 	
	  int itemCount = listView.getCount();
	  boolean[] facilitiesSelected = new boolean[itemCount];
	  
	  SparseBooleanArray sparseBooleanArray = listView.getCheckedItemPositions();

	  for(int i = 0; i < itemCount; i++)
	  {
		  facilitiesSelected[i] = sparseBooleanArray.get(i);
      }
		
	  // Prepare data intent 
	  Intent data = new Intent();
	  data.putExtra(PageViewActivity.SEL_MSG, facilitiesSelected);
	
	  // Activity finished ok, return the data
	  setResult(RESULT_OK, data);
		
	  super.finish();
	}
	
	
	private class FacilityAdapter extends ArrayAdapter<String> {

		public FacilityAdapter(Context context, int textViewResourceId,
				String[] objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}




	}

}
