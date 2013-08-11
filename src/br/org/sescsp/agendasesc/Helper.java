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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class Helper {
	private Context context;
	public static int DAY_MS = 1000*60*60*24;
	
	public Helper(Context context) {
		this.context = context;
	}
	
	public void showAboutDialog() {
		// Show OK-Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Aplicativo SESC Hackathon 2013 Grupo 3: Rafael, Saulo, Steffen e Rodrigo")
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                //do things
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	
	}
	
	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}
	
	public static ArrayList<View> getAllChildren(View v) {

	    if (!(v instanceof ViewGroup)) {
	        ArrayList<View> viewArrayList = new ArrayList<View>();
	        viewArrayList.add(v);
	        return viewArrayList;
	    }

	    ArrayList<View> result = new ArrayList<View>();

	    ViewGroup vg = (ViewGroup) v;
	    for (int i = 0; i < vg.getChildCount(); i++) {

	        View child = vg.getChildAt(i);

	        ArrayList<View> viewArrayList = new ArrayList<View>();
	        viewArrayList.add(v);
	        viewArrayList.addAll(getAllChildren(child));

	        result.addAll(viewArrayList);
	    }
	    return result;
	}
	
	public void msgbox(String message) {
    	Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
