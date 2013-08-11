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

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import br.org.sescsp.infosesc.R;

import com.androidquery.AQuery;

public class ShowImageActivity extends Activity {

	ImageViewTouch mImage;
	AQuery a;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_image);
	}

	
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		
	    Intent intent = getIntent();
	    String imageUrl = intent.getStringExtra(PageViewActivity.IMG_MSG);

		mImage = (ImageViewTouch) findViewById( R.id.image );
		
		// set the default image display type
		mImage.setDisplayType( DisplayType.NONE );
		
		new AQuery(this).id(R.id.image).image(imageUrl);
	}
	
	@Override
	public void onConfigurationChanged( Configuration newConfig ) {
		super.onConfigurationChanged( newConfig );
	}
	
	
}
