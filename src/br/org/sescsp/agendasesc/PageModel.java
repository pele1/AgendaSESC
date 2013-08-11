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

import java.util.Date;

import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PageModel {

	/*private int index;
	private String url;
	private String alternativeUrl; */
	
	public static final int TYPE_SHOW_AGENDA = 0;
	public static final int TYPE_ALTERNATIVE_AGENDA = 1;
	
	public TextView textView;
	public WebView webView1;
	public WebView webView2;
	public RelativeLayout relWebLayout;
	public float webDownX;
	public ProgressBar progressBar;

	AgendaSESC myAgenda1;
	AgendaSESC myAgenda2;
	
	//private AQuery a;
	//private Helper h;

	/*public PageModel(int index) {
		this.index = index;
		myAgenda1 = new AgendaSESC();
		myAgenda2 = new AgendaSESC();
		myAgenda1.setType(AgendaSESC.TYPE_PROGRAM);
		myAgenda2.setType(AgendaSESC.TYPE_COURSE);
		setIndex(index, false);
	} */
	
	public PageModel() {
		myAgenda1 = new AgendaSESC();
		myAgenda2 = new AgendaSESC();
	}
	public PageModel(AgendaSESC pageAgenda, AgendaSESC alternativeAgenda) {
		myAgenda1 = pageAgenda.copyInstance();
		myAgenda2 = alternativeAgenda.copyInstance();
	}
	
	public AgendaSESC getAgenda(int agendaType) {
		if(agendaType == TYPE_SHOW_AGENDA)
			return myAgenda1;
		else
			return myAgenda2;
	}

	public void setAgenda(AgendaSESC newAgenda, int agendaType) {
		if(agendaType == TYPE_SHOW_AGENDA)
			myAgenda1 = newAgenda.copyInstance();
		else
			myAgenda2 = newAgenda.copyInstance();
	}
	
	public void setAgenda(AgendaSESC newAgenda) {
		setAgenda(newAgenda, TYPE_SHOW_AGENDA);
	}

}