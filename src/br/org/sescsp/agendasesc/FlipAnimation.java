package br.org.sescsp.agendasesc;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

public class FlipAnimation extends Animation
{
    private Camera camera;
 
    //private View snapshotImage;
    private View fromView;
    private View toView;

    private float centerX;
    private float centerY;
 
    private boolean forward = true;
    
    /*private boolean createdFromSnapshot = false;
    private boolean createdToSnapshot = false;
    private boolean animationEnded = false; */
    
    /**
     * Creates a 3D flip animation between two views.
     *
     * @param fromView First view in the transition.
     * @param toView Second view in the transition.
    */
    public FlipAnimation(View fromView, View toView)
    {
        //this.snapshotImage = snapshotImage;
        this.fromView = fromView;
        this.toView = toView;
        
        setDuration(1500);
        setFillAfter(false);
        setInterpolator(new AccelerateDecelerateInterpolator());
    }
 
    public void reverse()
    {
        forward = false;
        /*View switchView = toView;
        toView = fromView;
        fromView = switchView; */
        
        /* ImageView switchImageView = toImage;
        toImage = fromImage;
        fromImage = switchImageView; */
        
    }
 
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight)
    {
        super.initialize(width, height, parentWidth, parentHeight);
        centerX = width/2;
        centerY = height/2;
        camera = new Camera();
        
    }
 
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t)
    {
        // Angle around the y-axis of the rotation at the given time
        // calculated both in radians and degrees.
        final double radians = Math.PI * interpolatedTime;
        float degrees = (float) (180.0 * radians / Math.PI);
        float offset = (float) (Math.sin(radians) * centerX/2 );
        		
        // Once we reach the midpoint in the animation, we need to hide the
        // source view and show the destination view. We also need to change
        // the angle by 180 degrees so that the destination does not come in
        // flipped around
        
        if(interpolatedTime == 0.0f) {
        	//webView.setVisibility(View.GONE);
        	//progressBar.setVisibility(View.VISIBLE);
        	//snapshotImage.setVisibility(View.INVISIBLE);
        	fromView.setVisibility(View.VISIBLE);        	
        } 
          
        if(interpolatedTime == 0.5f)
        {
            //degrees -= 180.f;
            //snapshotImage.setVisibility(View.GONE);
           	fromView.setVisibility(View.INVISIBLE);       	
           	if(toView.getVisibility() == View.INVISIBLE)
           			toView.setVisibility(View.VISIBLE);        	
        }
        
        if (interpolatedTime >= 0.5f)
        {
            degrees -= 180.f;
            //snapshotImage.setVisibility(View.GONE);
           	fromView.setVisibility(View.INVISIBLE);       	
           	if(toView.getVisibility() == View.INVISIBLE)
           			toView.setVisibility(View.VISIBLE); 
        	//webView.setVisibility(View.GONE);
        	//progressBar.setVisibility(View.VISIBLE);
            //snapshotImage.setVisibility(View.GONE);
            //((WebView)webView).
        }
        if (interpolatedTime == 1.0f)
        {
        	//webView.setVisibility(View.VISIBLE);
        	//progressBar.setVisibility(View.GONE);
        }
 
 
        if (forward)
            degrees = -degrees; //determines direction of rotation when flip begins
 
        final Matrix matrix = t.getMatrix();
        camera.save();
        camera.rotateY(degrees);
        //camera.rotateX(degrees);
        camera.translate(offset, 0f, 0f);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
        
    }
    
	/*private void showSnapshot(View snapshotView, ImageView imageView) {
		//Bitmap snapshot = null;
	    //Drawable drawable = null;
				
		//snapshotView.setVisibility(View.VISIBLE);
	    snapshotView.setDrawingCacheEnabled(true);
	    
	    snapshotView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW); //Quality of the snapshot
	    try {
	        //snapshot = Bitmap.createBitmap(snapshotView.getDrawingCache()); // You can tell how to crop the snapshot and whatever in this method
	        //drawable = new BitmapDrawable(snapshot);
	        imageView.setImageBitmap(snapshotView.getDrawingCache());
	        imageView.setVisibility(View.VISIBLE);
	    } finally {
	    	snapshotView.setDrawingCacheEnabled(false);
	    }	
	        
	}

	private void hideSnapshot(View snapshotView, ImageView imageView) {
        imageView.setVisibility(View.GONE);
        snapshotView.setDrawingCacheEnabled(false);
	} */
    
}