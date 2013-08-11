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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import br.org.sescsp.infosesc.R;

import com.androidquery.AQuery;


public class CategoriesSelectActivity extends Activity {
	static boolean[] categoriesSelected = {}; // will be edited in the adapter class
	AQuery a;
	Helper h;
	SharedPreferences mPrefs; 

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_categoriesselect);
	    
		mPrefs = this.getSharedPreferences("AgendaSESCPrefs", MODE_PRIVATE);

		// get variables from calling activity
		Intent intent = getIntent();
		if(intent != null) {
		  categoriesSelected = intent.getBooleanArrayExtra(PageViewActivity.SEL_MSG);
		}	

	    GridView gridview = (GridView) findViewById(R.id.gridview);
	    gridview.setAdapter(new CategoryAdapter(this));
	}
	
	@Override
	public void finish() {
	  // Prepare data intent 
	  Intent data = new Intent();
	  data.putExtra(PageViewActivity.SEL_MSG, categoriesSelected);
	
	  // Activity finished ok, return the data
	  setResult(RESULT_OK, data);
		
	  super.finish();
	}

	
	private class CategoryAdapter extends BaseAdapter implements OnClickListener {
		private Context mContext;
	
		public CategoryAdapter(Context c) {
			mContext = c;
			//this.categoryAgenda = categoryAgenda;
		}

		public int getCount() {
			//return AgendaSESC.CATEGORY_DRAWABLES.length;
			return categoriesSelected.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			CheckBox checkBox;
			TextView textView;
			View gridElementView;

			if (convertView == null) { // if it's not recycled, initialize some attributes
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				gridElementView = inflater.inflate(
						R.layout.category_grid_element, parent, false);
			} else {
				gridElementView = convertView;
			}

			Resources res = mContext.getResources();
			LayerDrawable image_checked = (LayerDrawable) res
					.getDrawable(R.drawable.ic_gridelement_checked);
			LayerDrawable image_notchecked = (LayerDrawable) res
					.getDrawable(R.drawable.ic_gridelement_not_checked);

			Bitmap replaceBitmap = BitmapFactory.decodeResource(res,
					AgendaSESC.CATEGORY_DRAWABLES[position]);
			BitmapDrawable replaceDrawable = new BitmapDrawable(res,
					replaceBitmap);
			replaceDrawable.setGravity(Gravity.TOP);

			image_checked.setDrawableByLayerId(R.id.grid_image_checked,
					replaceDrawable);
			image_notchecked.setDrawableByLayerId(R.id.grid_image_notchecked,
					replaceDrawable);

			StateListDrawable states = new StateListDrawable();
			states.addState(new int[] { android.R.attr.state_checked },
					image_checked);
			states.addState(new int[] {}, image_notchecked);

			checkBox = (CheckBox) gridElementView
					.findViewById(R.id.grid_checkBox);
			checkBox.setBackgroundDrawable(states);
			checkBox.setTag(Integer.valueOf(position));
			checkBox.setOnClickListener(this);
			
			if (categoriesSelected[position]) {
				checkBox.setChecked(true);
			} else {
				checkBox.setChecked(false);
			}

			textView = (TextView) gridElementView
					.findViewById(R.id.grid_textView);
			textView.setText(AgendaSESC.CATEGORY_NAMES[position]);

			return gridElementView;
		}

		@Override
		public void onClick(View v) {
			Object viewTag = v.getTag();
			
			if(viewTag instanceof Integer) {
				Integer categoryArrayId = (Integer) viewTag;
				if(categoryArrayId >= 0 && categoryArrayId < AgendaSESC.CATEGORY_IDS.length) {
					//categoryAgenda.toggleCategoryByArrayId(categoryArrayId);
					categoriesSelected[categoryArrayId] = !categoriesSelected[categoryArrayId];
				}			
			}
		}

	}
	
}
