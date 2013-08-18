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

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.regex.Matcher;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import br.org.sescsp.infosesc.R;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

public class PageViewActivity extends SherlockFragmentActivity implements
		OnDateSetListener, ViewPager.OnPageChangeListener {
	
	SharedPreferences mPrefs; 

	public final static String PACKAGE = "br.org.sescsp.infosesc";	
	// name the intent message keys
	public final static String IMG_MSG = PACKAGE+".IMAGEURL";
	public final static String SEL_MSG = PACKAGE+".SELECTION";
	public static final int CATEGORY_SEL_REQ = 1;
	public static final int FACILITY_SEL_REQ = 2;
			
	private boolean applicationStart = true;

	// we name the pages
	//private static final int PAGE_FIRST = 0;
	private static final int PAGE_LEFT = 0;
	private static final int PAGE_MIDDLE = 1;
	private static final int PAGE_RIGHT = 2;
	
	//private static final int PAGE_LAST = 4;
	
	private LayoutInflater mInflater;
	private int mSelectedPageIndex = PAGE_MIDDLE; // middle page will be the
												  // first page to be shown
	// we save each page in a model
	private PageModel[] mPageModel = new PageModel[PAGE_RIGHT + 1];

	private ViewPager viewPager;
	private boolean flipped = false;
	// private WebView finishedWebView;
	boolean loadingFinished = true;
	boolean redirect = false;
	boolean showingSplash = false;

	private AQuery a;
	private Helper h;

	// private MyPagerAdapter pageAdapter = null;
	private static AgendaSESC showAgenda;
	private static AgendaSESC alternativeAgenda;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page_view);
		
		a = new AQuery(this);
		h = new Helper(this);
		
		mPrefs = this.getSharedPreferences(PACKAGE+".PREFS", MODE_PRIVATE);

		showAgenda = new AgendaSESC();
		alternativeAgenda = new AgendaSESC();
			
		// load saved states
		// selection
		if(mPrefs.contains(CATEGORY_SEL_REQ+"show"))
				showAgenda.setSelectionByCSV(mPrefs.getString(CATEGORY_SEL_REQ+"show",""), AgendaSESC.ITEMS_CATEGORY);
		if(mPrefs.contains(CATEGORY_SEL_REQ+"alt"))
				alternativeAgenda.setSelectionByCSV(mPrefs.getString(CATEGORY_SEL_REQ+"alt",""), AgendaSESC.ITEMS_CATEGORY);
		
		showAgenda.setSelectionByCSV(mPrefs.getString(FACILITY_SEL_REQ+"show",""), AgendaSESC.ITEMS_FACILITY);
		alternativeAgenda.setSelectionByCSV(mPrefs.getString(FACILITY_SEL_REQ+"alt",""), AgendaSESC.ITEMS_FACILITY);
		// type
		showAgenda.setType       (mPrefs.getInt("showType",AgendaSESC.TYPE_PROGRAM));
		alternativeAgenda.setType(mPrefs.getInt("altType", AgendaSESC.TYPE_COURSE));
		showAgenda.setGroupBy       (mPrefs.getInt("showGroupBy",AgendaSESC.ITEMS_DATES));
		alternativeAgenda.setGroupBy(mPrefs.getInt("altGroupBy", AgendaSESC.ITEMS_CATEGORY));
		// flipped state
		flipped = mPrefs.getBoolean("flipped",false);

		// handle search request, if given
		handleIntent(getIntent());
		
		// splash screen
		if(applicationStart) {
			showSplash();
			applicationStart = false;
		}
		
		// button click functions
		a.id(R.id.imgNavBack).clickable(true).clicked(this, "navBackClick");
		a.id(R.id.imgNavForw).clickable(true).clicked(this, "navForwClick");
		a.id(R.id.txtDate).clickable(true).clicked(this, "footerClick");

		// show today date/item description in lower bar
		a.id(R.id.txtDate).text(showAgenda.getItemString());

		// initializing the page models
		mPageModel[PAGE_LEFT] = new PageModel(showAgenda, alternativeAgenda);
		mPageModel[PAGE_MIDDLE] = new PageModel(showAgenda, alternativeAgenda);
		mPageModel[PAGE_RIGHT]  = new PageModel(showAgenda, alternativeAgenda);
		setModelPages(showAgenda);
			
		mInflater = getLayoutInflater();

		MyPagerAdaper adapter = new MyPagerAdaper();

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(adapter);

		// jump directly to "PAGE_MIDDLE" (today) 
		viewPager.setCurrentItem(PAGE_MIDDLE, false);
		viewPager.setOffscreenPageLimit(2);

		// swap pages when page changes in order to virtually swipe infinitely
		viewPager.setOnPageChangeListener(this);
		
	}

	// show splash start screen
    private void showSplash() {
    	showingSplash = true;
		getSupportActionBar().hide();
		a.id(R.id.viewpager).invisible();
		a.id(R.id.bottomNav).invisible();
		a.id(R.id.splashScreen).visible();
		a.id(R.id.sescLogo).animate(R.anim.splash);
		a.id(R.id.sescLogo).getView().postDelayed(new Runnable() {
			// after 6 sec show app screen
			@Override
			public void run() {
				// hide splash screen
				hideSplash();
		    	showMsgProgramType();
			}
		}, 6000);
    }
    
    private void hideSplash() {	
    	showingSplash = false;
    	getSupportActionBar().show();
    	a.id(R.id.viewpager).visible();
    	a.id(R.id.bottomNav).visible();
    	a.id(R.id.splashScreen).invisible();
    }
	
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    // show menu items activity / search
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.page_view, menu);
		return true;
	}
	
	// disable menu if showing splash screen
	@Override
	 public boolean onPrepareOptionsMenu (Menu menu) {
		 boolean returnCode =  super.onPrepareOptionsMenu(menu);
		 
	     MenuItem aboutMenuItem = menu.findItem(R.id.menu_about);
	     aboutMenuItem.setVisible(!showingSplash);
	     return returnCode;
	 }
	
	// while showing splash just close (not the activity) it when using back-button
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK && showingSplash ) {
	        hideSplash();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}


	
		
	// handle option item clicks: flip, search, atividades, settings
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_event:
			flipEventPages();
			break;
		case R.id.menu_search:
            onSearchRequested();
			break; 
		case R.id.menu_atividades:
			showCategorySelection();
			break;
		case R.id.menu_unidades:
			showFacilitySelection();
			break;
		case R.id.menu_about:
			showAboutScreen();
			
			break;
		/*case R.id.menu_settings:
			startActivity(new Intent(PageViewActivity.this,
					SettingsActivity.class));
			break; */
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

    // handle search intent
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search the event
            //h.msgbox("Search: "+query);
            showAgenda.setSearch(query);
            applicationStart = false;
        }
    }
	
	// exchange the shown agendas: programação/aula (with animation)
	private void flipEventPages() {
		final PageModel currentModel = mPageModel[mSelectedPageIndex];

		FlipAnimation flipAnimation = new FlipAnimation(currentModel.webView1,
				currentModel.webView2);

		if (flipped) flipAnimation.reverse();
		// toggle flip state
		flipped = !flipped;
		
		currentModel.relWebLayout.startAnimation(flipAnimation);

		// exchange agendas
		AgendaSESC swapAgenda = showAgenda;
		showAgenda = alternativeAgenda;
		alternativeAgenda = swapAgenda;

		// exchange webviews
		currentModel.relWebLayout.removeView(currentModel.webView1);
		currentModel.relWebLayout.removeView(currentModel.webView2);

		WebView cachedWebView = currentModel.webView1;
		currentModel.webView1 = currentModel.webView2;
		currentModel.webView2 = cachedWebView;

		currentModel.relWebLayout.addView(currentModel.webView1);
		currentModel.relWebLayout.addView(currentModel.webView2);

		currentModel.webView1.setVisibility(View.VISIBLE);
		currentModel.webView2.setVisibility(View.INVISIBLE);

		setModelPages(showAgenda);
		setContent(PAGE_LEFT); 
		setContent(PAGE_RIGHT);
		
		showMsgProgramType();
		
		// save configuration
		saveConfig();
	}
	
	public void showMsgProgramType() {
		if(showAgenda.programType == AgendaSESC.TYPE_PROGRAM)
			h.msgbox("Programação");
		else if(showAgenda.programType == AgendaSESC.TYPE_COURSE)
			h.msgbox("Cursos");		
	}
	public void saveConfig() {
		// save complete config
		SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString(CATEGORY_SEL_REQ+"show", showAgenda.getSelectionCSV(AgendaSESC.ITEMS_CATEGORY));
  	    edit.putString(FACILITY_SEL_REQ+"show", showAgenda.getSelectionCSV(AgendaSESC.ITEMS_FACILITY));
		edit.putString(CATEGORY_SEL_REQ+"alt", alternativeAgenda.getSelectionCSV(AgendaSESC.ITEMS_CATEGORY));
  	    edit.putString(FACILITY_SEL_REQ+"alt", alternativeAgenda.getSelectionCSV(AgendaSESC.ITEMS_FACILITY));
  	    edit.putInt("showType", showAgenda.programType);
  	    edit.putInt("altType",  alternativeAgenda.programType);  	    
  	    edit.putInt("showGroupBy", showAgenda.groupBy);
  	    edit.putInt("altGroupBy",  alternativeAgenda.groupBy);  	    
  	    edit.putBoolean("flipped", flipped);
		edit.commit();			
	}

// CLICK HANDLERS START ----------
	
	// navigate to previous viewpager-page
	public void navBackClick(View v) {
		viewPager.setCurrentItem(mSelectedPageIndex - 1);
	}

	// navigate to next viewpager-page
	public void navForwClick(View v) {
		viewPager.setCurrentItem(mSelectedPageIndex + 1);
	}

	// open date picker in order to select the date of the agenda to be shown
	public void footerClick(View v) {
		switch (showAgenda.getGroupBy()) {
		case AgendaSESC.ITEMS_DATES:
			DialogFragment newFragment = new DatePickerFragment();
			newFragment.show(getSupportFragmentManager(), "datePicker");
			break;
		case AgendaSESC.ITEMS_CATEGORY:
			showCategorySelection();
			break;
		}

	}

	// hide splash screen
	public void hideSplashClick(View v) {
		hideSplash();
	}
	
	// load SESC SP site
	public void loadSescSiteClick(View v) {
		this.startActivity(
				new Intent(Intent.ACTION_VIEW, Uri.parse(AgendaSESC.urlDomain)));
	}
	
	
// ------------- CLICK HANDLERS END

	public void showCategorySelection() {
		Intent categorySelectIntent = new Intent(PageViewActivity.this, CategoriesSelectActivity.class);
		categorySelectIntent.putExtra(SEL_MSG, showAgenda.categoriesSelected);
		startActivityForResult(categorySelectIntent, CATEGORY_SEL_REQ);
	}
	
	public void showFacilitySelection() {
		Intent facilitySelectIntent = new Intent(PageViewActivity.this, FacilitySelectActivity.class);
		facilitySelectIntent.putExtra(SEL_MSG, showAgenda.facilitiesSelected);
		startActivityForResult(facilitySelectIntent, FACILITY_SEL_REQ);
	}
	
	
	public void showAboutScreen() {
		showingSplash = true;
		a.id(R.id.viewpager).invisible();
		a.id(R.id.bottomNav).invisible();
		a.id(R.id.splashScreen).visible();
		a.id(R.id.loadingContentAdvice).invisible();
		
		TextView splashText = a.id(R.id.splashDisclaimer).getTextView();
		splashText.setTextColor(Color.BLACK);
		splashText.setTextSize((float)18.0);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(10, 0, 10, 0);
		a.getView().setLayoutParams(lp);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Make sure the request was successful
        if (resultCode == RESULT_OK) {
        	boolean reloadContent = false;
        	// Check which request we're responding to
        	if (requestCode == CATEGORY_SEL_REQ) {
    	       	boolean[] oldCategoriesSelected = showAgenda.getCategories();   
    	       	boolean[] categoriesSelected = data.getExtras().getBooleanArray(PageViewActivity.SEL_MSG);
    	       	showAgenda.setCategories(categoriesSelected);        	    	
    	       	reloadContent = !showAgenda.sameCategoriesSelected(oldCategoriesSelected);
        	} else if (requestCode == FACILITY_SEL_REQ) {
    	       	boolean[] oldFacilitiesSelected = showAgenda.getFacilities();   
    	       	boolean[] facilitiesSelected = data.getExtras().getBooleanArray(PageViewActivity.SEL_MSG);
    	       	showAgenda.setFacilities(facilitiesSelected);  
    	       	reloadContent = !showAgenda.sameFacilitiesSelected(oldFacilitiesSelected);
        	} 	
        	
       		// if something changed, reload page
   	        if(reloadContent) {
        		setModelPages(showAgenda);
            	if(!(showAgenda.getGroupBy() == AgendaSESC.ITEMS_CATEGORY && requestCode == CATEGORY_SEL_REQ)) 
            		setContent(PAGE_MIDDLE);
            	setContent(PAGE_RIGHT);
            	setContent(PAGE_LEFT);
            }
	   	        
       		// save selected categories/facilities
       		saveConfig();
	    }
	}

	
	// update the content of a viewpager-page acording to agendasesc-options
	private void setContent(int index) {
			final PageModel model = mPageModel[index];

			model.progressBar.setVisibility(View.VISIBLE);
			model.webView1.setVisibility(View.GONE);
			model.webView1.stopLoading();

			loadURL(model.webView1, model.myAgenda1.getURL());
			if (index == PAGE_MIDDLE)
				loadURL(model.webView2, model.myAgenda2.getURL());
			// new Helper(this).msgbox(model.getURL());
	}

	// swap viewpager-pages (used for infinite swipe)
	private void swapPages(int page1, int page2) {
		final PageModel page1Model = mPageModel[page1];
		final PageModel page2Model = mPageModel[page2];

		page1Model.relWebLayout.removeView(page1Model.webView1);
		page2Model.relWebLayout.removeView(page2Model.webView1);

		WebView cachedWebView = page1Model.webView1;
		page1Model.webView1 = page2Model.webView1;
		page2Model.webView1 = cachedWebView;

		page1Model.relWebLayout.addView(page1Model.webView1);
		page2Model.relWebLayout.addView(page2Model.webView1);
	}

	
	// set the agendasesc-options to each viewpager-page according to the given "modelAgenda"
	private void setModelPages(AgendaSESC modelAgenda) {
		final PageModel leftPage = mPageModel[PAGE_LEFT];
		final PageModel middlePage = mPageModel[PAGE_MIDDLE];
		final PageModel rightPage = mPageModel[PAGE_RIGHT];

		rightPage.setAgenda(modelAgenda);
		leftPage.setAgenda(modelAgenda);
		middlePage.setAgenda(modelAgenda);
		
		leftPage.myAgenda1.prevItem();
		rightPage.myAgenda1.nextItem();
		
		//if (modelAgenda.programType == AgendaSESC.TYPE_PROGRAM) {
			 a.id(R.id.txtDate).text(modelAgenda.getItemString());
		//} else {
			//a.id(R.id.txtDate).text("Página " + showAgenda.getPage());
		//}

	}

	// adapter for viewpager-page handling
	
	// save viewpager-page index number
	@Override
	public void onPageSelected(int position) {
			mSelectedPageIndex = position;
	}

	// do nothing if viewpager-page scrolled a bit
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	// rearrange agenda and pages in order to swipe infinitely
	@Override
	public void onPageScrollStateChanged(int state) {
						
			if (state == ViewPager.SCROLL_STATE_IDLE) {

				// user swiped to right direction --> left page
				switch (mSelectedPageIndex) {

				case PAGE_LEFT:
					//showAgenda.setDayDiff(showAgenda.getDayDiff() - 1);
					showAgenda.prevItem();
					
					// exchange left and middle page, so that the left page
				    // can be displayed in the middle
				    // and user can continue scrolling
				    swapPages(PAGE_MIDDLE, PAGE_RIGHT);
				    swapPages(PAGE_MIDDLE, PAGE_LEFT); 
					
				    // commit changes of showDate to the whole model
					setModelPages(showAgenda);
					
					// now load new content (of new date) to the left page
					setContent(PAGE_LEFT);
					//showMiddlePage = true;
										
					break;

				case PAGE_RIGHT:
					//showAgenda.setDayDiff(showAgenda.getDayDiff() + 1);
					showAgenda.nextItem();
					//h.msgbox(showAgenda.getURL());
					
					// exchange right and middle page, so that the right
					// page can be displayed in the middle
					// and user can continue scrolling
					swapPages(PAGE_MIDDLE, PAGE_LEFT);
					swapPages(PAGE_MIDDLE, PAGE_RIGHT);
						
					// commit changes of showDate to the whole model
					setModelPages(showAgenda);
						
					// now load new content (of new date) to the left page
					setContent(PAGE_RIGHT);
					//showMiddlePage = true;
					//}
					
					break;

				}

				
				// show the middle page to the user (which now contains the
				// previous left/right page)
				viewPager.setCurrentItem(PAGE_MIDDLE, false);
				
				
				// h.msgbox(middlePage.getIndex()+"");
				// h.msgbox(showDate.getLocaleString());
			}
	}
	
	private class MyPagerAdaper extends PagerAdapter {
		final static int WEB_WIDTH = 380;
		
		private int getScale(){
		    Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		    int width = display.getWidth(); 
		    Double val = new Double(width)/new Double(WEB_WIDTH);
		    val = val * 100d;
		    return val.intValue();
		}
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			// we only need three pages
			return PAGE_RIGHT + 1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			View myView = mInflater.inflate(R.layout.webview_layout, container,
					false);

			PageModel currentPage = mPageModel[position];
			/*
			 * TextView textView = (TextView) myView.findViewById(R.id.tex);
			 * currentPage.textView = textView; if (textView != null) {
			 * textView.setText(position+""); }
			 */

			WebView webView1 = (WebView) myView.findViewById(R.id.webView1);
			WebView webView2 = (WebView) myView.findViewById(R.id.webView2);
			ProgressBar progressBar = (ProgressBar) myView
					.findViewById(R.id.progressBar);
			RelativeLayout relWebLayout = (RelativeLayout) myView
					.findViewById(R.id.relWebLayout);

			currentPage.relWebLayout = relWebLayout;
			currentPage.webView1 = webView1;
			currentPage.webView2 = webView2;
			currentPage.webDownX = 0;
			currentPage.progressBar = progressBar;
			// progressBar.setVisibility(View.VISIBLE);

			if (webView1 != null && webView2 != null) {
				webView1.setWebViewClient(new MyWebClient(progressBar));
				webView1.getSettings().setCacheMode(
						WebSettings.LOAD_CACHE_ELSE_NETWORK);
				//webView1.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);			
			    
				//webView1.getSettings().setLoadWithOverviewMode(true);
			    //webView1.getSettings().setUseWideViewPort(true);
				webView1.setInitialScale((int)Math.round(getScale()*1.005));
				
				webView2.setWebViewClient(new MyWebClient(progressBar));
				webView2.getSettings().setCacheMode(
						WebSettings.LOAD_CACHE_ELSE_NETWORK);
				//webView2.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);	
				
				//webView2.getSettings().setLoadWithOverviewMode(true);
			    //webView2.getSettings().setUseWideViewPort(true);
				webView2.setInitialScale((int)Math.round(getScale()*1.00));
				
				loadURL(webView1, currentPage.myAgenda1.getURL());
				if (position == PAGE_MIDDLE) {
					loadURL(webView2, alternativeAgenda.getURL());
				}
				
				// disable horizontal scrolling
				/*
				 * OnTouchListener myTouchListener = new View.OnTouchListener()
				 * { public boolean onTouch(View v, MotionEvent event) {
				 * 
				 * switch (event.getAction()) { case MotionEvent.ACTION_DOWN: {
				 * // save the x // webDownX = event.getX(); // not necessary to
				 * save // because x will be always 0 } break;
				 * 
				 * case MotionEvent.ACTION_MOVE: case MotionEvent.ACTION_CANCEL:
				 * case MotionEvent.ACTION_UP: { // set x always to 0, so that
				 * it doesn't move event.setLocation(0, event.getY()); } break;
				 * 
				 * }
				 * 
				 * return false; } };
				 * webView1.setOnTouchListener(myTouchListener);
				 * webView2.setOnTouchListener(myTouchListener);
				 */
			}

			container.addView(myView);
			return myView;

		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}
	}

	// Make ProgressBar disappear when content loaded and handle link clicks
	
	// load sesc html-ajax and apply according css
	private void loadURL(final WebView myWebView, final String myUrl) {
		//h.msgbox(myUrl);
		a.id(myWebView).ajax(myUrl, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String html, AjaxStatus status) {

				if (html == null) {
					// if there is no result, then set standard text
					html = "<h3>Não foi possivel carregar o conteúdo do site. Por favor verifique a conexão à internet.</h3>";
				}	
					
					AssetManager am = getAssets();
					try {
						// debug
						//html = "<p>"+url+"</p>"+html; 
						
						// on click onto images, show them in full screen
						html = html
								.replaceAll(
										"(<a href=\")([^\"]*)(\" title=\"saiba mais\" class=\"frame_overflow_img\">[^<]*<img src=\")([^\"]*)(\")",
										"$1$4$3$4$5");
						
						// replace img-width
						//html = html.replace("<img width=\"354\"","<img width=\"50%\"");
						// only open new browser if "saiba mais" button clicked
						// (mark url with "?button")
						html = html
								.replaceAll(
										"(<a href=\")([^\"]*)(\" title=\"Saiba mais\" class=\"bt_branco\">)Saiba mais",
										"$1$2"+ Matcher.quoteReplacement("?button")+"$3"+"Abre no navegador");
										
						InputStream is = am.open("header.html");
						String htmlHeader = Helper.convertStreamToString(is);

						html = htmlHeader + html
								+ "<br /><br /><br /><br /></html>";
						myWebView.loadDataWithBaseURL(
								"http://www.sescsp.org.br/programacao/ajax/",
								html, "text/html", null, myUrl);

					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}

				} 
		});
	}

	private class MyWebClient extends WebViewClient {
		private ProgressBar progressBar;

		public MyWebClient(ProgressBar progressBar) {
			this.progressBar = progressBar;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			/*
			 * if (!loadingFinished) { redirect = true; }
			 * 
			 * loadingFinished = false;
			 */

			// show images in seperate zoomable activity window
			if (url.contains(".jpg")) {
				Intent intent = new Intent(getApplicationContext(),
						ShowImageActivity.class);
				// EditText editText = (EditText)
				// findViewById(R.id.edit_message);
				intent.putExtra(IMG_MSG, url);
				startActivity(intent);
			}
			// show other contentpages in same window
			if (url.contains("&page=")) {
				loadURL(view, url);
			}
			// if button "sabia mais" clicked, then open a new browser activity
			// window
			else if (url.contains("?button")) {
				view.getContext().startActivity(
						new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
			return true;

		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// loadingFinished = false;
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			/*
			 * super.onPageFinished(view, url);
			 * 
			 * if (!redirect) { loadingFinished = true; }
			 */

			// if (loadingFinished && !redirect) {
			// page finished: disable progress bar, show webview after 100ms
			/*
			 * finishedWebView = view; view.postDelayed(new Runnable() {
			 * 
			 * @Override public void run() { if (finishedWebView.getVisibility()
			 * == View.GONE) finishedWebView.setVisibility(View.VISIBLE);
			 * progressBar.setVisibility(View.GONE); } }, 100);
			 */
			
			if (view.getVisibility() == View.GONE) {
				view.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			}

			/*
			 * } else { redirect = false; }
			 */

		}

	}
	
	
    // select a data to display
	public static class DatePickerFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = showAgenda.getCalendar();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(),
					(PageViewActivity) getActivity(), year, month, day);
		}

	}

	// load content according to selected date
	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		// set the model to the new date
		Calendar newAgendaDate = Calendar.getInstance();
		newAgendaDate.set(Calendar.YEAR, year);
		newAgendaDate.set(Calendar.MONTH, month);
		newAgendaDate.set(Calendar.DAY_OF_MONTH, day);

		showAgenda.setCalendar(newAgendaDate);
		setModelPages(showAgenda);
		// h.msgbox(year+"-"+month+"-"+day+": dayDiff="+showDate.getDayDiff());
		setContent(PAGE_LEFT);
		setContent(PAGE_MIDDLE);
		setContent(PAGE_RIGHT);
	}

}